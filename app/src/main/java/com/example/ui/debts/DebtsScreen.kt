package com.example.ui.debts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.DEBT_STATUS_SETTLED
import com.example.network.DEBT_TYPE_BORROWED
import com.example.network.DEBT_TYPE_LENT
import com.example.network.DebtResponse
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
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

    Box(modifier = modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Debts", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                    IconButton(onClick = { viewModel.openAddForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add debt", tint = colors.accent)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip("Lent", selectedType == DEBT_TYPE_LENT) { viewModel.selectType(DEBT_TYPE_LENT) }
                    TypeChip("Borrowed", selectedType == DEBT_TYPE_BORROWED) { viewModel.selectType(DEBT_TYPE_BORROWED) }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && debts.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else if (visible.isEmpty()) {
                item { EmptyState("No ${selectedType} debts yet.") }
            } else {
                items(visible, key = { it.id }) { debt ->
                    DebtCard(debt, currency, onRepay = { viewModel.openRepayForm(debt) })
                }
            }
        }
    }

    if (showAddForm) {
        AddDebtDialog(viewModel)
    }

    repayingDebt?.let {
        RepayDebtDialog(viewModel, currency)
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) colors.accent else colors.surfaceVariant)
            .border(1.dp, if (selected) Color.Transparent else colors.outline, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (selected) colors.onAccent else colors.textSecondary)
    }
}

@Composable
private fun DebtCard(debt: DebtResponse, currency: String, onRepay: () -> Unit) {
    val colors = LocalAppColors.current
    val isSettled = debt.status == DEBT_STATUS_SETTLED
    val progress = if (debt.totalAmount == 0L) 1f else 1f - (debt.remainingAmount.toFloat() / debt.totalAmount.toFloat())

    MatteCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(text = debt.personName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Due ${formatDisplayDate(debt.dueDate)}", fontSize = 11.sp, color = colors.textMuted)
                }
                Text(
                    text = if (isSettled) "SETTLED" else "PENDING",
                    fontSize = 10.sp,
                    color = if (isSettled) colors.positive else colors.textMuted,
                    letterSpacing = 1.sp
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = colors.accent,
                trackColor = colors.outline
            )

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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Record Repayment", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtDialog(viewModel: DebtsViewModel) {
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

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissAddForm() },
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.textSecondary,
        title = { Text(if (selectedType == DEBT_TYPE_LENT) "Money Lent" else "Money Borrowed") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = viewModel::onPersonNameChanged,
                    label = { Text("Person") },
                    singleLine = true,
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = viewModel::onTotalAmountChanged,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
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
                        shape = RoundedCornerShape(12.dp),
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(text = com.example.ui.common.epochMillisToIso8601(dueDateMillis).take(10), color = colors.onBackground, fontSize = 14.sp)
                    }
                }

                if (formError != null) {
                    Text(text = formError!!, color = colors.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitAddForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                } else {
                    Text("Save", color = colors.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissAddForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )

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

@Composable
private fun RepayDebtDialog(viewModel: DebtsViewModel, currency: String) {
    val colors = LocalAppColors.current
    val debt by viewModel.repayingDebt.collectAsState()
    val amount by viewModel.repayAmount.collectAsState()
    val accountId by viewModel.repayAccountId.collectAsState()
    val error by viewModel.repayError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val currentDebt = debt ?: return

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissRepayForm() },
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.textSecondary,
        title = { Text("Repay ${currentDebt.personName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "Remaining: ${formatMoney(currentDebt.remainingAmount, currency)}", fontSize = 13.sp, color = colors.textSecondary)
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::onRepayAmountChanged,
                    label = { Text("Repayment Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(colors),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBoxWrapper(accounts.find { it.id == accountId }?.name ?: "Select", accountMenuExpanded, { accountMenuExpanded = it }) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(text = { Text(acc.name, color = colors.onBackground) }, onClick = { viewModel.onRepayAccountSelected(acc.id); accountMenuExpanded = false })
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = colors.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitRepayForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                } else {
                    Text("Confirm", color = colors.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissRepayForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxWrapper(
    selectedLabel: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menuItems: @Composable () -> Unit
) {
    val colors = LocalAppColors.current
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Wallet") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = fieldColors(colors),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, containerColor = colors.surfaceVariant) {
            menuItems()
        }
    }
}
