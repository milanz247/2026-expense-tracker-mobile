package com.example.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

/**
 * There is no notification source on the backend yet (no push service, no in-app notification
 * feed endpoint) — this is an honest empty-state shell for where that feed will render once one
 * exists, not a fake list of made-up notifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Notifications", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(Spacing.xxxl),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(48.dp))
                Text(text = "You're all caught up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.onBackground, textAlign = TextAlign.Center)
                Text(
                    text = "Reminders for due debts and upcoming bills will show up here.",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
