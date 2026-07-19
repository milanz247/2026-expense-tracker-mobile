package com.example.ui.storetabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CreditorResponse
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.formatMoney
import com.example.ui.theme.AppColors
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors

internal val fieldColors: @Composable (AppColors) -> TextFieldColors = { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground, unfocusedTextColor = colors.onBackground,
        focusedBorderColor = colors.accent, unfocusedBorderColor = colors.outline,
        focusedLabelColor = colors.accent, unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
}

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

    Box(modifier = modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Store Tabs", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
                    IconButton(onClick = { viewModel.openAddShopForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add shop", tint = colors.accent)
                    }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && stores.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else if (stores.isEmpty()) {
                item { EmptyState("No store tabs yet. Tap + to add a shop.") }
            } else {
                items(stores, key = { it.id }) { store ->
                    ShopCard(store, currency, onClick = { onShopClick(store.id) })
                }
            }
        }
    }

    if (showAddForm) {
        AddShopDialog(viewModel)
    }
}

@Composable
private fun ShopCard(store: CreditorResponse, currency: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
            Text(text = store.name, fontSize = 15.sp, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "OWED", fontSize = 9.sp, color = colors.textMuted, letterSpacing = 1.sp)
            Text(text = formatMoney(store.outstandingDebt, currency), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onBackground, fontFamily = GeistMono)
        }
    }
}

@Composable
private fun AddShopDialog(viewModel: StoreTabsViewModel) {
    val colors = LocalAppColors.current
    val name by viewModel.formShopName.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissAddShopForm() },
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.textSecondary,
        title = { Text("Add Shop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onShopNameChanged,
                    label = { Text("Shop Name") },
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
            TextButton(onClick = { viewModel.submitAddShopForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                } else {
                    Text("Add", color = colors.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissAddShopForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}
