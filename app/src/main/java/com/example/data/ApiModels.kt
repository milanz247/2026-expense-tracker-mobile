package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// ==========================================
// User & Auth DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "currency") val currency: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "currency") val currency: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
// The backend's login response only ever includes name + currency here
// (see AuthUser in backend/internal/user/dto.go) — no email field.
data class UserProfile(
    @Json(name = "name") val name: String,
    @Json(name = "currency") val currency: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: UserProfile
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "timezone") val timezone: String
)

@JsonClass(generateAdapter = true)
data class ProfileUpdateRequest(
    @Json(name = "name") val name: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "timezone") val timezone: String
)

@JsonClass(generateAdapter = true)
data class PasswordUpdateRequest(
    @Json(name = "old_password") val oldPassword: String,
    @Json(name = "new_password") val newPassword: String,
    @Json(name = "confirm_password") val confirmPassword: String
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class ApiErrorBody(
    @Json(name = "error") val error: String
)

// ==========================================
// Wallet (Account) DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class AccountRequest(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "initial_balance") val initialBalance: Double,
    @Json(name = "credit_limit") val creditLimit: Double
)

@JsonClass(generateAdapter = true)
data class AccountUpdateRequest(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String
)

@JsonClass(generateAdapter = true)
data class AccountResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "balance") val balance: Long, // in cents
    @Json(name = "credit_limit") val creditLimit: Long, // in cents
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String
)

// ==========================================
// Category DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class CategoryRequest(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "color") val color: String,
    @Json(name = "icon") val icon: String
)

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "color") val color: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "is_system") val isSystem: Boolean,
    @Json(name = "created_at") val createdAt: String
)

// ==========================================
// Transaction DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class TransactionRequest(
    @Json(name = "type") val type: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "fee") val fee: Double,
    @Json(name = "account_id") val accountId: Long?,
    @Json(name = "source_account_id") val sourceAccountId: Long?,
    @Json(name = "destination_account_id") val destinationAccountId: Long?,
    @Json(name = "category_id") val categoryId: Long?,
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "type") val type: String,
    @Json(name = "amount") val amount: Long, // cents
    @Json(name = "fee") val fee: Long, // cents
    @Json(name = "account_id") val accountId: Long?,
    @Json(name = "account") val account: AccountResponse?,
    @Json(name = "source_account_id") val sourceAccountId: Long?,
    @Json(name = "source_account") val sourceAccount: AccountResponse?,
    @Json(name = "destination_account_id") val destinationAccountId: Long?,
    @Json(name = "destination_account") val destinationAccount: AccountResponse?,
    @Json(name = "category_id") val categoryId: Long?,
    @Json(name = "category") val category: CategoryResponse?,
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String,
    @Json(name = "created_at") val createdAt: String
)

// ==========================================
// Debt DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class DebtRequest(
    @Json(name = "person_name") val personName: String,
    @Json(name = "type") val type: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "due_date") val dueDate: String
)

@JsonClass(generateAdapter = true)
data class DebtResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "person_name") val personName: String,
    @Json(name = "type") val type: String,
    @Json(name = "total_amount") val totalAmount: Long, // cents
    @Json(name = "remaining_amount") val remainingAmount: Long, // cents
    @Json(name = "status") val status: String,
    @Json(name = "due_date") val dueDate: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class RepaymentRequest(
    @Json(name = "repayment_amount") val repaymentAmount: Double,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class RepaymentResponse(
    @Json(name = "debt_id") val debtId: Long,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "amount") val amount: Long, // cents
    @Json(name = "date") val date: String
)

// ==========================================
// Store Tab DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class StoreCreditorRequest(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class StoreCreditorResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "outstanding_debt") val outstandingDebt: Long, // cents
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class PurchaseRequest(
    @Json(name = "amount") val amount: Double,
    @Json(name = "fee") val fee: Double,
    @Json(name = "category_id") val categoryId: Long,
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class PurchaseResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "creditor_id") val creditorId: Long,
    @Json(name = "amount") val amount: Long, // cents
    @Json(name = "fee") val fee: Long, // cents
    @Json(name = "category_id") val categoryId: Long,
    @Json(name = "category") val category: CategoryResponse?,
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class SettlementRequest(
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "fee") val fee: Double,
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class SettlementResponse(
    @Json(name = "id") val id: Long,
    @Json(name = "creditor_id") val creditorId: Long,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "account") val account: AccountResponse?,
    @Json(name = "amount_paid") val amountPaid: Long, // cents
    @Json(name = "fee") val fee: Long, // cents
    @Json(name = "date") val date: String,
    @Json(name = "created_at") val createdAt: String
)

