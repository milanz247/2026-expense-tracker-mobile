package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.NumberFormat
import java.util.*

// ==========================================
// Shared design tokens — every screen and dialog pulls from this single
// palette (backed by LocalAppColors, see ui/theme/Palette.kt) so the
// whole app, including AlertDialogs which Material3 would otherwise tint
// with its own auto-derived tonal surface, reads as one consistent
// system that follows the light/dark toggle instead of a patchwork of
// screens frozen to one hardcoded mode.
// ==========================================

val AppBg: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.bg
val SurfaceColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.surface
val DialogSurfaceColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.dialogSurface
val TextPrimary: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.textPrimary
val TextSecondary: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.textSecondary
val AccentColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.accent
val AccentMuted: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.accentMuted
val AccentOnMuted: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.onAccentMuted
val DangerColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.danger
val SuccessColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.success
val BorderColor: Color @Composable get() = com.example.ui.theme.LocalAppColors.current.border

@Composable
fun StandardButtonColors(danger: Boolean = false) = ButtonDefaults.buttonColors(
    containerColor = if (danger) DangerColor else AccentColor,
    contentColor = Color.White,
    disabledContainerColor = AccentMuted,
    disabledContentColor = TextSecondary.copy(alpha = 0.5f)
)

/**
 * Every "add/edit" dialog in the app renders through this one wrapper so
 * they all share the same dark surface, the same scroll-on-overflow
 * behavior, and the same "Save button disables instead of silently
 * doing nothing" affordance when a required field is missing.
 */
