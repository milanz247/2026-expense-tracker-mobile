package com.example.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.network.ACCOUNT_TYPE_BANK
import com.example.network.ACCOUNT_TYPE_CASH
import com.example.network.ACCOUNT_TYPE_CREDIT_CARD
import com.example.network.ACCOUNT_TYPE_INVESTMENT
import com.example.ui.theme.AppColors

/** Maps a wallet's `type` to a recognizable Material icon — shared by every screen that lists accounts. */
fun iconForAccountType(type: String): ImageVector = when (type) {
    ACCOUNT_TYPE_BANK -> Icons.Default.AccountBalance
    ACCOUNT_TYPE_CASH -> Icons.Default.Payments
    ACCOUNT_TYPE_CREDIT_CARD -> Icons.Default.CreditCard
    ACCOUNT_TYPE_INVESTMENT -> Icons.Default.TrendingUp
    else -> Icons.Default.AccountBalance
}

/** Gives each wallet type a distinct accent instead of one flat color everywhere — cash reads as
 * "liquid" green, credit cards get the second gradient hue, bank/investment share the primary
 * accent (their icons already differ enough to stay distinguishable). */
fun tintForAccountType(type: String, colors: AppColors): Color = when (type) {
    ACCOUNT_TYPE_CASH -> colors.positive
    ACCOUNT_TYPE_CREDIT_CARD -> colors.accentSecondary
    else -> colors.accent
}
