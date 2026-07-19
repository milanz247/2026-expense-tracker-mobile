package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppColors
import com.example.ui.theme.GeistMono
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.Spacing

private val fieldColors: @Composable (AppColors) -> TextFieldColors = { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onBackground,
        unfocusedTextColor = colors.onBackground,
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.outline,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.accent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val name by viewModel.name.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isSignUpMode by viewModel.isSignUpMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    val entrance = remember { MutableTransitionState(false) }
    LaunchedEffect(Unit) { entrance.targetState = true }

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is AuthViewModel.AuthEvent.NavigateToDashboard -> onNavigateToDashboard()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(Spacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visibleState = entrance,
            enter = fadeIn(tween(420)) + expandVertically(tween(420), expandFrom = Alignment.Top),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
            ) {
                // Header / Brand
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.accent, colors.accent.copy(alpha = 0.72f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.onAccent,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    AnimatedContent(
                        targetState = isSignUpMode,
                        transitionSpec = {
                            (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 6 }) togetherWith
                                (fadeOut(tween(140)) + slideOutVertically(tween(140)) { -it / 6 })
                        },
                        label = "auth_headline"
                    ) { signUp ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Text(
                                text = if (signUp) "Create your account" else "Welcome back",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (signUp) "Set up your ledger in a minute" else "Sign in to keep tracking your money",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = Spacing.lg)
                            )
                        }
                    }
                }

                // Input Fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = viewModel::onNameChanged,
                                label = { Text("Full Name") },
                                placeholder = { Text("Jane Doe") },
                                singleLine = true,
                                colors = fieldColors(colors),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input"),
                                shape = MaterialTheme.shapes.medium
                            )

                            ExposedDropdownMenuBox(
                                expanded = currencyMenuExpanded,
                                onExpandedChange = { currencyMenuExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = currency,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Reporting Currency") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                                    colors = fieldColors(colors),
                                    textStyle = LocalTextStyle.current.copy(fontFamily = GeistMono),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("currency_input"),
                                    shape = MaterialTheme.shapes.medium
                                )
                                ExposedDropdownMenu(
                                    expanded = currencyMenuExpanded,
                                    onDismissRequest = { currencyMenuExpanded = false },
                                    containerColor = colors.surface
                                ) {
                                    SUPPORTED_CURRENCIES.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, color = colors.onBackground, fontFamily = GeistMono) },
                                            onClick = {
                                                viewModel.onCurrencySelected(option)
                                                currencyMenuExpanded = false
                                            },
                                            modifier = Modifier.testTag("currency_option_$option")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Email Address") },
                        placeholder = { Text("user@example.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors(colors),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Password") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("password_toggle")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = colors.textSecondary
                                )
                            }
                        },
                        colors = fieldColors(colors),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                AnimatedContent(
                    targetState = errorMessage to statusMessage,
                    label = "auth_feedback"
                ) { (error, status) ->
                    when {
                        error != null -> Text(
                            text = error,
                            color = colors.error,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.sm)
                                .testTag("error_banner")
                        )
                        status != null -> Text(
                            text = status,
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.sm)
                                .testTag("status_banner")
                        )
                        else -> Spacer(modifier = Modifier.height(0.dp))
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = viewModel::onSubmit,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent,
                            disabledContainerColor = colors.surfaceVariant,
                            disabledContentColor = colors.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_button"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = colors.onAccent,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            AnimatedContent(targetState = isSignUpMode, label = "auth_submit_label") { signUp ->
                                Text(
                                    text = if (signUp) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUpMode) "Already have an account?" else "New here?",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = if (isSignUpMode) "Sign In" else "Sign Up",
                            fontSize = 13.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = !isLoading) { viewModel.toggleMode() }
                                .testTag("toggle_mode_button")
                        )
                    }
                }
            }
        }
    }
}
