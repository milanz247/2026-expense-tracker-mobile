package com.example.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.CATEGORY_COLORS
import com.example.network.CATEGORY_ICONS
import com.example.network.CATEGORY_TYPE_EXPENSE
import com.example.network.CATEGORY_TYPE_INCOME
import com.example.network.categoryColorHex
import com.example.ui.common.AppFormSheet
import com.example.ui.common.AppListRow
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.iconForCategory
import com.example.ui.common.parseHexColor
import com.example.ui.theme.AppColors
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
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val selectedType by viewModel.selectedType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showForm by viewModel.showForm.collectAsState()
    val pendingDelete by viewModel.categoryPendingDelete.collectAsState()

    val visibleCategories = remember(categories, selectedType) { categories.filter { it.type == selectedType } }
    val typeOptions = listOf(CATEGORY_TYPE_EXPENSE, CATEGORY_TYPE_INCOME)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Categories", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Category") },
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

                if (isLoading && categories.isEmpty()) {
                    item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
                } else if (visibleCategories.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            EmptyState("No categories yet.")
                            Button(
                                onClick = { viewModel.openAddForm() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Category")
                            }
                        }
                    }
                } else {
                    items(visibleCategories, key = { it.id }) { category ->
                        AppListRow(
                            leadingIcon = iconForCategory(category.icon),
                            leadingTint = parseHexColor(categoryColorHex(category.color)),
                            modifier = Modifier.animateItem(),
                            trailing = {
                                if (!category.isSystem) {
                                    var menuExpanded by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { menuExpanded = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Category options", tint = colors.textMuted)
                                        }
                                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, containerColor = colors.surfaceVariant) {
                                            DropdownMenuItem(text = { Text("Edit", color = colors.onBackground) }, onClick = { menuExpanded = false; viewModel.openEditForm(category) })
                                            DropdownMenuItem(text = { Text("Delete", color = colors.error) }, onClick = { menuExpanded = false; viewModel.requestDelete(category) })
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                color = colors.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (category.isSystem) {
                                Text(text = "System", fontSize = 10.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        CategoryFormSheet(viewModel)
    }

    pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = colors.surface,
            titleContentColor = colors.onBackground,
            textContentColor = colors.textSecondary,
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("This can't be undone. Categories still used by transactions can't be deleted.") },
            confirmButton = { TextButton(onClick = { viewModel.confirmDelete() }) { Text("Delete", color = colors.error) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel", color = colors.textMuted) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFormSheet(viewModel: CategoriesViewModel) {
    val colors = LocalAppColors.current
    val name by viewModel.formName.collectAsState()
    val type by viewModel.formType.collectAsState()
    val color by viewModel.formColor.collectAsState()
    val icon by viewModel.formIcon.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val editingId by viewModel.editingCategoryId.collectAsState()
    val isCreate = editingId == null

    AppFormSheet(
        onDismiss = { viewModel.dismissForm() },
        title = if (isCreate) "Add Category ($type)" else "Edit Category",
        confirmLabel = if (isCreate) "Create" else "Save",
        onConfirm = { viewModel.submitForm() },
        isSubmitting = isSubmitting,
        errorMessage = formError
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onNameChanged,
            label = { Text("Name") },
            singleLine = true,
            colors = fieldColors(colors),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Color", fontSize = 12.sp, color = colors.textMuted)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(CATEGORY_COLORS) { colorOption ->
                val hex = categoryColorHex(colorOption)
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(hex))
                            .border(2.dp, if (color == colorOption) colors.onBackground else Color.Transparent, CircleShape)
                            .clickable { viewModel.onColorSelected(colorOption) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == colorOption) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = colors.onBackground, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Text(text = "Icon", fontSize = 12.sp, color = colors.textMuted)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(CATEGORY_ICONS) { iconOption ->
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (icon == iconOption) colors.accent else colors.surfaceVariant)
                            .clickable { viewModel.onIconSelected(iconOption) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForCategory(iconOption),
                            contentDescription = iconOption,
                            tint = if (icon == iconOption) colors.onAccent else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
