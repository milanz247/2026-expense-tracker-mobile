package com.example.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.common.AppFormSheet
import com.example.ui.common.AppListRow
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
import com.example.ui.theme.Spacing

private val fieldColors: @Composable (AppColors) -> TextFieldColors = { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground, unfocusedTextColor = colors.onBackground,
        focusedBorderColor = colors.accent, unfocusedBorderColor = colors.outline,
        focusedLabelColor = colors.accent, unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = fixedAccount?.name ?: "Transactions", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { viewModel.openAddForm() },
                containerColor = colors.accent,
                contentColor = colors.onAccent
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).windowInsetsPadding(WindowInsets.safeDrawing)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(top = Spacing.lg, bottom = 96.dp)
            ) {
                fixedAccount?.let { account ->
                    item {
                        MatteCard(cornerRadius = 28, contentPadding = PaddingValues(Spacing.xxl)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "BALANCE", fontSize = 10.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
                                Text(
                                    text = formatMoney(account.balance, currency),
                                    fontSize = 30.sp,
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
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            EmptyState("No transactions yet.")
                            Button(
                                onClick = { viewModel.openAddForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Transaction")
                            }
                        }
                    }
                } else {
                    items(transactions, key = { it.id }) { txn ->
                        TransactionRow(txn, categoryById[txn.categoryId], currency, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }

    if (showForm) {
        AddTransactionSheet(viewModel)
    }
}

@Composable
private fun TransactionRow(txn: TransactionResponse, category: CategoryResponse?, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isExpense = txn.type == TXN_TYPE_EXPENSE
    val isTransfer = txn.type == TXN_TYPE_TRANSFER
    val tint = category?.let { parseHexColor(categoryColorHex(it.color)) } ?: colors.textMuted

    AppListRow(
        leadingIcon = if (isTransfer) Icons.Default.SwapHoriz else iconForCategory(category?.icon ?: "Tag"),
        leadingTint = tint,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(viewModel: TransactionsViewModel) {
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

    val typeOptions = listOf(TXN_TYPE_EXPENSE, TXN_TYPE_INCOME, TXN_TYPE_TRANSFER)

    AppFormSheet(
        onDismiss = { viewModel.dismissForm() },
        title = "Add Transaction",
        confirmLabel = "Save",
        onConfirm = { viewModel.submitForm() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            typeOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = typeOptions.size),
                    onClick = { viewModel.onTypeSelected(option) },
                    selected = type == option,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colors.accent,
                        activeContentColor = colors.onAccent,
                        inactiveContainerColor = colors.surfaceVariant,
                        inactiveContentColor = colors.textSecondary
                    )
                ) { Text(option.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onAmountChanged,
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fee,
            onValueChange = viewModel::onFeeChanged,
            label = { Text("Fee (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
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
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
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
                .clip(MaterialTheme.shapes.medium)
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