// ==========================================
// Reports DTOs
// ==========================================

@JsonClass(generateAdapter = true)
data class ReportSummaryAmount(
    @Json(name = "amount_cents") val amountCents: Long,
    @Json(name = "change_percent") val changePercent: Double?
)

@JsonClass(generateAdapter = true)
data class CashFlowEntry(
    @Json(name = "month") val month: String,
    @Json(name = "income") val income: Long, // cents
    @Json(name = "expense") val expense: Long // cents
)

@JsonClass(generateAdapter = true)
data class CategoryBreakdownEntry(
    @Json(name = "category_name") val categoryName: String,
    @Json(name = "amount_cents") val amountCents: Long,
    @Json(name = "percentage") val percentage: Double,
    @Json(name = "color") val color: String
)

@JsonClass(generateAdapter = true)
data class ReportSummaryRecentTransaction(
    @Json(name = "id") val id: Long,
    @Json(name = "type") val type: String,
    @Json(name = "amount") val amount: Long, // cents
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String,
    @Json(name = "category") val category: ReportSummaryCategory?
)

@JsonClass(generateAdapter = true)
data class ReportSummaryCategory(
    @Json(name = "name") val name: String,
    @Json(name = "color") val color: String,
    @Json(name = "icon") val icon: String
)

@JsonClass(generateAdapter = true)
data class ReportSummaryResponse(
    @Json(name = "total_income") val totalIncome: ReportSummaryAmount,
    @Json(name = "total_expense") val totalExpense: ReportSummaryAmount,
    @Json(name = "net_balance") val netBalance: ReportSummaryAmount,
    @Json(name = "cash_flow") val cashFlow: List<CashFlowEntry>,
    @Json(name = "category_breakdown") val categoryBreakdown: List<CategoryBreakdownEntry>,
    @Json(name = "recent_transactions") val recentTransactions: List<ReportSummaryRecentTransaction>
)

// ==========================================
// Retrofit ApiService Interface
// ==========================================

interface ApiService {

    // --- Auth & Profile ---
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ProfileResponse

    @POST("profile/password")
    suspend fun updatePassword(@Body request: PasswordUpdateRequest): MessageResponse

    // --- Accounts (Wallets) ---
    @POST("accounts")
    suspend fun createAccount(@Body request: AccountRequest): AccountResponse

    @GET("accounts")
    suspend fun getAccounts(): List<AccountResponse>

    @PUT("accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Long, @Body request: AccountUpdateRequest): AccountResponse

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: Long): Response<Unit>

    // --- Transactions ---
    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionResponse

    @GET("transactions")
    suspend fun getTransactions(@Query("account_id") accountId: Long?): List<TransactionResponse>

    // --- Categories ---
    @POST("categories")
    suspend fun createCategory(@Body request: CategoryRequest): CategoryResponse

    @GET("categories")
    suspend fun getCategories(): List<CategoryResponse>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body request: CategoryRequest): CategoryResponse

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<Unit>

    // --- Debts ---
    @POST("debts")
    suspend fun createDebt(@Body request: DebtRequest): DebtResponse

    @GET("debts")
    suspend fun getDebts(): List<DebtResponse>

    @POST("debts/{id}/repay")
    suspend fun repayDebt(@Path("id") id: Long, @Body request: RepaymentRequest): DebtResponse

    // --- Store Tabs (Creditors) ---
    @POST("store-creditors")
    suspend fun createStoreCreditor(@Body request: StoreCreditorRequest): StoreCreditorResponse

    @GET("store-creditors")
    suspend fun getStoreCreditors(): List<StoreCreditorResponse>

    @POST("store-creditors/{id}/purchases")
    suspend fun createPurchase(@Path("id") id: Long, @Body request: PurchaseRequest): PurchaseResponse

    @GET("store-creditors/{id}/purchases")
    suspend fun getPurchases(@Path("id") id: Long): List<PurchaseResponse>

    @POST("store-creditors/{id}/settlements")
    suspend fun createSettlement(@Path("id") id: Long, @Body request: SettlementRequest): SettlementResponse

    @GET("store-creditors/{id}/settlements")
    suspend fun getSettlements(@Path("id") id: Long): List<SettlementResponse>

    // --- Reports ---
    @GET("reports/summary")
    suspend fun getReportSummary(): ReportSummaryResponse

    @GET("reports/export")
    @Streaming
    suspend fun exportReport(
        @Query("format") format: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
        @Query("account_id") accountId: Long?,
        @Query("category_id") categoryId: Long?,
        @Query("type") type: String?
    ): ResponseBody
}
