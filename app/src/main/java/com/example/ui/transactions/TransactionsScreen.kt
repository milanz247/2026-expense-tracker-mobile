package com.example.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CategoryResponse
import com.example.network.TXN_TYPE_EXPENSE
import com.example.network.TXN_TYPE_INCOME
import com.example.network.TXN_TYPE_TRANSFER
import com.example.network.TransactionResponse
import com.example.network.categoryColorHex
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
import com.example.ui.common.iconForCategory
import com.example.ui.common.parseHexColor
import com.example.ui.theme.AppColors
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors

private val fieldColors: @Composable (AppColors) -> TextFieldColors = { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground, unfocusedTextColor = colors.onBackground,
        focusedBorderColor = colors.accent, unfocusedBorderColor = colors.outline,
        focusedLabelColor = colors.accent, unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
}

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    fixedAccountId: Long?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    LaunchedEffect(fixedAccountId) { viewModel.initialize(fixedAccountId) }

    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val fixedAccount by viewModel.fixedAccount.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showForm by viewModel.showForm.collectAsState()

    val categoryById = remember(categories) { categories.associateBy { it.id } }

    Box(modifier = modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                        }
                    }
                    Text(
                        text = fixedAccount?.name ?: "Transactions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.openAddForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add transaction", tint = colors.accent)
                    }
                }
            }

            fixedAccount?.let { account ->
                item {
                    MatteCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "BALANCE", fontSize = 10.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
                            Text(
                                text = formatMoney(account.balance, currency),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground,
                                fontFamily = GeistMono
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && transactions.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else if (transactions.isEmpty()) {
                item { EmptyState("No transactions yet. Tap + to add one.") }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    TransactionRow(txn, categoryById[txn.categoryId], currency)
                }
            }
        }
    }

    if (showForm) {
        AddTransactionDialog(viewModel)
    }
}

@Composable
private fun TransactionRow(txn: TransactionResponse, category: CategoryResponse?, currency: String) {
    val colors = LocalAppColors.current
    val isExpense = txn.type == TXN_TYPE_EXPENSE
    val isTransfer = txn.type == TXN_TYPE_TRANSFER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val tint = category?.let { parseHexColor(categoryColorHex(it.color)) } ?: colors.textMuted
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTransfer) Icons.Default.SwapHoriz else iconForCategory(category?.icon ?: "Tag"),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = txn.description.ifBlank { category?.name ?: txn.type.replaceFirstChar { it.uppercase() } },
                    fontSize = 14.sp,
                    color = colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = formatDisplayDate(txn.date), fontSize = 11.sp, color = colors.textMuted)
            }
        }
        Text(
            text = (if (isExpense) "-" else if (isTransfer) "" else "+") + formatMoney(txn.amount, currency),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) colors.textSecondary else colors.positive,
            fontFamily = GeistMono
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionDialog(viewModel: TransactionsViewModel) {
    val colors = LocalAppColors.current
    val type by viewModel.formType.collectAsState()
    val amount by viewModel.formAmount.collectAsState()
    val fee by viewModel.formFee.collectAsState()
    val accountId by viewModel.formAccountId.collectAsState()
    val sourceId by viewModel.formSourceAccountId.collectAsState()
    val destinationId by viewModel.formDestinationAccountId.collectAsState()
    val categoryId by viewModel.formCategoryId.collectAsState()
    val description by viewModel.formDescription.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissForm() },
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.textSecondary,
        title = { Text("Add Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TXN_TYPE_EXPENSE, TXN_TYPE_INCOME, TXN_TYPE_TRANSFER).forEach { option ->
                        val selected = type == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.accent else colors.surfaceVariant)
                                .clickable { viewModel.onTypeSelected(option) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = option.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                color = if (selected) colors.onAccent else colors.textSecondary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::onAmountChanged,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fee,
                    onValueChange = viewModel::onFeeChanged,
                    label = { Text("Fee (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == TXN_TYPE_TRANSFER) {
                    PickerField("From Wallet", accounts.find { it.id == sourceId }?.name ?: "Select") { _, dismiss ->
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onSourceAccountSelected(acc.id); dismiss() })
                        }
                    }
                    PickerField("To Wallet", accounts.find { it.id == destinationId }?.name ?: "Select") { _, dismiss ->
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onDestinationAccountSelected(acc.id); dismiss() })
                        }
                    }
                } else {
                    PickerField("Wallet", accounts.find { it.id == accountId }?.name ?: "Select") { _, dismiss ->
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onAccountSelected(acc.id); dismiss() })
                        }
                    }
                    val categoryOptions = viewModel.categoriesForFormType()
                    PickerField("Category", categoryOptions.find { it.id == categoryId }?.name ?: "Select") { _, dismiss ->
                        categoryOptions.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name, color = colors.onBackground) }, onClick = { viewModel.onCategorySelected(cat.id); dismiss() })
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Description") },
                    singleLine = true,
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (formError != null) {
                    Text(text = formError!!, color = colors.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                } else {
                    Text("Save", color = colors.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun PickerField(label: String, selectedLabel: String, menuContent: @Composable (expanded: Boolean, dismiss: () -> Unit) -> Unit) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = label, fontSize = 12.sp, color = colors.textMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(text = selectedLabel, color = colors.onBackground, fontSize = 14.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = colors.surfaceVariant) {
            menuContent(expanded) { expanded = false }
        }
    }
}
