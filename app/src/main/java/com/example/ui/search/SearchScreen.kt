package com.example.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.TXN_TYPE_EXPENSE
import com.example.network.TXN_TYPE_TRANSFER
import com.example.network.TransactionResponse
import com.example.network.categoryColorHex
import com.example.ui.common.AppListRow
import com.example.ui.common.EmptyState
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
import com.example.ui.common.iconForCategory
import com.example.ui.common.parseHexColor
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing
import com.example.ui.transactions.TransactionsViewModel

/**
 * Client-side search over the transactions [TransactionsViewModel] has already fetched via
 * `listTransactions()` — there's no server-side search endpoint, so this filters in memory by
 * description or category name. Honest about that scope: it won't find anything outside what's
 * already loaded on the device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: TransactionsViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    LaunchedEffect(Unit) { viewModel.initialize(null) }

    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val categoryById = remember(categories) { categories.associateBy { it.id } }

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val results = remember(transactions, categoryById, query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            val needle = query.trim().lowercase()
            transactions.filter { txn ->
                txn.description.lowercase().contains(needle) ||
                    (categoryById[txn.categoryId]?.name?.lowercase()?.contains(needle) == true)
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search transactions…") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.accent
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).windowInsetsPadding(WindowInsets.safeDrawing)) {
            if (query.isBlank()) {
                Box(modifier = Modifier.fillMaxSize().padding(Spacing.xxxl), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(40.dp))
                        Text("Search by description or category", color = colors.textMuted, fontSize = 13.sp)
                    }
                }
            } else if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(Spacing.xl), contentAlignment = Alignment.TopCenter) {
                    EmptyState("No transactions match \"$query\".")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    contentPadding = PaddingValues(top = Spacing.lg, bottom = 96.dp)
                ) {
                    items(results, key = { it.id }) { txn ->
                        SearchResultRow(txn, categoryById[txn.categoryId]?.let { parseHexColor(categoryColorHex(it.color)) }, categoryById[txn.categoryId]?.icon, currency, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(txn: TransactionResponse, categoryTint: Color?, categoryIcon: String?, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isExpense = txn.type == TXN_TYPE_EXPENSE
    val isTransfer = txn.type == TXN_TYPE_TRANSFER

    AppListRow(
        leadingIcon = if (isTransfer) Icons.Default.SwapHoriz else categoryIcon?.let { iconForCategory(it) } ?: Icons.Default.Search,
        leadingTint = categoryTint ?: colors.textMuted,
        modifier = modifier,
        trailing = {
            Text(
                text = (if (isExpense) "-" else if (isTransfer) "" else "+") + formatMoney(txn.amount, currency),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isTransfer -> colors.onBackground
                    isExpense -> colors.negative
                    else -> colors.positive
                },
                fontFamily = GeistMono
            )
        }
    ) {
        Text(text = txn.description.ifBlank { txn.type.replaceFirstChar { it.uppercase() } }, fontSize = 14.sp, color = colors.onBackground)
        Text(text = formatDisplayDate(txn.date), fontSize = 11.sp, color = colors.textMuted)
    }
}
