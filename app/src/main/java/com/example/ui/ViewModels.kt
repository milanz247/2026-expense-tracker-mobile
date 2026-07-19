package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val profile: LocalUserProfile) : AuthState
    data class Error(val message: String) : AuthState
}

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // --- Global toast/error channel for non-auth mutations ---
    private val _events = MutableSharedFlow<String>(replay = 0)
    val events: SharedFlow<String> = _events.asSharedFlow()

    // --- Theme (device-level preference, independent of login session) ---
    val darkTheme: StateFlow<Boolean> = repository.darkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    // --- App lock (PIN + biometric, local device setting) ---
    val hasPinLock: StateFlow<Boolean> = repository.hasPinLockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val biometricEnabled: StateFlow<Boolean> = repository.biometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setPin(pin: String) {
        viewModelScope.launch { repository.setPin(pin) }
    }

    fun clearPin() {
        viewModelScope.launch { repository.clearPin() }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    suspend fun verifyPin(pin: String): Boolean = repository.verifyPin(pin)

    private fun launchGuarded(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: ApiException) {
                _events.emit(e.message ?: "Something went wrong.")
            }
        }
    }

    // --- Authentication ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                if (profile != null) {
                    _authState.value = AuthState.Authenticated(profile)
                } else if (_authState.value !is AuthState.Loading) {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, currency: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.register(name, email, password, currency)
            } catch (e: ApiException) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.login(email, password)
            } catch (e: ApiException) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // --- Accounts / Wallets ---
    val accounts: StateFlow<List<LocalAccount>> = repository.accountsFlow

    fun createAccount(
        name: String,
        type: String,
        initialBalance: Double,
        creditLimit: Double,
        branchName: String = "",
        accountNumber: String = "",
        holderName: String = ""
    ) = launchGuarded {
        repository.createAccount(name, type, initialBalance, creditLimit, branchName, accountNumber, holderName)
        refreshReports()
    }

    fun updateAccount(
        id: Long,
        name: String,
        type: String,
        branchName: String = "",
        accountNumber: String = "",
        holderName: String = ""
    ) = launchGuarded {
        repository.updateAccount(id, name, type, branchName, accountNumber, holderName)
    }

    fun deleteAccount(id: Long) = launchGuarded {
        repository.deleteAccount(id)
    }

    // --- Categories ---
    val categories: StateFlow<List<LocalCategory>> = repository.categoriesFlow

    fun createCategory(name: String, type: String, color: String, icon: String) = launchGuarded {
        repository.createCategory(name, type, color, icon)
    }

    fun updateCategory(id: Long, name: String, color: String, icon: String) = launchGuarded {
        repository.updateCategory(id, name, color, icon)
    }

    fun deleteCategory(id: Long) = launchGuarded {
        repository.deleteCategory(id)
    }

    // --- Transactions ---
    private val _selectedAccountFilter = MutableStateFlow<Long?>(null)
    val selectedAccountFilter = _selectedAccountFilter.asStateFlow()

    fun filterTransactionsByAccount(accountId: Long?) {
        _selectedAccountFilter.value = accountId
    }

    val transactions: StateFlow<List<LocalTransaction>> = combine(
        repository.transactionsFlow,
        _selectedAccountFilter
    ) { list, filterId ->
        if (filterId == null) {
            list
        } else {
            list.filter { it.accountId == filterId || it.sourceAccountId == filterId || it.destinationAccountId == filterId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTransaction(
        type: String,
        amount: Double,
        fee: Double,
        accountId: Long?,
        sourceAccountId: Long?,
        destinationAccountId: Long?,
        categoryId: Long?,
        description: String,
        date: String
    ) = launchGuarded {
        repository.createTransaction(
            type = type,
            amount = amount,
            fee = fee,
            accountId = accountId,
            sourceAccountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            categoryId = categoryId,
            description = description,
            date = date
        )
        refreshReports()
    }

    // --- Debts ---
    val debts: StateFlow<List<LocalDebt>> = repository.debtsFlow

    private val _selectedDebtId = MutableStateFlow<Long?>(null)
    val selectedDebtId = _selectedDebtId.asStateFlow()

    // The backend has no repayment-history endpoint; repayments are recovered
    // from the real transaction ledger it does expose (type + person-name match).
    val selectedDebtRepayments: StateFlow<List<LocalRepayment>> = combine(
        _selectedDebtId,
        repository.transactionsFlow,
        debts
    ) { debtId, txs, debtList ->
        val debt = debtList.find { it.id == debtId } ?: return@combine emptyList<LocalRepayment>()
        txs.filter { tx ->
            (tx.type == "repayment_received" || tx.type == "repayment_paid") &&
                tx.description.contains(debt.personName, ignoreCase = true)
        }.map { tx ->
            LocalRepayment(
                id = tx.id,
                debtId = debt.id,
                accountId = tx.accountId ?: 0L,
                amount = tx.amount,
                fee = tx.fee,
                date = tx.date
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDebt(debtId: Long?) {
        _selectedDebtId.value = debtId
    }

    fun createDebt(personName: String, type: String, totalAmount: Double, accountId: Long, dueDate: String) = launchGuarded {
        repository.createDebt(personName, type, totalAmount, accountId, dueDate)
        refreshReports()
    }

    fun repayDebt(debtId: Long, amount: Double, fee: Double, accountId: Long) = launchGuarded {
        repository.repayDebt(debtId, amount, fee, accountId, repository.getCurrentIsoTimestamp())
        refreshReports()
    }

    // --- Store Tabs ---
    val storeCreditors: StateFlow<List<LocalStoreCreditor>> = repository.storeCreditorsFlow

    private val _selectedCreditorId = MutableStateFlow<Long?>(null)
    val selectedCreditorId = _selectedCreditorId.asStateFlow()

    private val _selectedCreditorPurchases = MutableStateFlow<List<LocalPurchase>>(emptyList())
    val selectedCreditorPurchases: StateFlow<List<LocalPurchase>> = _selectedCreditorPurchases.asStateFlow()

    private val _selectedCreditorSettlements = MutableStateFlow<List<LocalSettlement>>(emptyList())
    val selectedCreditorSettlements: StateFlow<List<LocalSettlement>> = _selectedCreditorSettlements.asStateFlow()

    fun selectCreditor(creditorId: Long?) {
        _selectedCreditorId.value = creditorId
        if (creditorId == null) {
            _selectedCreditorPurchases.value = emptyList()
            _selectedCreditorSettlements.value = emptyList()
            return
        }
        launchGuarded {
            _selectedCreditorPurchases.value = repository.getPurchasesForCreditor(creditorId)
            _selectedCreditorSettlements.value = repository.getSettlementsForCreditor(creditorId)
        }
    }

    fun createStoreCreditor(name: String) = launchGuarded {
        repository.createStoreCreditor(name)
    }

    fun createPurchase(creditorId: Long, amount: Double, fee: Double, categoryId: Long, description: String) = launchGuarded {
        repository.createPurchase(creditorId, amount, fee, categoryId, description, repository.getCurrentIsoTimestamp())
        selectCreditor(creditorId)
        refreshReports()
    }

    fun createSettlement(creditorId: Long, accountId: Long, amountPaid: Double, fee: Double) = launchGuarded {
        repository.createSettlement(creditorId, accountId, amountPaid, fee, repository.getCurrentIsoTimestamp())
        selectCreditor(creditorId)
        refreshReports()
    }

    // --- Reports ---
    private val _reportSummary = MutableStateFlow<ReportSummaryResponse?>(null)
    val reportSummary = _reportSummary.asStateFlow()

    fun refreshReports() = launchGuarded {
        _reportSummary.value = repository.getReportSummary()
    }

    // --- Exports ---
    private val _exportedUri = MutableSharedFlow<Uri>(replay = 0)
    val exportedUri = _exportedUri.asSharedFlow()

    fun exportTransactions(
        format: String, // csv, pdf
        startDate: String?,
        endDate: String?,
        accountId: Long?,
        categoryId: Long?,
        type: String?
    ) = launchGuarded {
        val uri = repository.exportReport(format, startDate, endDate, accountId, categoryId, type)
        _exportedUri.emit(uri)
    }

    // --- Settings / Profile ---
    fun updateProfile(name: String, currency: String, timezone: String) = launchGuarded {
        repository.updateProfile(name, currency, timezone)
        _events.emit("Profile updated.")
    }

    private val _passwordChangeSuccess = MutableSharedFlow<Boolean>(replay = 0)
    val passwordChangeSuccess = _passwordChangeSuccess.asSharedFlow()

    fun changePassword(old: String, new: String, confirm: String) {
        viewModelScope.launch {
            try {
                repository.changePassword(old, new, confirm)
                _passwordChangeSuccess.emit(true)
            } catch (e: ApiException) {
                _events.emit(e.message ?: "Could not change password.")
                _passwordChangeSuccess.emit(false)
            }
        }
    }

    fun getCurrentIsoTimestamp(): String = repository.getCurrentIsoTimestamp()
}

class FinanceViewModelFactory(
    private val application: Application,
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
