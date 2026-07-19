package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAppColors

/**
 * The "icon-in-a-tinted-circle, title/subtitle column, trailing slot" card row shared by
 * Wallets, Transactions, Debts and Store Tabs — previously hand-rolled from scratch in each
 * of those four screens.
 */
@Composable
fun AppListRow(
    leadingIcon: ImageVector,
    leadingTint: Color = LocalAppColors.current.accent,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val clickableModifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surface)
            .border(1.dp, colors.outline, MaterialTheme.shapes.large)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(leadingTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f), content = content)
        }
        trailing?.invoke()
    }
}
