package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==========================================
// Client-side display/session models.
// Populated entirely from Go backend API responses — there is no local
// database. UserSession is the only thing persisted on-device (via
// DataStore Preferences) so the user doesn't have to log in every launch.
// ==========================================

@JsonClass(generateAdapter = true)
data class UserSession(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "currency") val currency: String,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "token") val token: String,
    @Json(name = "baseServerUrl") val baseServerUrl: String
)

// Kept as a type alias-like wrapper name for compatibility with the rest of
// the UI layer, which refers to the authenticated user as "LocalUserProfile".
typealias LocalUserProfile = UserSession

data class LocalAccount(
    val id: Long = 0L,
    val name: String,
    val type: String, // bank, cash, credit_card, investment
    val balance: Long, // cents
    val creditLimit: Long, // cents
    val isActive: Boolean = true,
    val branchName: String? = null,
    val accountNumber: String? = null,
    val holderName: String? = null,
    val createdAt: String = ""
)

data class LocalCategory(
    val id: Long = 0L,
    val name: String,
    val type: String, // income, expense
    val color: String, // Tailwind token
    val icon: String, // Lucide icon
    val isSystem: Boolean = false,
    val createdAt: String = ""
)

data class LocalTransaction(
    val id: Long = 0L,
    val type: String, // income, expense, transfer
    val amount: Long, // cents
    val fee: Long = 0L, // cents
    val accountId: Long?,
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null,
    val categoryId: Long?,
    val description: String,
    val date: String,
    val createdAt: String = ""
)

data class LocalDebt(
    val id: Long = 0L,
    val personName: String,
    val type: String, // lent, borrowed
    val totalAmount: Long, // cents
    val remainingAmount: Long, // cents
    val status: String, // pending, settled
    val dueDate: String,
    val createdAt: String = ""
)

data class LocalRepayment(
    val id: Long = 0L,
    val debtId: Long,
    val accountId: Long,
    val amount: Long, // cents
    val fee: Long = 0L, // cents
    val date: String
)

data class LocalStoreCreditor(
    val id: Long = 0L,
    val name: String,
    val outstandingDebt: Long, // cents
    val isActive: Boolean = true,
    val createdAt: String = ""
)

data class LocalPurchase(
    val id: Long = 0L,
    val creditorId: Long,
    val amount: Long, // cents
    val fee: Long = 0L, // cents
    val categoryId: Long,
    val description: String,
    val date: String,
    val createdAt: String = ""
)

data class LocalSettlement(
    val id: Long = 0L,
    val creditorId: Long,
    val accountId: Long,
    val amountPaid: Long, // cents
    val fee: Long = 0L, // cents
    val date: String,
    val createdAt: String = ""
)
