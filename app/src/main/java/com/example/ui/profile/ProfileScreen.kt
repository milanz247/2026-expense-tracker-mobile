package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.SUPPORTED_CURRENCIES
import com.example.network.SUPPORTED_TIMEZONES
import com.example.ui.common.ErrorBanner
import com.example.ui.common.MatteCard
import com.example.ui.theme.AppColors
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.ThemeMode

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
fun ProfileScreen(
    viewModel: ProfileViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onManageCategories: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val timezone by viewModel.timezone.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var timezoneMenuExpanded by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.background).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Text(text = "Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.onBackground)
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }
            if (saveMessage != null) {
                item { Text(text = saveMessage!!, color = colors.textSecondary, fontSize = 13.sp) }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    com.example.ui.common.SectionLabel("Appearance")
                    MatteCard {
                        AppearancePicker(themeMode = themeMode, onThemeModeChange = onThemeModeChange)
                    }
                }
            }

            item {
                MatteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = viewModel::onNameChanged,
                            label = { Text("Name") },
                            singleLine = true,
                            colors = fieldColors(colors),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Email") },
                            singleLine = true,
                            colors = fieldColors(colors),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenuBox(expanded = currencyMenuExpanded, onExpandedChange = { currencyMenuExpanded = it }) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                                colors = fieldColors(colors),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = currencyMenuExpanded, onDismissRequest = { currencyMenuExpanded = false }, containerColor = colors.surfaceVariant) {
                                SUPPORTED_CURRENCIES.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = colors.onBackground) }, onClick = { viewModel.onCurrencySelected(option); currencyMenuExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = timezoneMenuExpanded, onExpandedChange = { timezoneMenuExpanded = it }) {
                            OutlinedTextField(
                                value = timezone,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Timezone") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timezoneMenuExpanded) },
                                colors = fieldColors(colors),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = timezoneMenuExpanded, onDismissRequest = { timezoneMenuExpanded = false }, containerColor = colors.surfaceVariant) {
                                SUPPORTED_TIMEZONES.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = colors.onBackground) }, onClick = { viewModel.onTimezoneSelected(option); timezoneMenuExpanded = false })
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.saveProfile() },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onAccent)
                            else Text("Save Changes", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item { SettingsRow("Manage Categories", onClick = onManageCategories) }
            item { SettingsRow("Change Password", onClick = { showPasswordDialog = true }) }

            item {
                Button(
                    onClick = { showLogoutConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.error),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out")
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(viewModel, onDismiss = { showPasswordDialog = false })
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = colors.surface,
            titleContentColor = colors.onBackground,
            textContentColor = colors.textSecondary,
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your ledger.") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.logout(onLoggedOut) }) { Text("Log Out", color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel", color = colors.textMuted) } }
        )
    }
}

@Composable
private fun AppearancePicker(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val colors = LocalAppColors.current
    val options = listOf(
        Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
        Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
        Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (mode, label, icon) ->
            val selected = themeMode == mode
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) colors.accent else colors.surfaceVariant)
                    .clickable { onThemeModeChange(mode) }
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) colors.onAccent else colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) colors.onAccent else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = colors.onBackground)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
    }
}

@Composable
private fun ChangePasswordDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    val colors = LocalAppColors.current
    val oldPassword by viewModel.oldPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val error by viewModel.passwordError.collectAsState()
    val successMessage by viewModel.passwordSuccessMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.clearPasswordSuccessMessage(); onDismiss() },
        containerColor = colors.surface,
        titleContentColor = colors.onBackground,
        textContentColor = colors.textSecondary,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (successMessage != null) {
                    Text(text = successMessage!!, color = colors.textSecondary, fontSize = 13.sp)
                } else {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = viewModel::onOldPasswordChanged,
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(colors),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = viewModel::onNewPasswordChanged,
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(colors),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(colors),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error!!, color = colors.error, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                TextButton(onClick = { viewModel.clearPasswordSuccessMessage(); onDismiss() }) { Text("Done", color = colors.accent) }
            } else {
                TextButton(onClick = { viewModel.submitPasswordChange() }, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
                    else Text("Update", color = colors.accent)
                }
            }
        },
        dismissButton = {
            if (successMessage == null) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textMuted) }
            }
        }
    )
}
