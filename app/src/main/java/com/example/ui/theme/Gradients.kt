package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradients are for non-interactive, hero-level surfaces only — a Dashboard balance card, a
 * brand mark. Never apply one to a `Button`, FAB, or any confirm action: a gradient fill on an
 * actionable control reads as decorative rather than pressable and works against Material's
 * affordance conventions. Interactive controls stay on flat [AppColors.accent] everywhere.
 */
fun AppColors.heroGradient(): Brush =
    Brush.linearGradient(listOf(accent, accentSecondary))

/** Softer variant for smaller decorative surfaces (e.g. a wallet chip backdrop) where the full
 * [heroGradient] would be too loud. */
fun AppColors.subtleAccentWash(): Brush =
    Brush.verticalGradient(listOf(accent.copy(alpha = 0.10f), Color.Transparent))
