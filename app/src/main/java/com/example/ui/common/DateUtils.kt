package com.example.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Uses java.text (not java.time) since minSdk 24 is below java.time's API 26 floor
// and this project doesn't enable core library desugaring.
private fun iso8601Format() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

fun nowIso8601(): String = iso8601Format().format(Date())

fun epochMillisToIso8601(millis: Long): String = iso8601Format().format(Date(millis))

/** Cheap, parser-free display: an RFC3339 timestamp always starts with `YYYY-MM-DD`. */
fun formatDisplayDate(iso: String): String = iso.take(10)

/**
 * Best-effort "Today" / "Yesterday" / "This Week" / "Earlier" bucket for grouping list items —
 * a presentation-only grouping, not a precise calendar computation (doesn't special-case the
 * Dec 31 → Jan 1 boundary), which is fine for a UI section header.
 */
fun relativeDayLabel(iso: String): String {
    val date = runCatching { iso8601Format().parse(iso) }.getOrNull() ?: return formatDisplayDate(iso)
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }
    if (now.get(Calendar.YEAR) != target.get(Calendar.YEAR)) return "Earlier"
    val dayDelta = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)
    return when {
        dayDelta == 0 -> "Today"
        dayDelta == 1 -> "Yesterday"
        dayDelta in 2..6 -> "This Week"
        else -> "Earlier"
    }
}
