package com.example.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import com.example.network.DEBT_STATUS_SETTLED
import com.example.network.DEBT_TYPE_BORROWED
import com.example.network.DEBT_TYPE_LENT
import com.example.network.DebtResponse
import com.example.ui.common.AppFormSheet
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
import com.example.ui.theme.AppColors
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing
import kotlin.math.roundToInt

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
fun DebtsScreen(viewModel: DebtsViewModel, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val debts by viewModel.debts.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showAddForm by viewModel.showAddForm.collectAsState()
    val repayingDebt by viewModel.repayingDebt.collectAsState()

    val visible = remember(debts, selectedType) { debts.filter { it.type == selectedType } }
    val typeOptions = listOf(DEBT_TYPE_LENT, DEBT_TYPE_BORROWED)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Debts", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Debt") },
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
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        typeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = typeOptions.size),
                                onClick = { viewModel.selectType(option) },
                                selected = selectedType == option,
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = colors.accent,
                                    activeContentColor = colors.onAccent,
                                    inactiveContainerColor = colors.surfaceVariant,
                                    inactiveContentColor = colors.textSecondary
                                )
                            ) { Text(option.replaceFirstChar { it.uppercase() }) }
                        }
                    }
                }

                if (errorMessage != null) {
                    item { ErrorBanner(errorMessage!!) }
                }

                if (isLoading && debts.isEmpty()) {
                    item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
                } else if (visible.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            EmptyState("No ${selectedType} debts yet.")
                            Button(
                                onClick = { viewModel.openAddForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Debt")
                            }
                        }
                    }
                } else {
                    items(visible, key = { it.id }) { debt ->
                        DebtCard(debt, currency, onRepay = { viewModel.openRepayForm(debt) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }

    if (showAddForm) {
        AddDebtSheet(viewModel)
    }

    repayingDebt?.let {
        RepayDebtSheet(viewModel, currency)
    }
}

@Composable
private fun DebtCard(debt: DebtResponse, currency: String, onRepay: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isSettled = debt.status == DEBT_STATUS_SETTLED
    val isLent = debt.type == DEBT_TYPE_LENT
    val progress = if (debt.totalAmount == 0L) 1f else 1f - (debt.remainingAmount.toFloat() / debt.totalAmount.toFloat())
    val directionTint = if (isLent) colors.positive else colors.negative

    MatteCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(directionTint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (isLent) "Lent (receivable)" else "Borrowed (payable)",
                            tint = directionTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(text = debt.personName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Due ${formatDisplayDate(debt.dueDate)}", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSettled) colors.positive.copy(alpha = 0.16f) else colors.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isSettled) "SETTLED" else "PENDING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSettled) colors.positive else colors.textSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = directionTint,
                    trackColor = colors.outline
                )
                Text(
                    text = "${(progress.coerceIn(0f, 1f) * 100).roundToInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    fontFamily = GeistMono
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Remaining", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = formatMoney(debt.remainingAmount, currency), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, fontFamily = GeistMono)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total", fontSize = 10.sp, color = colors.textMuted)
                    Text(text = formatMoney(debt.totalAmount, currency), fontSize = 13.sp, color = colors.textSecondary, fontFamily = GeistMono)
                }
            }

            if (!isSettled) {
                Button(
                    onClick = onRepay,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Record Repayment", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtSheet(viewModel: DebtsViewModel) {
    val colors = LocalAppColors.current
    val personName by viewModel.formPersonName.collectAsState()
    val totalAmount by viewModel.formTotalAmount.collectAsState()
    val accountId by viewModel.formAccountId.collectAsState()
    val dueDateMillis by viewModel.formDueDateMillis.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    var accountMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AppFormSheet(
        onDismiss = { viewModel.dismissAddForm() },
        title = if (selectedType == DEBT_TYPE_LENT) "Money Lent" else "Money Borrowed",
        confirmLabel = "Save",
        onConfirm = { viewModel.submitAddForm() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        OutlinedTextField(
            value = personName,
            onValueChange = viewModel::onPersonNameChanged,
            label = { Text("Person") },
            singleLine = true,
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = totalAmount,
            onValueChange = viewModel::onTotalAmountChanged,
            label = { Text("Amount") },
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
                label = { Text("Wallet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                colors = fieldColors(colors),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = accountMenuExpanded, onDismissRequest = { accountMenuExpanded = false }, containerColor = colors.surfaceVariant) {
                accounts.forEach { acc ->
                    DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onFormAccountSelected(acc.id); accountMenuExpanded = false })
                }
            }
        }

        Column {
            Text(text = "Due Date", fontSize = 12.sp, color = colors.textMuted)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(colors.surfaceVariant)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(text = com.example.ui.common.epochMillisToIso8601(dueDateMillis).take(10), color = colors.onBackground, fontSize = 14.sp)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDueDateSelected(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepayDebtSheet(viewModel: DebtsViewModel, currency: String) {
    val colors = LocalAppColors.current
    val debt by viewModel.repayingDebt.collectAsState()
    val amount by viewModel.repayAmount.collectAsState()
    val accountId by viewModel.repayAccountId.collectAsState()
    val error by viewModel.repayError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val currentDebt = debt ?: return

    AppFormSheet(
        onDismiss = { viewModel.dismissRepayForm() },
        title = "Repay ${currentDebt.personName}",
        confirmLabel = "Confirm",
        onConfirm = { viewModel.submitRepayForm() },
        isSubmitting = isSubmitting,
        errorMessage = error
    ) {
        Text(text = "Remaining: ${formatMoney(currentDebt.remainingAmount, currency)}", fontSize = 13.sp, color = colors.textSecondary)
        OutlinedTextField(
            value = amount,
            onValueChange = viewModel::onRepayAmountChanged,
            label = { Text("Repayment Amount") },
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
                    DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onRepayAccountSelected(acc.id); accountMenuExpanded = false })
                }
            }
        }
    }
}
