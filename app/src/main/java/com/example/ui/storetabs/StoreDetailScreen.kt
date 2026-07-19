package com.example.ui.storetabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.PurchaseResponse
import com.example.network.SettlementResponse
import com.example.network.categoryColorHex
import com.example.ui.common.AppFormSheet
import com.example.ui.common.AppListRow
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
import com.example.ui.common.iconForCategory
import com.example.ui.common.parseHexColor
import com.example.ui.common.relativeDayLabel
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    viewModel: StoreTabsViewModel,
    creditorId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    LaunchedEffect(creditorId) { viewModel.ensureDetailLoaded(creditorId) }

    val creditor by viewModel.detailCreditor.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showPurchaseForm by viewModel.showPurchaseForm.collectAsState()
    val showSettlementForm by viewModel.showSettlementForm.collectAsState()

    val categoryById = remember(categories) { categories.associateBy { it.id } }
    val groupedPurchases = remember(purchases) { purchases.groupBy { relativeDayLabel(it.date) } }
    val groupedSettlements = remember(settlements) { settlements.groupBy { relativeDayLabel(it.date) } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = creditor?.name ?: "Shop", fontWeight = FontWeight.Bold, color = colors.onBackground) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                contentPadding = PaddingValues(top = Spacing.lg, bottom = 96.dp)
            ) {
                creditor?.let {
                    item {
                        MatteCard(cornerRadius = 28, contentPadding = PaddingValues(Spacing.xxl)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "OUTSTANDING", fontSize = 10.sp, color = colors.textMuted, letterSpacing = 1.sp, fontFamily = GeistMono)
                                Text(text = formatMoney(it.outstandingDebt, currency), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, fontFamily = GeistMono)
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Button(
                            onClick = { viewModel.openPurchaseForm() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant, contentColor = colors.onBackground),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(1f)
                        ) { Text("Record Purchase", fontSize = 12.sp) }
                        Button(
                            onClick = { viewModel.openSettlementForm() },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(1f)
                        ) { Text("Settle Up", fontSize = 12.sp) }
                    }
                }

                if (errorMessage != null) {
                    item { ErrorBanner(errorMessage!!) }
                }

                if (isLoading && purchases.isEmpty() && settlements.isEmpty()) {
                    item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(160.dp)) }
                } else {
                    item { SectionLabel("Purchases") }
                    if (purchases.isEmpty()) {
                        item { EmptyState("No purchases recorded yet.") }
                    } else {
                        groupedPurchases.forEach { (label, group) ->
                            item { Text(text = label, fontSize = 11.sp, color = colors.textMuted) }
                            items(group, key = { "p${it.id}" }) { purchase ->
                                PurchaseRow(purchase, categoryById[purchase.categoryId]?.let { parseHexColor(categoryColorHex(it.color)) }, categoryById[purchase.categoryId]?.icon, currency, modifier = Modifier.animateItem())
                            }
                        }
                    }

                    item { SectionLabel("Settlements") }
                    if (settlements.isEmpty()) {
                        item { EmptyState("No settlements recorded yet.") }
                    } else {
                        groupedSettlements.forEach { (label, group) ->
                            item { Text(text = label, fontSize = 11.sp, color = colors.textMuted) }
                            items(group, key = { "s${it.id}" }) { settlement ->
                                SettlementRow(settlement, currency, modifier = Modifier.animateItem())
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPurchaseForm) {
        RecordPurchaseSheet(viewModel)
    }
    if (showSettlementForm) {
        RecordSettlementSheet(viewModel, currency)
    }
}

@Composable
private fun PurchaseRow(purchase: PurchaseResponse, categoryTint: Color?, categoryIcon: String?, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    AppListRow(
        leadingIcon = categoryIcon?.let { iconForCategory(it) } ?: Icons.Default.ShoppingBag,
        leadingTint = categoryTint ?: colors.textMuted,
        modifier = modifier,
        trailing = {
            Text(text = "+${formatMoney(purchase.amount + purchase.fee, currency)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.negative, fontFamily = GeistMono)
        }
    ) {
        Text(text = purchase.description.ifBlank { "Purchase" }, fontSize = 13.sp, color = colors.onBackground)
        Text(text = formatDisplayDate(purchase.date), fontSize = 11.sp, color = colors.textMuted)
    }
}

@Composable
private fun SettlementRow(settlement: SettlementResponse, currency: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    AppListRow(
        leadingIcon = Icons.Default.Payments,
        leadingTint = colors.positive,
        modifier = modifier,
        trailing = {
            Text(text = "-${formatMoney(settlement.amountPaid, currency)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.positive, fontFamily = GeistMono)
        }
    ) {
        Text(text = "Settlement", fontSize = 13.sp, color = colors.onBackground)
        Text(text = formatDisplayDate(settlement.date), fontSize = 11.sp, color = colors.textMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPurchaseSheet(viewModel: StoreTabsViewModel) {
    val colors = LocalAppColors.current
    val amount by viewModel.purchaseAmount.collectAsState()
    val fee by viewModel.purchaseFee.collectAsState()
    val categoryId by viewModel.purchaseCategoryId.collectAsState()
    val description by viewModel.purchaseDescription.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    AppFormSheet(
        onDismiss = { viewModel.dismissPurchaseForm() },
        title = "Record Purchase",
        confirmLabel = "Save",
        onConfirm = { viewModel.submitPurchase() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onPurchaseAmountChanged,
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fee,
            onValueChange = viewModel::onPurchaseFeeChanged,
            label = { Text("Fee (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
            OutlinedTextField(
                value = categories.find { it.id == categoryId }?.name ?: "Select",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                colors = fieldColors(colors),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }, containerColor = colors.surfaceVariant) {
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name, color = colors.onBackground) }, onClick = { viewModel.onPurchaseCategorySelected(cat.id); categoryMenuExpanded = false })
                }
            }
        }
        OutlinedTextField(
            value = description,
            onValueChange = viewModel::onPurchaseDescriptionChanged,
            label = { Text("Description") },
            singleLine = true,
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSettlementSheet(viewModel: StoreTabsViewModel, currency: String) {
    val colors = LocalAppColors.current
    val amount by viewModel.settlementAmount.collectAsState()
    val fee by viewModel.settlementFee.collectAsState()
    val accountId by viewModel.settlementAccountId.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val creditor by viewModel.detailCreditor.collectAsState()
    var accountMenuExpanded by remember { mutableStateOf(false) }

    AppFormSheet(
        onDismiss = { viewModel.dismissSettlementForm() },
        title = "Settle Up",
        confirmLabel = "Confirm",
        onConfirm = { viewModel.submitSettlement() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        Text(text = "Outstanding: ${formatMoney(creditor?.outstandingDebt ?: 0L, currency)}", fontSize = 13.sp, color = colors.textSecondary)
        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onSettlementAmountChanged,
            label = { Text("Amount to Pay") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fee,
            onValueChange = viewModel::onSettlementFeeChanged,
            label = { Text("Fee (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(expanded = accountMenuExpanded, onExpandedChange = { accountMenuExpanded = it }) {
            OutlinedTextField(
                value = accounts.find { it.id == accountId }?.name ?: "Select",
                onValueChange = {},
                readOnly = true,
                label = { Text("Pay From") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                colors = fieldColors(colors),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = accountMenuExpanded, onDismissRequest = { accountMenuExpanded = false }, containerColor = colors.surfaceVariant) {
                accounts.forEach { acc ->
                    DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onSettlementAccountSelected(acc.id); accountMenuExpanded = false })
                }
            }
        }
    }
}
