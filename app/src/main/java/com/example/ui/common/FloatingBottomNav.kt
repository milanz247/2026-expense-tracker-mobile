package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

data class FloatingNavItem(val route: String, val label: String, val icon: ImageVector)

/**
 * A size-to-content pill nav bar (Cash App / Google Pay style) instead of a full-width Material
 * [androidx.compose.material3.NavigationBar]. Deliberately custom rather than a wrapped stock
 * NavigationBar — that component is built for equally-weighted full-width items and always
 * paints its own full-width background, which fights a "hugs its content" pill shape.
 *
 * Kept decoupled from NavController — [items] carry an opaque [FloatingNavItem.route] string, so
 * this stays a generic, reusable piece of UI rather than a nav-graph-aware component.
 */
@Composable
fun FloatingBottomNavBar(
    items: List<FloatingNavItem>,
    selectedRoute: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    // enableEdgeToEdge() is active app-wide, so unlike stock NavigationBar (which insets itself
    // internally) this custom Surface gets no automatic system-bar inset handling — apply it
    // explicitly here, before the bar's own decorative margin, or the pill collides with the
    // 3-button nav bar on devices that have one.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = colors.surfaceElevated,
            shadowElevation = 12.dp,
            tonalElevation = 2.dp,
            modifier = Modifier.wrapContentWidth().height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .selectableGroup()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.forEach { item ->
                    NavPillItem(
                        item = item,
                        selected = item.route == selectedRoute,
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavPillItem(item: FloatingNavItem, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val iconTint by animateColorAsState(if (selected) colors.accent else colors.textMuted, label = "nav_icon_tint")
    val iconScale by animateFloatAsState(if (selected) 1.1f else 1f, label = "nav_icon_scale")

    Box(
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .animateContentSize(spring())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = item.icon,
                contentDescription = if (selected) null else item.label,
                tint = iconTint,
                modifier = Modifier.height(22.dp).scale(iconScale)
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = item.label,
                    color = colors.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