@Composable
fun StandardFormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    danger: Boolean = false,
    helperText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogSurfaceColor,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                content()
                if (helperText != null) {
                    Text(helperText, fontSize = 11.sp, color = DangerColor)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = StandardButtonColors(danger)
            ) {
                Text(confirmLabel, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

/** Label for a form field that highlights when it still needs a value. */
@Composable
fun RequiredFieldLabel(text: String, satisfied: Boolean) {
    Text(
        text = if (satisfied) text else "$text *",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (satisfied) TextSecondary else AccentColor
    )
}

/**
 * Standard tap-to-open dropdown for picking one item out of a list —
 * used everywhere a dialog previously made you scan a horizontal row of
 * chips for a wallet or category. Shows an optional leading icon/color
 * per item so a category dropdown can carry its icon along with it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    required: Boolean = true,
    emptyMessage: String = "Nothing available yet.",
    itemIcon: ((T) -> ImageVector)? = null,
    itemColor: ((T) -> Color)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        RequiredFieldLabel(label, satisfied = !required || selected != null)
        if (items.isEmpty()) {
            Text(emptyMessage, fontSize = 12.sp, color = TextSecondary)
        } else {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.let(itemLabel) ?: "Tap to select",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    leadingIcon = if (selected != null && itemIcon != null) {
                        {
                            Icon(
                                itemIcon(selected),
                                contentDescription = null,
                                tint = itemColor?.invoke(selected) ?: AccentColor
                            )
                        }
                    } else null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(itemLabel(item)) },
                            leadingIcon = itemIcon?.let { getIcon ->
                                {
                                    Icon(
                                        getIcon(item),
                                        contentDescription = null,
                                        tint = itemColor?.invoke(item) ?: TextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onSelect(item)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/** A big, unmistakably-obvious two-way choice (e.g. lent vs. borrowed money). */
@Composable
fun DirectionOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentColor else SurfaceColor)
            .border(
                width = 1.dp,
                color = if (selected) AccentColor else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color.White else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// Color & Icon Helpers for Jetpack Compose
// ==========================================

fun getColorForName(name: String): Color {
    return when (name.lowercase()) {
        "emerald" -> Color(0xFF10B981)
        "blue" -> Color(0xFF3B82F6)
        "teal" -> Color(0xFF14B8A6)
        "orange" -> Color(0xFFF97316)
        "yellow" -> Color(0xFFFBBF24)
        "slate" -> Color(0xFF64748B)
        "violet" -> Color(0xFF8B5CF6)
        "red" -> Color(0xFFEF4444)
        "rose" -> Color(0xFFF43F5E)
        "pink" -> Color(0xFFEC4899)
        "fuchsia" -> Color(0xFFD946EF)
        "sky" -> Color(0xFF0EA5E9)
        else -> Color(0xFF78909C)
    }
}

fun getIconForName(name: String): ImageVector {
    return when (name) {
        "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
        "TrendingDown" -> Icons.AutoMirrored.Filled.TrendingDown
        "Coins" -> Icons.Default.MonetizationOn
        "Wallet" -> Icons.Default.Wallet
        "Utensils" -> Icons.Default.Restaurant
        "Car" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "Clapperboard" -> Icons.Default.Movie
        "Zap" -> Icons.Default.FlashOn
        "HeartPulse" -> Icons.Default.LocalHospital
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Gift" -> Icons.Default.CardGiftcard
        "Plane" -> Icons.Default.Flight
        "GraduationCap" -> Icons.Default.School
        "Briefcase" -> Icons.Default.Work
        "Smartphone" -> Icons.Default.PhoneAndroid
        "Dumbbell" -> Icons.Default.FitnessCenter
        "PawPrint" -> Icons.Default.Pets
        "CreditCard" -> Icons.Default.CreditCard
        "Tag" -> Icons.Default.LocalOffer
        "Music" -> Icons.Default.MusicNote
        "Shirt" -> Icons.Default.Checkroom
        "ArrowLeftRight" -> Icons.Default.SwapHoriz
        "BookOpen" -> Icons.Default.Book
        "HandCoins" -> Icons.Default.Payments
        "CheckCircle" -> Icons.Default.CheckCircle
        else -> Icons.Default.Category
    }
}

fun formatCents(cents: Long, currency: String = "USD"): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.currency = Currency.getInstance(currency)
    return format.format(cents / 100.0)
}

/** A real ring/donut chart for a category breakdown — each entry already carries its own hex color from the backend, so the chart is naturally multi-colored rather than one dominant accent tone. */
@Composable
fun CategoryPieChart(
    entries: List<CategoryBreakdownEntry>,
    modifier: Modifier = Modifier
) {
    val fallbackColors = listOf(AccentColor, SuccessColor, Color(0xFF3B82F6), Color(0xFFFBBF24), Color(0xFF8B5CF6), Color(0xFF0EA5E9))
    val slices = remember(entries) {
        entries.mapIndexed { index, entry ->
            val color = try {
                Color(android.graphics.Color.parseColor(entry.color))
            } catch (e: Exception) {
                fallbackColors[index % fallbackColors.size]
            }
            entry to color
        }
    }
    val trackColor = BorderColor

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = size.minDimension * 0.26f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                topLeft = topLeft,
                size = arcSize
            )

            var startAngle = -90f
            slices.forEach { (entry, color) ->
                val sweep = (entry.percentage / 100.0 * 360.0).toFloat()
                if (sweep > 0f) {
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep.coerceAtMost(359.5f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        topLeft = topLeft,
                        size = arcSize
                    )
                    startAngle += sweep
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.take(6).forEach { (entry, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${entry.categoryName} · ${String.format(Locale.US, "%.0f", entry.percentage)}%",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// Authentication Screens (Login & Register)
// ==========================================

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: FinanceViewModel
) {
    var email by remember { mutableStateOf("milanmadusankamms@gmail.com") }
    var password by remember { mutableStateOf("secure_password123") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "App Logo",
                tint = AccentColor,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Planetary Finance",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Secure Personal Finance Management System",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = AccentColor)
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Create a new account instead", color = AccentColor)
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: FinanceViewModel
) {
    var name by remember { mutableStateOf("Milan Madusanka") }
    var email by remember { mutableStateOf("milanmadusankamms@gmail.com") }
    var password by remember { mutableStateOf("secure_password123") }
    var currency by remember { mutableStateOf("USD") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = "Register Logo",
                tint = AccentColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Join Planetary Finance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Secure Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Default Currency (e.g. USD, EUR, LKR)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    focusedLabelColor = AccentColor
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = AccentColor)
            } else {
                Button(
                    onClick = { viewModel.register(name, email, password, currency) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Register & Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign in with existing credentials", color = AccentColor)
                }
            }
        }
    }
}

// ==========================================
// Dashboard Screen
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToWallets: () -> Unit = {}
) {
    val report by viewModel.reportSummary.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    LaunchedEffect(Unit) {
        viewModel.refreshReports()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcoming Card with Net Balance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AccentMuted),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "NET FINANCIAL POSITION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOnMuted
                )
                Text(
                    text = report?.netBalance?.amountCents?.let { formatCents(it, currency) } ?: "$0.00",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentOnMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Monthly Cash Inflow", fontSize = 11.sp, color = AccentOnMuted.copy(alpha = 0.7f))
                        Text(
                            text = report?.totalIncome?.amountCents?.let { formatCents(it, currency) } ?: "$0.00",
                            fontWeight = FontWeight.Bold,
                            color = SuccessColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Monthly Outflow", fontSize = 11.sp, color = AccentOnMuted.copy(alpha = 0.7f))
                        Text(
                            text = report?.totalExpense?.amountCents?.let { formatCents(it, currency) } ?: "$0.00",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B7A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions block
        Text("Quick Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToWallets,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor, contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Wallets")
            }
            Button(
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Ledger Logs")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category-wise spending breakdown
        val breakdown = report?.categoryBreakdown.orEmpty().filter { it.amountCents > 0 }
        if (breakdown.isNotEmpty()) {
            Text("Spending by Category", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                CategoryPieChart(
                    entries = breakdown,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recent Transaction Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            TextButton(onClick = onNavigateToTransactions) {
                Text("View All", color = AccentColor)
            }
        }

        if (report?.recentTransactions.isNullOrEmpty()) {
            Text(
                text = "No recorded transactions found. Try making your first expense or inflow log.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            report?.recentTransactions?.forEach { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                tx.category?.color?.let { Color(android.graphics.Color.parseColor(it)) }
                                    ?: BorderColor
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconForName(tx.category?.icon ?: "Wallet"),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.description,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TextPrimary
                        )
                        Text(
                            text = tx.category?.name ?: tx.type.uppercase(),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = (if (tx.type == "expense") "-" else "+") + formatCents(tx.amount, currency),
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == "expense") DangerColor else SuccessColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// Wallets Screen
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WalletsScreen(viewModel: FinanceViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    var showAddWalletDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<LocalAccount?>(null) }

    Scaffold(
        containerColor = AppBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWalletDialog = true },
                containerColor = AccentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Wallet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .padding(16.dp)
        ) {
            Text("Wallets & Accounts", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
            if (accounts.isNotEmpty()) {
                Text(
                    "Long-press a wallet to edit or archive it",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (accounts.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddWalletDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No wallets created yet. Tap '+' to configure.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(accounts) { acc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { accountToEdit = acc }
                                ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = acc.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = acc.type.replace("_", " ").uppercase(),
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = formatCents(acc.balance, currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (acc.balance < 0) DangerColor else AccentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWalletDialog) {
        var walletName by remember { mutableStateOf("") }
        var walletType by remember { mutableStateOf("bank") }
        var initialBalance by remember { mutableStateOf("") }
        var creditLimit by remember { mutableStateOf("0") }

        StandardFormDialog(
            title = "New Wallet",
            onDismiss = { showAddWalletDialog = false },
            confirmLabel = "Save Wallet",
            confirmEnabled = walletName.isNotBlank(),
            onConfirm = {
                viewModel.createAccount(
                    walletName,
                    walletType,
                    initialBalance.toDoubleOrNull() ?: 0.0,
                    creditLimit.toDoubleOrNull() ?: 0.0
                )
                showAddWalletDialog = false
            }
        ) {
            RequiredFieldLabel("Wallet Name", walletName.isNotBlank())
            OutlinedTextField(
                value = walletName,
                onValueChange = { walletName = it },
                placeholder = { Text("e.g. Main Bank Account") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Account Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("bank", "cash", "credit_card", "investment").forEach { type ->
                    ElevatedFilterChip(
                        selected = walletType == type,
                        onClick = { walletType = type },
                        label = { Text(type.replace("_", " "), fontSize = 10.sp) }
                    )
                }
            }
            OutlinedTextField(
                value = initialBalance,
                onValueChange = { initialBalance = it },
                label = { Text("Initial Balance") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (walletType == "credit_card") {
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("Credit Limit") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (accountToEdit != null) {
        val editing = accountToEdit!!
        var editName by remember(editing.id) { mutableStateOf(editing.name) }
        var editType by remember(editing.id) { mutableStateOf(editing.type) }

        AlertDialog(
            onDismissRequest = { accountToEdit = null },
            containerColor = DialogSurfaceColor,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Edit Wallet", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RequiredFieldLabel("Wallet Name", editName.isNotBlank())
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Account Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("bank", "cash", "credit_card", "investment").forEach { type ->
                            ElevatedFilterChip(
                                selected = editType == type,
                                onClick = { editType = type },
                                label = { Text(type.replace("_", " "), fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAccount(editing.id, editName, editType)
                        accountToEdit = null
                    },
                    enabled = editName.isNotBlank(),
                    colors = StandardButtonColors()
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        viewModel.deleteAccount(editing.id)
                        accountToEdit = null
                    }) {
                        Text("Archive", color = DangerColor)
                    }
                    TextButton(onClick = { accountToEdit = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        )
    }
}

// ==========================================
// Transactions Screen
// ==========================================

@Composable
fun TransactionsScreen(viewModel: FinanceViewModel) {
    val txs by viewModel.transactions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedWalletFilter by viewModel.selectedAccountFilter.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    var showAddTxDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTxDialog = true },
                containerColor = AccentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBg)
                .padding(16.dp)
        ) {
            Text("Ledger History", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Wallet Horizontal Filtering Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    InputChip(
                        selected = selectedWalletFilter == null,
                        onClick = { viewModel.filterTransactionsByAccount(null) },
                        label = { Text("All Accounts") }
                    )
                }
                items(accounts) { acc ->
                    InputChip(
                        selected = selectedWalletFilter == acc.id,
                        onClick = { viewModel.filterTransactionsByAccount(acc.id) },
                        label = { Text(acc.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (txs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions logged. Tap '+' to create.", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(txs) { tx ->
                        val matchingCat = categories.find { it.id == tx.categoryId }
                        val matchingWallet = accounts.find { it.id == tx.accountId }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(matchingCat?.let { getColorForName(it.color) } ?: Color(0xFF78909C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconForName(matchingCat?.icon ?: "Wallet"),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.description,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = matchingWallet?.name ?: tx.type.uppercase(),
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        if (tx.fee > 0) {
                                            Text(
                                                text = "Fee: ${formatCents(tx.fee, currency)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFFF8A80)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = (if (tx.type == "expense") "-" else "+") + formatCents(tx.amount, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "expense") DangerColor else SuccessColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTxDialog) {
        var txType by remember { mutableStateOf("expense") }
        var amount by remember { mutableStateOf("") }
        var fee by remember { mutableStateOf("0") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }
        var selectedDestWalletId by remember { mutableStateOf<Long?>(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id) }
        var selectedCatId by remember { mutableStateOf<Long?>(categories.firstOrNull()?.id) }
        var description by remember { mutableStateOf("") }

        val amountVal = amount.toDoubleOrNull()
        val isTransfer = txType == "transfer"
        val walletsAvailable = accounts.isNotEmpty()
        val transferNeedsTwoWallets = isTransfer && accounts.size < 2
        val confirmEnabled = walletsAvailable &&
            amountVal != null && amountVal > 0.0 &&
            selectedWalletId != null &&
            (!isTransfer || (selectedDestWalletId != null && selectedDestWalletId != selectedWalletId))

        StandardFormDialog(
            title = "Log Transaction",
            onDismiss = { showAddTxDialog = false },
            confirmLabel = "Add Entry",
            confirmEnabled = confirmEnabled,
            helperText = when {
                !walletsAvailable -> "Create a wallet first (Overview tab) before logging a transaction."
                transferNeedsTwoWallets -> "You need at least two wallets to record a transfer."
                isTransfer && selectedDestWalletId == selectedWalletId && selectedWalletId != null ->
                    "Source and destination wallets must be different."
                amount.isNotBlank() && (amountVal == null || amountVal <= 0.0) -> "Enter a valid amount greater than 0."
                else -> null
            },
            onConfirm = {
                viewModel.createTransaction(
                    type = txType,
                    amount = amountVal!!,
                    fee = fee.toDoubleOrNull() ?: 0.0,
                    accountId = if (isTransfer) null else selectedWalletId,
                    sourceAccountId = if (isTransfer) selectedWalletId else null,
                    destinationAccountId = if (isTransfer) selectedDestWalletId else null,
                    categoryId = if (isTransfer) null else selectedCatId,
                    description = description,
                    date = viewModel.getCurrentIsoTimestamp()
                )
                showAddTxDialog = false
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("income", "expense", "transfer").forEach { type ->
                    ElevatedFilterChip(
                        selected = txType == type,
                        onClick = {
                            txType = type
                            if (type == "transfer") {
                                selectedCatId = null
                            }
                        },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            RequiredFieldLabel("Amount", amountVal != null && amountVal > 0.0)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fee,
                onValueChange = { fee = it },
                label = { Text("Fee (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            if (walletsAvailable) {
                DropdownSelector(
                    label = if (isTransfer) "Source Account" else "Wallet",
                    items = accounts,
                    selected = accounts.find { it.id == selectedWalletId },
                    onSelect = { selectedWalletId = it.id },
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("No wallets yet — add one from the Overview tab first.", fontSize = 12.sp, color = DangerColor)
            }

            if (isTransfer) {
                DropdownSelector(
                    label = "Destination Account",
                    items = accounts,
                    selected = accounts.find { it.id == selectedDestWalletId },
                    onSelect = { selectedDestWalletId = it.id },
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!isTransfer) {
                val matchingCats = categories.filter { it.type == txType }
                DropdownSelector(
                    label = "Category",
                    items = matchingCats,
                    selected = matchingCats.find { it.id == selectedCatId },
                    onSelect = { selectedCatId = it.id },
                    itemLabel = { it.name },
                    itemIcon = { getIconForName(it.icon) },
                    itemColor = { getColorForName(it.color) },
                    required = false,
                    emptyMessage = "No $txType categories yet — add one in the Categories tab.",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

}

// ==========================================
// Debts Screen
// ==========================================

@Composable
fun DebtsScreen(viewModel: FinanceViewModel) {
    val debts by viewModel.debts.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val repayments by viewModel.selectedDebtRepayments.collectAsState()
    val selectedDebtId by viewModel.selectedDebtId.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showRepayDialog by remember { mutableStateOf(false) }

    val activeDebt = debts.find { it.id == selectedDebtId }

    Scaffold(
        floatingActionButton = {
            if (activeDebt == null) {
                FloatingActionButton(
                    onClick = { showAddDebtDialog = true },
                    containerColor = AccentColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Debt Log")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .padding(16.dp)
        ) {
            if (activeDebt != null) {
                // Inside selected Debt Details screen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    IconButton(onClick = { viewModel.selectDebt(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Debt Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(activeDebt.personName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text("Type: ${activeDebt.type.uppercase()}", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCents(activeDebt.totalAmount, currency), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = formatCents(activeDebt.remainingAmount, currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (activeDebt.remainingAmount > 0) DangerColor else SuccessColor
                                )
                            }
                        }
                        if (activeDebt.remainingAmount > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showRepayDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                            ) {
                                Text("Record Repayment Settlement")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Repayment Transactions Log", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                if (repayments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No repayments documented yet.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                        items(repayments) { rep ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Repayment Received", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(rep.date, fontSize = 11.sp, color = TextSecondary)
                                }
                                Text(
                                    formatCents(rep.amount, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessColor
                                )
                            }
                        }
                    }
                }

            } else {
                // List of Debts
                Text("Debts Ledger", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                if (debts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active or completed debts on record.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(debts) { d ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectDebt(d.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(d.personName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                        Text("Type: ${d.type.uppercase()} (Due: ${d.dueDate})", fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCents(d.remainingAmount, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (d.status == "settled") SuccessColor else DangerColor
                                        )
                                        Text(d.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDebtDialog) {
        var personName by remember { mutableStateOf("") }
        var debtType by remember { mutableStateOf("lent") }
        var totalAmount by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }
        var dueDate by remember { mutableStateOf("") }

        val debtAmount = totalAmount.toDoubleOrNull()
        val debtWalletsAvailable = accounts.isNotEmpty()
        val debtConfirmEnabled = personName.isNotBlank() && debtAmount != null && debtAmount > 0.0 && selectedWalletId != null

        StandardFormDialog(
            title = "Log New Debt",
            onDismiss = { showAddDebtDialog = false },
            confirmLabel = "Add Debt Record",
            confirmEnabled = debtConfirmEnabled,
            helperText = when {
                !debtWalletsAvailable -> "Create a wallet first (Overview tab) before logging a debt."
                totalAmount.isNotBlank() && (debtAmount == null || debtAmount <= 0.0) -> "Enter a valid amount greater than 0."
                else -> null
            },
            onConfirm = {
                viewModel.createDebt(
                    personName,
                    debtType,
                    debtAmount!!,
                    selectedWalletId!!,
                    dueDate.ifEmpty { "2026-12-31" }
                )
                showAddDebtDialog = false
            }
        ) {
            RequiredFieldLabel("Person's Name", personName.isNotBlank())
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                placeholder = { Text("Who's involved?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Direction", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DirectionOption(
                    label = "I Lent Money",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    selected = debtType == "lent",
                    onClick = { debtType = "lent" },
                    modifier = Modifier.weight(1f)
                )
                DirectionOption(
                    label = "I Borrowed Money",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    selected = debtType == "borrowed",
                    onClick = { debtType = "borrowed" },
                    modifier = Modifier.weight(1f)
                )
            }
            RequiredFieldLabel("Amount", debtAmount != null && debtAmount > 0.0)
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (debtWalletsAvailable) {
                DropdownSelector(
                    label = "Wallet",
                    items = accounts,
                    selected = accounts.find { it.id == selectedWalletId },
                    onSelect = { selectedWalletId = it.id },
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("No wallets yet — add one from the Overview tab first.", fontSize = 12.sp, color = DangerColor)
            }
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (optional)") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showRepayDialog && activeDebt != null) {
        var repayAmount by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }
        val repayVal = repayAmount.toDoubleOrNull()
        val exceedsRemaining = repayVal != null && repayVal * 100 > activeDebt.remainingAmount

        StandardFormDialog(
            title = "Record Repayment",
            onDismiss = { showRepayDialog = false },
            confirmLabel = "Submit Repayment",
            confirmEnabled = repayVal != null && repayVal > 0.0 && selectedWalletId != null,
            helperText = when {
                repayAmount.isNotBlank() && (repayVal == null || repayVal <= 0.0) -> "Enter a valid amount greater than 0."
                exceedsRemaining -> "This is more than the remaining balance — the backend will reject it."
                else -> null
            },
            onConfirm = {
                viewModel.repayDebt(activeDebt.id, repayVal!!, selectedWalletId!!)
                showRepayDialog = false
            }
        ) {
            Text(
                "Remaining balance: ${formatCents(activeDebt.remainingAmount, currency)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            RequiredFieldLabel("Repayment Amount", repayVal != null && repayVal > 0.0)
            OutlinedTextField(
                value = repayAmount,
                onValueChange = { repayAmount = it },
                placeholder = { Text("0.00") },
                singleLine = true,
                isError = exceedsRemaining,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            DropdownSelector(
                label = "Paid From Wallet",
                items = accounts,
                selected = accounts.find { it.id == selectedWalletId },
                onSelect = { selectedWalletId = it.id },
                itemLabel = { it.name },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==========================================
// Store Tabs Screen
// ==========================================

@Composable
fun StoreTabsScreen(viewModel: FinanceViewModel) {
    val creditors by viewModel.storeCreditors.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCreditorId by viewModel.selectedCreditorId.collectAsState()
    val purchases by viewModel.selectedCreditorPurchases.collectAsState()
    val settlements by viewModel.selectedCreditorSettlements.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    var showAddCreditorDialog by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }

    val activeCreditor = creditors.find { it.id == selectedCreditorId }

    Scaffold(
        floatingActionButton = {
            if (activeCreditor == null) {
                FloatingActionButton(
                    onClick = { showAddCreditorDialog = true },
                    containerColor = AccentColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Store Creditor")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .padding(16.dp)
        ) {
            if (activeCreditor != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    IconButton(onClick = { viewModel.selectCreditor(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Store Tab Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(activeCreditor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Outstanding Tab Debt: " + formatCents(activeCreditor.outstandingDebt, currency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DangerColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showPurchaseDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                            ) {
                                Text("New Purchase", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { showSettleDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = TextSecondary)
                            ) {
                                Text("Settle/Pay Off", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tab Purchases Log", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                if (purchases.isEmpty()) {
                    Text("No purchases logged on this tab yet.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(purchases) { pur ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(pur.description, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(pur.date, fontSize = 11.sp, color = TextSecondary)
                                }
                                Text(
                                    formatCents(pur.amount, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = DangerColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Tab Settlements", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                if (settlements.isEmpty()) {
                    Text("No payments have been settled on this tab.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(settlements) { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Settlement Payment", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(set.date, fontSize = 11.sp, color = TextSecondary)
                                }
                                Text(
                                    formatCents(set.amountPaid, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessColor
                                )
                            }
                        }
                    }
                }

            } else {
                Text("Shop Creditors (Tabs)", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                if (creditors.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No shop creditor tabs logged.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(creditors) { cr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectCreditor(cr.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cr.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                        Text("Outstanding Tab: " + formatCents(cr.outstandingDebt, currency), fontSize = 12.sp, color = TextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCreditorDialog) {
        var credName by remember { mutableStateOf("") }

        StandardFormDialog(
            title = "Add Store Tab",
            onDismiss = { showAddCreditorDialog = false },
            confirmLabel = "Save Merchant",
            confirmEnabled = credName.isNotBlank(),
            onConfirm = {
                viewModel.createStoreCreditor(credName)
                showAddCreditorDialog = false
            }
        ) {
            RequiredFieldLabel("Store / Merchant Name", credName.isNotBlank())
            OutlinedTextField(
                value = credName,
                onValueChange = { credName = it },
                placeholder = { Text("e.g. Corner Grocery") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showPurchaseDialog && activeCreditor != null) {
        var purchaseAmount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCatId by remember { mutableStateOf<Long?>(categories.firstOrNull { it.type == "expense" }?.id) }
        val purchaseVal = purchaseAmount.toDoubleOrNull()
        val expenseCats = categories.filter { it.type == "expense" }

        StandardFormDialog(
            title = "Record Tab Purchase",
            onDismiss = { showPurchaseDialog = false },
            confirmLabel = "Confirm Purchase",
            confirmEnabled = purchaseVal != null && purchaseVal > 0.0 && selectedCatId != null,
            helperText = when {
                expenseCats.isEmpty() -> "No expense categories yet — add one in the Categories tab."
                purchaseAmount.isNotBlank() && (purchaseVal == null || purchaseVal <= 0.0) -> "Enter a valid amount greater than 0."
                else -> null
            },
            onConfirm = {
                viewModel.createPurchase(
                    activeCreditor.id,
                    purchaseVal!!,
                    0.0,
                    selectedCatId!!,
                    description
                )
                showPurchaseDialog = false
            }
        ) {
            RequiredFieldLabel("Amount", purchaseVal != null && purchaseVal > 0.0)
            OutlinedTextField(
                value = purchaseAmount,
                onValueChange = { purchaseAmount = it },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Item Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownSelector(
                label = "Expense Category",
                items = expenseCats,
                selected = expenseCats.find { it.id == selectedCatId },
                onSelect = { selectedCatId = it.id },
                itemLabel = { it.name },
                itemIcon = { getIconForName(it.icon) },
                itemColor = { getColorForName(it.color) },
                emptyMessage = "No expense categories yet — add one in the Categories tab.",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSettleDialog && activeCreditor != null) {
        var payAmount by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }
        val payVal = payAmount.toDoubleOrNull()
        val settleWalletsAvailable = accounts.isNotEmpty()

        StandardFormDialog(
            title = "Settle Tab Payment",
            onDismiss = { showSettleDialog = false },
            confirmLabel = "Submit Settlement",
            confirmEnabled = payVal != null && payVal > 0.0 && selectedWalletId != null,
            helperText = when {
                !settleWalletsAvailable -> "Create a wallet first (Overview tab) before settling a tab."
                payAmount.isNotBlank() && (payVal == null || payVal <= 0.0) -> "Enter a valid amount greater than 0."
                else -> null
            },
            onConfirm = {
                viewModel.createSettlement(
                    activeCreditor.id,
                    selectedWalletId!!,
                    payVal!!,
                    0.0
                )
                showSettleDialog = false
            }
        ) {
            Text(
                "Outstanding tab debt: ${formatCents(activeCreditor.outstandingDebt, currency)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            RequiredFieldLabel("Amount Paid", payVal != null && payVal > 0.0)
            OutlinedTextField(
                value = payAmount,
                onValueChange = { payAmount = it },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (settleWalletsAvailable) {
                DropdownSelector(
                    label = "Paid From Wallet",
                    items = accounts,
                    selected = accounts.find { it.id == selectedWalletId },
                    onSelect = { selectedWalletId = it.id },
                    itemLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("No wallets yet — add one from the Overview tab first.", fontSize = 12.sp, color = DangerColor)
            }
        }
    }
}

// ==========================================
// Reports & Exports Screen
// ==========================================

@Composable
fun ReportsScreen(viewModel: FinanceViewModel) {
    val report by viewModel.reportSummary.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    LaunchedEffect(Unit) {
        viewModel.refreshReports()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Financial Analytics & Statement", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        // Donut / Pie Progress Representation for Category Expenses
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Category Expense Allocation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                val breakdown = report?.categoryBreakdown.orEmpty().filter { it.amountCents > 0 }
                if (breakdown.isEmpty()) {
                    Text(
                        "No expenses recorded yet — this fills in once you log a transaction.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                } else {
                    breakdown.forEach { entry ->
                        val color = remember(entry.color) {
                            try {
                                Color(android.graphics.Color.parseColor(entry.color))
                            } catch (e: Exception) {
                                Color(0xFF8B5CF6)
                            }
                        }
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.categoryName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("${String.format(Locale.US, "%.1f", entry.percentage)}%", fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (entry.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = color,
                                trackColor = BorderColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Cash Flow Chart (Simple column heights inside a row)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7-Month Cash Flow Trends", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(20.dp))

                val cashFlow = report?.cashFlow.orEmpty()
                val maxCashFlowValue = cashFlow.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.takeIf { it > 0 } ?: 1L

                if (cashFlow.all { it.income == 0L && it.expense == 0L }) {
                    Text(
                        "No cash flow yet — this fills in as you log income and expenses.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        cashFlow.forEach { cf ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Income bar — height is relative to the biggest value in
                                    // this dataset, not a fixed divisor, so real amounts (which
                                    // are usually well under $10k) still render visibly instead
                                    // of collapsing to the same tiny sliver every time.
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(
                                                (if (cf.income > 0)
                                                    ((cf.income.toDouble() / maxCashFlowValue.toDouble()) * 100).coerceIn(4.0, 100.0)
                                                else 0.0).dp
                                            )
                                            .background(AccentColor, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    )
                                    // Expense bar
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .height(
                                                (if (cf.expense > 0)
                                                    ((cf.expense.toDouble() / maxCashFlowValue.toDouble()) * 100).coerceIn(4.0, 100.0)
                                                else 0.0).dp
                                            )
                                            .background(DangerColor, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(cf.month, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(AccentColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inflow", fontSize = 10.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(10.dp).background(DangerColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Outflow", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exporter Controls block
        Text("Generate Statements", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.exportTransactions("csv", null, null, null, null, null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV")
            }
            Button(
                onClick = { viewModel.exportTransactions("pdf", null, null, null, null, null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = TextSecondary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Statement PDF")
            }
        }
    }
}

// ==========================================
// Settings / Profile Screen
// ==========================================

@Composable
fun SettingsScreen(viewModel: FinanceViewModel, onNavigateToCategories: () -> Unit = {}) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val prof = (authState as AuthState.Authenticated).profile
            name = prof.name
            currency = prof.currency
            timezone = prof.timezone
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Profile & System Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)

        val isDarkTheme by viewModel.darkTheme.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = AccentColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text(
                            if (isDarkTheme) "On — switch to light theme" else "Off — switch to dark theme",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentColor)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Personal Parameters", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Default Currency Representation") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = timezone,
                    onValueChange = { timezone = it },
                    label = { Text("Timezone Designation") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { viewModel.updateProfile(name, currency, timezone) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Update Personal Profile", color = Color.White)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Change Passcode Identity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (newPassword == confirmPassword && oldPassword.isNotBlank() && newPassword.isNotBlank()) {
                            viewModel.changePassword(oldPassword, newPassword, confirmPassword)
                        } else {
                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Apply Passcode Security", color = Color.White)
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.passwordChangeSuccess.collect { success ->
                if (success) {
                    Toast.makeText(context, "Password changed successfully.", Toast.LENGTH_SHORT).show()
                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToCategories),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = AccentColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Categories", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Manage income & expense tags", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DangerColor)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Destroy Token & Terminate Session", color = Color.White)
        }
    }
}

private val PRESET_CATEGORY_COLORS = listOf("emerald", "blue", "teal", "orange", "yellow", "slate", "violet", "red", "rose", "pink", "fuchsia", "sky")
private val PRESET_CATEGORY_ICONS = listOf(
    "Wallet", "Utensils", "Car", "Home", "Clapperboard", "Zap", "HeartPulse",
    "ShoppingBag", "Gift", "Plane", "GraduationCap", "Briefcase", "Smartphone",
    "Dumbbell", "PawPrint", "Music", "Shirt", "Tag", "TrendingUp", "Coins"
)

@Composable
private fun CategoryGroup(title: String, accent: Color, categories: List<LocalCategory>, viewModel: FinanceViewModel) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = accent)
    Spacer(modifier = Modifier.height(8.dp))
    if (categories.isEmpty()) {
        Text("No $title categories yet — tap + to add one.", fontSize = 12.sp, color = TextSecondary)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getColorForName(cat.color)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(cat.icon),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            if (cat.isSystem) {
                                Text("Built-in", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                    if (!cat.isSystem) {
                        IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = DangerColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(viewModel: FinanceViewModel, onNavigateBack: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val incomeCats = categories.filter { it.type == "income" }
    val expenseCats = categories.filter { it.type == "expense" }

    Scaffold(
        containerColor = AppBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = AccentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Categories", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
            }
            Text(
                "Income and expense tags used across transactions and store purchases.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            CategoryGroup("Income", SuccessColor, incomeCats, viewModel)
            Spacer(modifier = Modifier.height(20.dp))
            CategoryGroup("Expense", DangerColor, expenseCats, viewModel)
        }
    }

    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("expense") }
        var catColor by remember { mutableStateOf(PRESET_CATEGORY_COLORS.first()) }
        var catIcon by remember { mutableStateOf(PRESET_CATEGORY_ICONS.first()) }

        StandardFormDialog(
            title = "Add Category",
            onDismiss = { showAddCategoryDialog = false },
            confirmLabel = "Save Category",
            confirmEnabled = catName.isNotBlank(),
            onConfirm = {
                viewModel.createCategory(catName, catType, catColor, catIcon)
                showAddCategoryDialog = false
            }
        ) {
            RequiredFieldLabel("Category Name", catName.isNotBlank())
            OutlinedTextField(
                value = catName,
                onValueChange = { catName = it },
                placeholder = { Text("e.g. Groceries") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("income", "expense").forEach { type ->
                    ElevatedFilterChip(
                        selected = catType == type,
                        onClick = { catType = type },
                        label = { Text(type.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Text("Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_CATEGORY_COLORS) { c ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(getColorForName(c))
                            .border(
                                width = if (catColor == c) 2.dp else 0.dp,
                                color = TextPrimary,
                                shape = CircleShape
                            )
                            .clickable { catColor = c },
                        contentAlignment = Alignment.Center
                    ) {
                        if (catColor == c) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Text("Icon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PRESET_CATEGORY_ICONS) { iconName ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (catIcon == iconName) AccentColor else BorderColor)
                            .clickable { catIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getIconForName(iconName), contentDescription = iconName, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
