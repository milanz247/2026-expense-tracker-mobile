package com.example.ui.storetabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CreditorResponse
import com.example.ui.common.AppFormSheet
import com.example.ui.common.AppListRow
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.formatMoney
import com.example.ui.theme.AppColors
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

internal val fieldColors: @Composable (AppColors) -> TextFieldColors = { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground, unfocusedTextColor = colors.onBackground,
        focusedBorderColor = colors.accent, unfocusedBorderColor = colors.outline,
        focusedLabelColor = colors.accent, unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreTabsScreen(
    viewModel: StoreTabsViewModel,
    onShopClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    LaunchedEffect(Unit) { viewModel.ensureListLoaded() }

    val stores by viewModel.stores.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showAddForm by viewModel.showAddShopForm.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Store Tabs", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Shop") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { viewModel.openAddShopForm() },
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
                if (errorMessage != null) {
                    item { ErrorBanner(errorMessage!!) }
                }

                if (isLoading && stores.isEmpty()) {
                    item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
                } else if (stores.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            EmptyState("No store tabs yet. Add a shop to start a running tab.")
                            Button(
                                onClick = { viewModel.openAddShopForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Shop")
                            }
                        }
                    }
                } else {
                    items(stores, key = { it.id }) { store ->
                        ShopCard(store, currency, onClick = { onShopClick(store.id) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }

    if (showAddForm) {
        AddShopSheet(viewModel)
    }
}

@Composable
private fun ShopCard(store: CreditorResponse, currency: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    AppListRow(
        leadingIcon = Icons.Default.Store,
        leadingTint = colors.textSecondary,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "OWED", fontSize = 9.sp, color = colors.textMuted, letterSpacing = 1.sp)
                Text(text = formatMoney(store.outstandingDebt, currency), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, fontFamily = GeistMono)
            }
        }
    ) {
        Text(text = store.name, fontSize = 15.sp, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddShopSheet(viewModel: StoreTabsViewModel) {
    val colors = LocalAppColors.current
    val name by viewModel.formShopName.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    AppFormSheet(
        onDismiss = { viewModel.dismissAddShopForm() },
        title = "Add Shop",
        confirmLabel = "Add",
        onConfirm = { viewModel.submitAddShopForm() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onShopNameChanged,
            label = { Text("Shop Name") },
            singleLine = true,
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
