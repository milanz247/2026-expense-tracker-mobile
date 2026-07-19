package com.example.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class ApiException(message: String) : Exception(message)

/**
 * All application data lives on the Go backend — this repository holds no
 * local database. It keeps the current lists in memory (StateFlow) and
 * re-fetches from the API after every mutation. Only the auth session
 * (token/server URL/profile) is cached on-device, via DataStore
 * Preferences, purely so the user isn't forced to log in on every launch.
 */
class FinanceRepository(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val CURRENCY = stringPreferencesKey("currency")
        val TIMEZONE = stringPreferencesKey("timezone")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val darkThemeFlow: kotlinx.coroutines.flow.Flow<Boolean> =
        context.sessionDataStore.data.map { it[Keys.DARK_THEME] ?: true }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.sessionDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    // ==========================================
    // App lock (PIN + biometric) — purely a local device setting. The PIN
    // is never sent to the backend: only its SHA-256 hash lives in
    // DataStore, and biometric matching never leaves the OS's own
    // BiometricPrompt/keystore. This gates re-entry into an already
    // logged-in session, it isn't a second account credential.
    // ==========================================

    val hasPinLockFlow: kotlinx.coroutines.flow.Flow<Boolean> =
        context.sessionDataStore.data.map { it[Keys.PIN_HASH] != null }

    val biometricEnabledFlow: kotlinx.coroutines.flow.Flow<Boolean> =
        context.sessionDataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    private fun sha256(value: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    suspend fun setPin(pin: String) {
        context.sessionDataStore.edit { it[Keys.PIN_HASH] = sha256(pin) }
    }

    suspend fun clearPin() {
        context.sessionDataStore.edit {
            it.remove(Keys.PIN_HASH)
            it.remove(Keys.BIOMETRIC_ENABLED)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.sessionDataStore.data.first()[Keys.PIN_HASH] ?: return false
        return stored == sha256(pin)
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.sessionDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val errorAdapter = moshi.adapter(ApiErrorBody::class.java)

    private var baseServerUrl = com.example.BuildConfig.API_BASE_URL
    private var authToken: String? = null
    private var apiService: ApiService? = null

    private val _userProfile = MutableStateFlow<LocalUserProfile?>(null)
    val userProfileFlow: StateFlow<LocalUserProfile?> = _userProfile.asStateFlow()

    private val _accounts = MutableStateFlow<List<LocalAccount>>(emptyList())
    val accountsFlow: StateFlow<List<LocalAccount>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<LocalCategory>>(emptyList())
    val categoriesFlow: StateFlow<List<LocalCategory>> = _categories.asStateFlow()

    private val _transactions = MutableStateFlow<List<LocalTransaction>>(emptyList())
    val transactionsFlow: StateFlow<List<LocalTransaction>> = _transactions.asStateFlow()

    private val _debts = MutableStateFlow<List<LocalDebt>>(emptyList())
    val debtsFlow: StateFlow<List<LocalDebt>> = _debts.asStateFlow()

    private val _storeCreditors = MutableStateFlow<List<LocalStoreCreditor>>(emptyList())
    val storeCreditorsFlow: StateFlow<List<LocalStoreCreditor>> = _storeCreditors.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            restoreSession()
        }
    }

    // ==========================================
    // Session bootstrap / persistence (DataStore only — no data database)
    // ==========================================

    private suspend fun restoreSession() {
        val prefs = context.sessionDataStore.data.first()
        val token = prefs[Keys.TOKEN]
        if (token != null) {
            authToken = token
            baseServerUrl = com.example.BuildConfig.API_BASE_URL
            rebuildApiService()
            _userProfile.value = LocalUserProfile(
                name = prefs[Keys.NAME] ?: "",
                email = prefs[Keys.EMAIL] ?: "",
                currency = prefs[Keys.CURRENCY] ?: "USD",
                timezone = prefs[Keys.TIMEZONE] ?: "Asia/Colombo",
                token = token,
                baseServerUrl = baseServerUrl
            )
            try {
                refreshAll()
            } catch (e: Exception) {
                // A stale/expired token 401s through the interceptor, which logs out.
            }
        }
    }

    private suspend fun persistSession(profile: LocalUserProfile) {
        context.sessionDataStore.edit { p ->
            p[Keys.TOKEN] = profile.token
            p[Keys.NAME] = profile.name
            p[Keys.EMAIL] = profile.email
            p[Keys.CURRENCY] = profile.currency
            p[Keys.TIMEZONE] = profile.timezone
        }
    }

    private fun rebuildApiService() {
        val url = if (baseServerUrl.endsWith("/")) baseServerUrl else "$baseServerUrl/"
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            authToken?.let { requestBuilder.header("Authorization", "Bearer $it") }
            val response = chain.proceed(requestBuilder.build())
            if (response.code == 401 && authToken != null) {
                CoroutineScope(Dispatchers.IO).launch { logout() }
            }
            response
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private fun requireApi(): ApiService = apiService ?: throw ApiException("Not connected to the server.")

    private fun humanizeError(e: Throwable): ApiException {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string()
            val parsed = body?.let {
                try { errorAdapter.fromJson(it)?.error } catch (_: Exception) { null }
            }
            return ApiException(parsed ?: "Request failed (HTTP ${e.code()})")
        }
        if (e is IOException) {
            return ApiException("Could not reach the server. Check your connection and server URL.")
        }
        return ApiException(e.message ?: "Something went wrong.")
    }

    private suspend fun <T> call(block: suspend (ApiService) -> T): T = withContext(Dispatchers.IO) {
        try {
            block(requireApi())
        } catch (e: Exception) {
            throw humanizeError(e)
        }
    }

    fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())
    }

    /** Backend fields typed as time.Time require a full RFC3339 timestamp. */
    private fun toRfc3339(dateOnlyOrFull: String): String {
        return if (dateOnlyOrFull.length == 10) "${dateOnlyOrFull}T00:00:00Z" else dateOnlyOrFull
    }

    // ==========================================
    // Auth & Profile
    // ==========================================

    suspend fun register(name: String, email: String, password: String, currency: String) {
        baseServerUrl = com.example.BuildConfig.API_BASE_URL
        rebuildApiService()
        call { it.register(RegisterRequest(name, email, password, currency)) }
        // Registration doesn't return a session token, so log in immediately after.
        login(email, password)
    }

    suspend fun login(email: String, password: String) {
        baseServerUrl = com.example.BuildConfig.API_BASE_URL
        rebuildApiService()
        val response = call { it.login(LoginRequest(email, password)) }
        authToken = response.token
        rebuildApiService()
        val profile = LocalUserProfile(
            name = response.user.name,
            email = email,
            currency = response.user.currency,
            timezone = "Asia/Colombo",
            token = response.token,
            baseServerUrl = baseServerUrl
        )
        persistSession(profile)
        _userProfile.value = profile
        try {
            refreshAll()
        } catch (_: Exception) {
            // Session is valid even if the first data refresh fails; screens retry individually.
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        authToken = null
        apiService = null
        context.sessionDataStore.edit { prefs ->
            // Keep device-level settings — theme and app-lock are not part
            // of the session, they persist across logins on this device.
            val darkTheme = prefs[Keys.DARK_THEME]
            val pinHash = prefs[Keys.PIN_HASH]
            val biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED]
            prefs.clear()
            if (darkTheme != null) prefs[Keys.DARK_THEME] = darkTheme
            if (pinHash != null) prefs[Keys.PIN_HASH] = pinHash
            if (biometricEnabled != null) prefs[Keys.BIOMETRIC_ENABLED] = biometricEnabled
        }
        _userProfile.value = null
        _accounts.value = emptyList()
        _categories.value = emptyList()
        _transactions.value = emptyList()
        _debts.value = emptyList()
        _storeCreditors.value = emptyList()
    }

    suspend fun updateProfile(name: String, currency: String, timezone: String) {
        val response = call { it.updateProfile(ProfileUpdateRequest(name, currency, timezone)) }
        val current = _userProfile.value ?: return
        val updated = current.copy(name = response.name, currency = response.currency, timezone = response.timezone)
        persistSession(updated)
        _userProfile.value = updated
    }

    suspend fun changePassword(old: String, new: String, confirm: String) {
        call { it.updatePassword(PasswordUpdateRequest(old, new, confirm)) }
    }

    // ==========================================
    // Full data refresh (after login and after mutations)
    // ==========================================

    suspend fun refreshAll() = withContext(Dispatchers.IO) {
        loadAccounts()
        loadCategories()
        loadTransactions()
        loadDebts()
        loadStoreCreditors()
    }

    suspend fun loadAccounts() {
        val accounts = call { it.getAccounts() }
        _accounts.value = accounts.map { it.toLocal() }
    }

    suspend fun loadCategories() {
        val categories = call { it.getCategories() }
        _categories.value = categories.map { it.toLocal() }
    }

    suspend fun loadTransactions(accountId: Long? = null) {
        val txs = call { it.getTransactions(accountId) }
        _transactions.value = txs.map { it.toLocal() }
    }

    suspend fun loadDebts() {
        val debts = call { it.getDebts() }
        _debts.value = debts.map { it.toLocal() }
    }

    suspend fun loadStoreCreditors() {
        val creditors = call { it.getStoreCreditors() }
        _storeCreditors.value = creditors.map { it.toLocal() }
    }

    suspend fun getReportSummary(): ReportSummaryResponse = call { it.getReportSummary() }

    // ==========================================
    // Wallets (Accounts)
    // ==========================================

    suspend fun createAccount(
        name: String,
        type: String,
        initialBalance: Double,
        creditLimit: Double,
        branchName: String = "",
        accountNumber: String = "",
        holderName: String = ""
    ) {
        call {
            it.createAccount(
                AccountRequest(
                    name = name,
                    type = type,
                    initialBalance = initialBalance,
                    creditLimit = creditLimit,
                    branchName = branchName,
                    accountNumber = accountNumber,
                    holderName = holderName
                )
            )
        }
        loadAccounts()
    }

    suspend fun updateAccount(
        id: Long,
        name: String,
        type: String,
        branchName: String = "",
        accountNumber: String = "",
        holderName: String = ""
    ) {
        call {
            it.updateAccount(
                id,
                AccountUpdateRequest(
                    name = name,
                    type = type,
                    branchName = branchName,
                    accountNumber = accountNumber,
                    holderName = holderName
                )
            )
        }
        loadAccounts()
    }

    suspend fun deleteAccount(id: Long) {
        call { it.deleteAccount(id) }
        loadAccounts()
    }

    // ==========================================
    // Categories
    // ==========================================

    suspend fun createCategory(name: String, type: String, color: String, icon: String) {
        call { it.createCategory(CategoryRequest(name, type, color, icon)) }
        loadCategories()
    }

    suspend fun updateCategory(id: Long, name: String, color: String, icon: String) {
        val type = _categories.value.find { it.id == id }?.type ?: "expense"
        call { it.updateCategory(id, CategoryRequest(name, type, color, icon)) }
        loadCategories()
    }

    suspend fun deleteCategory(id: Long) {
        call { it.deleteCategory(id) }
        loadCategories()
    }

    // ==========================================
    // Transactions
    // ==========================================

    suspend fun createTransaction(
        type: String,
        amount: Double,
        fee: Double,
        accountId: Long?,
        sourceAccountId: Long?,
        destinationAccountId: Long?,
        categoryId: Long?,
        description: String,
        date: String
    ) {
        call {
            it.createTransaction(
                TransactionRequest(
                    type = type,
                    amount = amount,
                    fee = fee,
                    accountId = accountId,
                    sourceAccountId = sourceAccountId,
                    destinationAccountId = destinationAccountId,
                    categoryId = categoryId,
                    description = description,
                    date = toRfc3339(date)
                )
            )
        }
        loadTransactions()
        loadAccounts()
    }

    // ==========================================
    // Debts
    // ==========================================

    suspend fun createDebt(personName: String, type: String, totalAmount: Double, accountId: Long, dueDate: String) {
        call { it.createDebt(DebtRequest(personName, type, totalAmount, accountId, toRfc3339(dueDate))) }
        loadDebts()
        loadAccounts()
        loadTransactions()
    }

    suspend fun repayDebt(debtId: Long, amount: Double, fee: Double, accountId: Long, date: String) {
        call {
            it.repayDebt(
                debtId,
                RepaymentRequest(
                    repaymentAmount = amount,
                    fee = fee,
                    accountId = accountId,
                    date = toRfc3339(date)
                )
            )
        }
        loadDebts()
        loadAccounts()
        loadTransactions()
    }

    // ==========================================
    // Store Tabs (Shop Creditors)
    // ==========================================

    suspend fun createStoreCreditor(name: String) {
        call { it.createStoreCreditor(StoreCreditorRequest(name)) }
        loadStoreCreditors()
    }

    suspend fun createPurchase(creditorId: Long, amount: Double, fee: Double, categoryId: Long, description: String, date: String) {
        call { it.createPurchase(creditorId, PurchaseRequest(amount, fee, categoryId, description, toRfc3339(date))) }
        loadStoreCreditors()
        loadTransactions()
    }

    suspend fun createSettlement(creditorId: Long, accountId: Long, amountPaid: Double, fee: Double, date: String) {
        call { it.createSettlement(creditorId, SettlementRequest(accountId, amountPaid, fee, toRfc3339(date))) }
        loadStoreCreditors()
        loadAccounts()
        loadTransactions()
    }

    suspend fun getPurchasesForCreditor(creditorId: Long): List<LocalPurchase> =
        call { it.getPurchases(creditorId) }.map { it.toLocal() }

    suspend fun getSettlementsForCreditor(creditorId: Long): List<LocalSettlement> =
        call { it.getSettlements(creditorId) }.map { it.toLocal() }

    // ==========================================
    // Exports (server-generated CSV/PDF — never generated on-device)
    // ==========================================

    suspend fun exportReport(
        format: String, // csv, pdf
        startDate: String?,
        endDate: String?,
        accountId: Long?,
        categoryId: Long?,
        type: String?
    ): Uri = withContext(Dispatchers.IO) {
        val body = call {
            it.exportReport(format, startDate, endDate, accountId, categoryId, type)
        }
        val extension = if (format == "pdf") "pdf" else "csv"
        val dir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val outFile = File(dir, "statement_${System.currentTimeMillis()}.$extension")
        body.byteStream().use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }
}

