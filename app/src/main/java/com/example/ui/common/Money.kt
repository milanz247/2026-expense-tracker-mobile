package com.example.ui.common

import java.util.Locale

/** Backend wire amounts are always integer cents; UI always deals in major units. */
fun Long.centsToMajor(): Double = this / 100.0

fun Double.majorToCents(): Long = Math.round(this * 100.0)

fun formatMoney(cents: Long, currency: String, signed: Boolean = false): String {
    val amount = cents.centsToMajor()
    // A genuinely negative amount (e.g. a credit-card wallet's balance, or a negative net
    // balance) must always show its "-" — dropping it would silently present debt as a
    // positive figure. `signed` only controls whether a *positive* amount also gets a "+".
    val sign = when {
        amount < 0 -> "-"
        signed && amount > 0 -> "+"
        else -> ""
    }
    return "$sign$currency ${String.format(Locale.US, "%,.2f", Math.abs(amount))}"
}

fun formatMoneyCompact(cents: Long, currency: String): String {
    val amount = cents.centsToMajor()
    return "$currency ${String.format(Locale.US, "%,.0f", amount)}"
}
