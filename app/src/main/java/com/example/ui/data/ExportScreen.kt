package com.example.ui.data

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.buildTransactionsCsv
import com.example.ui.common.nowIso8601
import com.example.ui.common.writeCsvToCache
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing
import com.example.ui.transactions.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: TransactionsViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.initialize(null) }

    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryById = remember(categories) { categories.associateBy { it.id } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Export & Backup", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SectionLabel("Export")
                MatteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                        Text(
                            text = "Download your ${transactions.size} transactions as a CSV file — opens in Sheets, Excel, or any spreadsheet app.",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Button(
                            onClick = {
                                val csv = buildTransactionsCsv(transactions, categoryById)
                                val uri = writeCsvToCache(context, "transactions_${nowIso8601().take(10)}.csv", csv)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export transactions"))
                            },
                            enabled = transactions.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.onAccent,
                                disabledContainerColor = colors.surfaceVariant,
                                disabledContentColor = colors.textMuted
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (transactions.isEmpty()) "Nothing to export yet" else "Export Transactions (CSV)")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SectionLabel("Cloud Backup")
                MatteCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Full account backup & restore isn't available yet — it needs a backend endpoint that doesn't exist. Your wallets, transactions, and categories already live safely on the server tied to your account.",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}