// ==========================================
// API response -> display model mapping
// ==========================================

private fun AccountResponse.toLocal() = LocalAccount(
    id = id, name = name, type = type, balance = balance,
    creditLimit = creditLimit, isActive = isActive,
    branchName = branchName, accountNumber = accountNumber, holderName = holderName,
    createdAt = createdAt
)

private fun CategoryResponse.toLocal() = LocalCategory(
    id = id, name = name, type = type, color = color,
    icon = icon, isSystem = isSystem, createdAt = createdAt
)

private fun TransactionResponse.toLocal() = LocalTransaction(
    id = id, type = type, amount = amount, fee = fee,
    accountId = accountId, sourceAccountId = sourceAccountId,
    destinationAccountId = destinationAccountId, categoryId = categoryId,
    description = description, date = date, createdAt = createdAt
)

private fun DebtResponse.toLocal() = LocalDebt(
    id = id, personName = personName, type = type, totalAmount = totalAmount,
    remainingAmount = remainingAmount, status = status, dueDate = dueDate, createdAt = createdAt
)

private fun StoreCreditorResponse.toLocal() = LocalStoreCreditor(
    id = id, name = name, outstandingDebt = outstandingDebt, isActive = isActive, createdAt = createdAt
)

private fun PurchaseResponse.toLocal() = LocalPurchase(
    id = id, creditorId = creditorId, amount = amount, fee = fee,
    categoryId = categoryId, description = description, date = date, createdAt = createdAt
)

private fun SettlementResponse.toLocal() = LocalSettlement(
    id = id, creditorId = creditorId, accountId = accountId, amountPaid = amountPaid,
    fee = fee, date = date, createdAt = createdAt
)
