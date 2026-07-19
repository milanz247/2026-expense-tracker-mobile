package com.example.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

/**
 * Shared full-width bottom-sheet form scaffold — replaces the AlertDialog-crammed-with-fields
 * pattern every "Add X" flow in this app used to hand-roll individually. Title + arbitrary field
 * content + inline error + a single full-width confirm action; dismissing (swipe/scrim) is
 * disabled while [isSubmitting] so an in-flight request can't be abandoned mid-air.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFormSheet(
    onDismiss: () -> Unit,
    title: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    isSubmitting: Boolean,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = colors.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)

            content()

            if (errorMessage != null) {
                Text(text = errorMessage, color = colors.error, fontSize = 12.sp)
            }

            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                    disabledContainerColor = colors.surfaceVariant,
                    disabledContentColor = colors.textMuted
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onAccent)
                } else {
                    Text(confirmLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
