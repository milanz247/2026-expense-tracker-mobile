package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NoirColorScheme =
  darkColorScheme(
    primary = NoirPrimary,
    onPrimary = Color.White,
    primaryContainer = NoirPrimaryContainer,
    onPrimaryContainer = NoirOnPrimaryContainer,
    secondary = NoirPrimary,
    // A *selected* chip/option must read as clearly different from an
    // unselected one. Leaving this at the same tone as the normal surface
    // (the earlier bug) made selection state on filter chips — e.g. the
    // lent/borrowed choice in the debt dialog — invisible.
    secondaryContainer = NoirPrimaryContainer,
    onSecondaryContainer = NoirOnPrimaryContainer,
    background = NoirBg,
    surface = NoirBg,
    surfaceVariant = NoirSurface,
    onSurfaceVariant = NoirTextSecondary,
    onBackground = NoirText,
    onSurface = NoirText,
    error = Color(0xFFFF4655),
    outline = NoirBorder,
  )

/**
 * The app is deliberately always dark with a red (crimson) accent — this is
 * a fixed design choice, not something that follows the system theme.
 */
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = NoirColorScheme, typography = Typography, content = content)
}
