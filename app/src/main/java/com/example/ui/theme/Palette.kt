package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * App-wide design tokens, mirroring the web frontend's own theme exactly:
 * neutral zinc surfaces in both modes, with red reserved for the accent/
 * danger/expense role instead of tinting every surface (see globals.css —
 * the web app's dark mode is chroma-0 neutral gray, red only shows up as
 * --destructive). Two full instances (dark/light) are provided through
 * LocalAppColors so every screen picks up the active theme automatically.
 */
data class AppColors(
    val bg: Color,
    val surface: Color,
    val dialogSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentMuted: Color,
    val onAccentMuted: Color,
    val danger: Color,
    val success: Color,
    val border: Color,
)

// zinc-950 / zinc-900 / zinc-800 background stack, red-600 accent,
// emerald-600 for income — the exact hex the web app's cash-flow-chart
// already uses for the same semantics, so both surfaces read as one brand.
val DarkAppColors = AppColors(
    bg = Color(0xFF09090B),
    surface = Color(0xFF18181B),
    dialogSurface = Color(0xFF1F1F23),
    textPrimary = Color(0xFFFAFAFA),
    textSecondary = Color(0xFFA1A1AA),
    accent = Color(0xFFDC2626),
    accentMuted = Color(0xFF450A0A),
    onAccentMuted = Color(0xFFFCA5A5),
    danger = Color(0xFFDC2626),
    success = Color(0xFF10B981),
    border = Color(0xFF27272A),
)

val LightAppColors = AppColors(
    bg = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    dialogSurface = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF18181B),
    textSecondary = Color(0xFF71717A),
    accent = Color(0xFFDC2626),
    accentMuted = Color(0xFFFEE2E2),
    onAccentMuted = Color(0xFFB91C1C),
    danger = Color(0xFFDC2626),
    success = Color(0xFF059669),
    border = Color(0xFFE4E4E7),
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
