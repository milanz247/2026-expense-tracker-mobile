package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private fun colorSchemeFor(colors: AppColors, dark: Boolean) =
    if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            primaryContainer = colors.accentMuted,
            onPrimaryContainer = colors.onAccentMuted,
            secondary = colors.accent,
            secondaryContainer = colors.accentMuted,
            onSecondaryContainer = colors.onAccentMuted,
            background = colors.bg,
            surface = colors.bg,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.textSecondary,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.danger,
            outline = colors.border,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            primaryContainer = colors.accentMuted,
            onPrimaryContainer = colors.onAccentMuted,
            secondary = colors.accent,
            secondaryContainer = colors.accentMuted,
            onSecondaryContainer = colors.onAccentMuted,
            background = colors.bg,
            surface = colors.bg,
            surfaceVariant = colors.surface,
            onSurfaceVariant = colors.textSecondary,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            error = colors.danger,
            outline = colors.border,
        )
    }

/**
 * The app now follows a real light/dark toggle (see FinanceViewModel's
 * darkTheme state) instead of a fixed always-dark design. Both palettes
 * mirror the web frontend's own theme — neutral zinc surfaces with red
 * reserved for the accent/expense/danger role — so switching themes never
 * changes the brand, just which mode it renders in.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = colorSchemeFor(appColors, darkTheme), typography = Typography, content = content)
    }
}
