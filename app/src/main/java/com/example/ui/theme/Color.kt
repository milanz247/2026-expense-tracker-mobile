package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val PitchBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)

val Zinc100 = Color(0xFFF4F4F5)
val Zinc200 = Color(0xFFE4E4E7)
val Zinc300 = Color(0xFFD4D4D8)
val Zinc400 = Color(0xFFA1A1AA)
val Zinc500 = Color(0xFF71717A)
val Zinc600 = Color(0xFF52525B)
val Zinc700 = Color(0xFF3F3F46)
val Zinc800 = Color(0xFF3F3F46)
val Zinc900 = Color(0xFF222226)
val Zinc950 = Color(0xFF17171A)

/**
 * Semantic palette every screen reads from via [LocalAppColors]. Screens never branch on
 * light/dark themselves — they name a role (background, accent, textMuted, ...) and get the
 * right value for whichever theme is active.
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
    val positive: Color,
    val negative: Color,
    val error: Color,
    /** A tier above [surface] for chrome that should read as genuinely floating above the base
     * layer — the pill nav bar, sheet surfaces — as opposed to just another card. */
    val surfaceElevated: Color,
    /** Second gradient stop paired with [accent], reserved for hero-only surfaces (see
     * [heroGradient]) — never used on an interactive control. */
    val accentSecondary: Color,
)

// Pixel-style dark theme: true-black AMOLED background, soft (not pure) white text, and
// Google's own "Blue 300" accent — the same hue Android's system UI uses for toggles/links
// on dark surfaces.
val DarkAppColors = AppColors(
    background = PitchBlack,
    surface = Zinc950,
    surfaceVariant = Zinc900,
    onBackground = Color(0xFFF2F2F5),
    textSecondary = Zinc400,
    textMuted = Zinc500,
    outline = Zinc800,
    accent = Color(0xFF8AB4F8),
    onAccent = Color(0xFF0A2A55),
    positive = Color(0xFF4ADE80),
    negative = Color(0xFFF87171),
    error = Color(0xFFF87171),
    surfaceElevated = Color(0xFF2A2A30),
    accentSecondary = Color(0xFF5B8DEF),
)

// Pixel-style light theme: soft off-white background (not stark white) with elevated white
// cards, and Google's "Blue 600" — the light-surface counterpart of the dark accent above.
val LightAppColors = AppColors(
    background = Color(0xFFFAFAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F1F4),
    onBackground = Color(0xFF1C1C1F),
    textSecondary = Zinc600,
    textMuted = Zinc500,
    outline = Zinc200,
    accent = Color(0xFF1A73E8),
    onAccent = Color(0xFFFFFFFF),
    positive = Color(0xFF16A34A),
    negative = Color(0xFFDC2626),
    error = Color(0xFFDC2626),
    surfaceElevated = Color(0xFFFFFFFF),
    accentSecondary = Color(0xFF0B57D0),
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
