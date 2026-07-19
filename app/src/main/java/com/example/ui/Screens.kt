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
import androidx.compose.ui.graphics.Color
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
                .background(Color(0xFF120A0C))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "App Logo",
                tint = Color(0xFFE0263B),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Planetary Finance",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5EAEC)
            )
            Text(
                text = "Secure Personal Finance Management System",
                fontSize = 14.sp,
                color = Color(0xFFB9A3A7),
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
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
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
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color(0xFFE0263B))
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onNavigateToRegister) {
                    Text("Create a new account instead", color = Color(0xFFE0263B))
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
                .background(Color(0xFF120A0C))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AppRegistration,
                contentDescription = "Register Logo",
                tint = Color(0xFFE0263B),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Join Planetary Finance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5EAEC)
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
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
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
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
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Default Currency (e.g. USD, EUR, LKR)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE0263B),
                    focusedLabelColor = Color(0xFFE0263B)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color(0xFFE0263B))
            } else {
                Button(
                    onClick = { viewModel.register(name, email, password, currency) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Register & Save Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign in with existing credentials", color = Color(0xFFE0263B))
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
    onNavigateToTransactions: () -> Unit
) {
    val report by viewModel.reportSummary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val currency = remember(authState) {
        (authState as? AuthState.Authenticated)?.profile?.currency ?: "USD"
    }

    var showAddWalletDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<LocalAccount?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshReports()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120A0C))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcoming Card with Net Balance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3A131A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "NET FINANCIAL POSITION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC4CA)
                )
                Text(
                    text = report?.netBalance?.amountCents?.let { formatCents(it, currency) } ?: "$0.00",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFC4CA)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Monthly Cash Inflow", fontSize = 11.sp, color = Color(0xFFFFC4CA).copy(alpha = 0.7f))
                        Text(
                            text = report?.totalIncome?.amountCents?.let { formatCents(it, currency) } ?: "$0.00",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Monthly Outflow", fontSize = 11.sp, color = Color(0xFFFFC4CA).copy(alpha = 0.7f))
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

        // Wallets & Accounts Horizontal List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wallets & Accounts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
            IconButton(onClick = { showAddWalletDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account", tint = Color(0xFFE0263B))
            }
        }
        if (accounts.isNotEmpty()) {
            Text(
                text = "Long-press a wallet to edit or archive it",
                fontSize = 10.sp,
                color = Color(0xFFB9A3A7)
            )
        }

        if (accounts.isEmpty()) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddWalletDialog = true }
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No wallets created yet. Tap '+' to configure.", color = Color(0xFFB9A3A7), fontSize = 14.sp)
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(accounts) { acc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215)),
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { accountToEdit = acc }
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = acc.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFFF5EAEC)
                            )
                            Column {
                                Text(
                                    text = formatCents(acc.balance, currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFE0263B)
                                )
                                Text(
                                    text = acc.type.uppercase(),
                                    fontSize = 10.sp,
                                    color = Color(0xFFB9A3A7)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions block
        Text("Quick Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Ledger Logs")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transaction Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
            TextButton(onClick = onNavigateToTransactions) {
                Text("View All", color = Color(0xFFE0263B))
            }
        }

        if (report?.recentTransactions.isNullOrEmpty()) {
            Text(
                text = "No recorded transactions found. Try making your first expense or inflow log.",
                fontSize = 13.sp,
                color = Color(0xFFB9A3A7),
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
                                    ?: Color(0xFF2C1A1E)
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
                            color = Color(0xFFF5EAEC)
                        )
                        Text(
                            text = tx.category?.name ?: tx.type.uppercase(),
                            fontSize = 11.sp,
                            color = Color(0xFFB9A3A7)
                        )
                    }
                    Text(
                        text = (if (tx.type == "expense") "-" else "+") + formatCents(tx.amount, currency),
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == "expense") Color(0xFFFF4655) else Color(0xFF34D399),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    if (showAddWalletDialog) {
        var walletName by remember { mutableStateOf("") }
        var walletType by remember { mutableStateOf("bank") }
        var initialBalance by remember { mutableStateOf("") }
        var creditLimit by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showAddWalletDialog = false },
            title = { Text("Configure New Wallet", color = Color(0xFFF5EAEC)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = walletName,
                        onValueChange = { walletName = it },
                        label = { Text("Wallet Name") }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("bank", "cash", "credit_card", "investment").forEach { type ->
                            ElevatedFilterChip(
                                selected = walletType == type,
                                onClick = { walletType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Initial Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    if (walletType == "credit_card") {
                        OutlinedTextField(
                            value = creditLimit,
                            onValueChange = { creditLimit = it },
                            label = { Text("Credit Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (walletName.isNotBlank()) {
                            viewModel.createAccount(
                                walletName,
                                walletType,
                                initialBalance.toDoubleOrNull() ?: 0.0,
                                creditLimit.toDoubleOrNull() ?: 0.0
                            )
                            showAddWalletDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Save Wallet", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWalletDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
    }

    if (accountToEdit != null) {
        val editing = accountToEdit!!
        var editName by remember(editing.id) { mutableStateOf(editing.name) }
        var editType by remember(editing.id) { mutableStateOf(editing.type) }

        AlertDialog(
            onDismissRequest = { accountToEdit = null },
            title = { Text("Edit Wallet", color = Color(0xFFF5EAEC)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Wallet Name") }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("bank", "cash", "credit_card", "investment").forEach { type ->
                            ElevatedFilterChip(
                                selected = editType == type,
                                onClick = { editType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateAccount(editing.id, editName, editType)
                            accountToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.deleteAccount(editing.id)
                        accountToEdit = null
                    }) {
                        Text("Archive Wallet", color = Color(0xFFFF4655))
                    }
                    TextButton(onClick = { accountToEdit = null }) {
                        Text("Cancel", color = Color(0xFFB9A3A7))
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
                containerColor = Color(0xFFE0263B),
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
                .background(Color(0xFF120A0C))
                .padding(16.dp)
        ) {
            Text("Ledger History", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF5EAEC))
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
                    Text("No transactions logged. Tap '+' to create.", color = Color(0xFFB9A3A7))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(txs) { tx ->
                        val matchingCat = categories.find { it.id == tx.categoryId }
                        val matchingWallet = accounts.find { it.id == tx.accountId }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215)),
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
                                        color = Color(0xFFF5EAEC)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = matchingWallet?.name ?: tx.type.uppercase(),
                                            fontSize = 11.sp,
                                            color = Color(0xFFB9A3A7)
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
                                    color = if (tx.type == "expense") Color(0xFFFF4655) else Color(0xFF34D399),
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

        AlertDialog(
            onDismissRequest = { showAddTxDialog = false },
            title = { Text("Log Transaction") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
                                label = { Text(type) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = fee,
                        onValueChange = { fee = it },
                        label = { Text("Fee") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text(if (txType == "transfer") "Source Account" else "Wallet Account Source")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(accounts) { acc ->
                            ElevatedFilterChip(
                                selected = selectedWalletId == acc.id,
                                onClick = { selectedWalletId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }

                    if (txType == "transfer") {
                        Text("Destination Account")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(accounts) { acc ->
                                ElevatedFilterChip(
                                    selected = selectedDestWalletId == acc.id,
                                    onClick = { selectedDestWalletId = acc.id },
                                    label = { Text(acc.name) }
                                )
                            }
                        }
                    }

                    if (txType != "transfer") {
                        Text("Category")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories.filter { it.type == txType }) { cat ->
                                ElevatedFilterChip(
                                    selected = selectedCatId == cat.id,
                                    onClick = { selectedCatId = cat.id },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description Memo") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = amount.toDoubleOrNull()
                        if (amountVal != null && selectedWalletId != null) {
                            val isTransfer = txType == "transfer"
                            viewModel.createTransaction(
                                type = txType,
                                amount = amountVal,
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
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Add Entry", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTxDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
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
                    containerColor = Color(0xFFE0263B),
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
                .background(Color(0xFF120A0C))
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
                    Text("Debt Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFF5EAEC))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(activeDebt.personName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
                        Text("Type: ${activeDebt.type.uppercase()}", fontSize = 13.sp, color = Color(0xFFB9A3A7))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total", fontSize = 11.sp, color = Color(0xFFB9A3A7))
                                Text(formatCents(activeDebt.totalAmount, currency), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Remaining", fontSize = 11.sp, color = Color(0xFFB9A3A7))
                                Text(
                                    text = formatCents(activeDebt.remainingAmount, currency),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (activeDebt.remainingAmount > 0) Color(0xFFFF4655) else Color(0xFF34D399)
                                )
                            }
                        }
                        if (activeDebt.remainingAmount > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showRepayDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                            ) {
                                Text("Record Repayment Settlement")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Repayment Transactions Log", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))

                if (repayments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No repayments documented yet.", color = Color(0xFFB9A3A7))
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
                                    Text(rep.date, fontSize = 11.sp, color = Color(0xFFB9A3A7))
                                }
                                Text(
                                    formatCents(rep.amount, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }

            } else {
                // List of Debts
                Text("Debts Ledger", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF5EAEC))
                Spacer(modifier = Modifier.height(16.dp))

                if (debts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active or completed debts on record.", color = Color(0xFFB9A3A7))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(debts) { d ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215)),
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
                                        Text(d.personName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))
                                        Text("Type: ${d.type.uppercase()} (Due: ${d.dueDate})", fontSize = 12.sp, color = Color(0xFFB9A3A7))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCents(d.remainingAmount, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (d.status == "settled") Color(0xFF34D399) else Color(0xFFFF4655)
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

        AlertDialog(
            onDismissRequest = { showAddDebtDialog = false },
            title = { Text("Log New Debt Balance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Counterparty Name") }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("lent", "borrowed").forEach { type ->
                            ElevatedFilterChip(
                                selected = debtType == type,
                                onClick = { debtType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("Amount Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text("Select Wallet Account")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts) { acc ->
                            ElevatedFilterChip(
                                selected = selectedWalletId == acc.id,
                                onClick = { selectedWalletId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date Target (YYYY-MM-DD)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = totalAmount.toDoubleOrNull()
                        if (personName.isNotBlank() && amount != null && selectedWalletId != null) {
                            viewModel.createDebt(
                                personName,
                                debtType,
                                amount,
                                selectedWalletId!!,
                                dueDate.ifEmpty { "2026-12-31" }
                            )
                            showAddDebtDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Add Debt Record", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDebtDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
    }

    if (showRepayDialog && activeDebt != null) {
        var repayAmount by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showRepayDialog = false },
            title = { Text("Record Partial/Full Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Remaining debt balance: ${formatCents(activeDebt.remainingAmount, currency)}")
                    OutlinedTextField(
                        value = repayAmount,
                        onValueChange = { repayAmount = it },
                        label = { Text("Repayment Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text("Target Wallet Account Source")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts) { acc ->
                            ElevatedFilterChip(
                                selected = selectedWalletId == acc.id,
                                onClick = { selectedWalletId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = repayAmount.toDoubleOrNull()
                        if (amount != null && selectedWalletId != null) {
                            viewModel.repayDebt(activeDebt.id, amount, selectedWalletId!!)
                            showRepayDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Submit Repayment", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepayDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
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
                    containerColor = Color(0xFFE0263B),
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
                .background(Color(0xFF120A0C))
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
                    Text("Store Tab Details", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFF5EAEC))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(activeCreditor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Outstanding Tab Debt: " + formatCents(activeCreditor.outstandingDebt, currency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFF4655)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showPurchaseDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                            ) {
                                Text("New Purchase", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { showSettleDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9A3A7))
                            ) {
                                Text("Settle/Pay Off", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tab Purchases Log", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))

                if (purchases.isEmpty()) {
                    Text("No purchases logged on this tab yet.", fontSize = 13.sp, color = Color(0xFFB9A3A7), modifier = Modifier.padding(top = 8.dp))
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
                                    Text(pur.date, fontSize = 11.sp, color = Color(0xFFB9A3A7))
                                }
                                Text(
                                    formatCents(pur.amount, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF4655)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Tab Settlements", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))

                if (settlements.isEmpty()) {
                    Text("No payments have been settled on this tab.", fontSize = 13.sp, color = Color(0xFFB9A3A7), modifier = Modifier.padding(top = 8.dp))
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
                                    Text(set.date, fontSize = 11.sp, color = Color(0xFFB9A3A7))
                                }
                                Text(
                                    formatCents(set.amountPaid, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    }
                }

            } else {
                Text("Shop Creditors (Tabs)", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF5EAEC))
                Spacer(modifier = Modifier.height(16.dp))

                if (creditors.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No shop creditor tabs logged.", color = Color(0xFFB9A3A7))
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(creditors) { cr ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215)),
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
                                        Text(cr.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))
                                        Text("Outstanding Tab: " + formatCents(cr.outstandingDebt, currency), fontSize = 12.sp, color = Color(0xFFB9A3A7))
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

        AlertDialog(
            onDismissRequest = { showAddCreditorDialog = false },
            title = { Text("Add Store Creditor Ledger") },
            text = {
                OutlinedTextField(
                    value = credName,
                    onValueChange = { credName = it },
                    label = { Text("Store / Merchant Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (credName.isNotBlank()) {
                            viewModel.createStoreCreditor(credName)
                            showAddCreditorDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Save Merchant", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCreditorDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
    }

    if (showPurchaseDialog && activeCreditor != null) {
        var purchaseAmount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedCatId by remember { mutableStateOf<Long?>(categories.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showPurchaseDialog = false },
            title = { Text("Record Tab Credit Purchase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchaseAmount,
                        onValueChange = { purchaseAmount = it },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Item Description") }
                    )
                    Text("Select Expense Category")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories.filter { it.type == "expense" }) { cat ->
                            ElevatedFilterChip(
                                selected = selectedCatId == cat.id,
                                onClick = { selectedCatId = cat.id },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = purchaseAmount.toDoubleOrNull()
                        if (amount != null && selectedCatId != null) {
                            viewModel.createPurchase(
                                activeCreditor.id,
                                amount,
                                0.0,
                                selectedCatId!!,
                                description
                            )
                            showPurchaseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Confirm Purchase", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
    }

    if (showSettleDialog && activeCreditor != null) {
        var payAmount by remember { mutableStateOf("") }
        var selectedWalletId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showSettleDialog = false },
            title = { Text("Settle Tab Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Outstanding Tab Debt: ${formatCents(activeCreditor.outstandingDebt, currency)}")
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Payment Amount Paid") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text("Select Source Account Source")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts) { acc ->
                            ElevatedFilterChip(
                                selected = selectedWalletId == acc.id,
                                onClick = { selectedWalletId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.toDoubleOrNull()
                        if (amount != null && selectedWalletId != null) {
                            viewModel.createSettlement(
                                activeCreditor.id,
                                selectedWalletId!!,
                                amount,
                                0.0
                            )
                            showSettleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Submit Settlement", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
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
            .background(Color(0xFF120A0C))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Financial Analytics & Statement", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF5EAEC))
        Spacer(modifier = Modifier.height(16.dp))

        // Donut / Pie Progress Representation for Category Expenses
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Category Expense Allocation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))
                Spacer(modifier = Modifier.height(12.dp))

                report?.categoryBreakdown?.forEach { entry ->
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
                            Text(entry.categoryName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${String.format(Locale.US, "%.1f", entry.percentage)}%", fontSize = 13.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (entry.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = Color(0xFF2C1A1E)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Cash Flow Chart (Simple column heights inside a row)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7-Month Cash Flow Trends", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    report?.cashFlow?.forEach { cf ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Income bar
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(
                                            (((cf.income.toDouble() / 1000000.0) * 100)
                                                .coerceIn(5.0, 100.0)).dp
                                        )
                                        .background(Color(0xFFE0263B), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )
                                // Expense bar
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(
                                            (((cf.expense.toDouble() / 1000000.0) * 100)
                                                .coerceIn(5.0, 100.0)).dp
                                        )
                                        .background(Color(0xFFFF4655), RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cf.month, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFE0263B)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inflow", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF4655)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Outflow", fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exporter Controls block
        Text("Generate Statements", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF5EAEC))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.exportTransactions("csv", null, null, null, null, null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV")
            }
            Button(
                onClick = { viewModel.exportTransactions("pdf", null, null, null, null, null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9A3A7))
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
fun SettingsScreen(viewModel: FinanceViewModel) {
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
            .background(Color(0xFF120A0C))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Profile & System Settings", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFF5EAEC))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Personal Parameters", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Update Personal Profile", color = Color.White)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Change Passcode Identity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
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

        CategoryManagementCard(viewModel)

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4655))
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Destroy Token & Terminate Session", color = Color.White)
        }
    }
}

@Composable
private fun CategoryManagementCard(viewModel: FinanceViewModel) {
    val categories by viewModel.categories.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val presetColors = listOf("emerald", "blue", "teal", "orange", "yellow", "slate", "violet", "red", "rose", "pink", "fuchsia", "sky")
    val presetIcons = listOf(
        "Wallet", "Utensils", "Car", "Home", "Clapperboard", "Zap", "HeartPulse",
        "ShoppingBag", "Gift", "Plane", "GraduationCap", "Briefcase", "Smartphone",
        "Dumbbell", "PawPrint", "Music", "Shirt", "Tag", "TrendingUp", "Coins"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1215))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Categories", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFF5EAEC))
                IconButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color(0xFFE0263B))
                }
            }

            if (categories.isEmpty()) {
                Text("No categories yet.", fontSize = 12.sp, color = Color(0xFFB9A3A7))
            } else {
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(getColorForName(cat.color)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconForName(cat.icon),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(cat.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF5EAEC))
                                Text(cat.type.uppercase(), fontSize = 9.sp, color = Color(0xFFB9A3A7))
                            }
                        }
                        if (!cat.isSystem) {
                            IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = Color(0xFFFF4655))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf("expense") }
        var catColor by remember { mutableStateOf(presetColors.first()) }
        var catIcon by remember { mutableStateOf(presetIcons.first()) }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Category") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("income", "expense").forEach { type ->
                            ElevatedFilterChip(
                                selected = catType == type,
                                onClick = { catType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    Text("Color", fontSize = 12.sp, color = Color(0xFFB9A3A7))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetColors) { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(getColorForName(c))
                                    .clickable { catColor = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (catColor == c) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Text("Icon", fontSize = 12.sp, color = Color(0xFFB9A3A7))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetIcons) { iconName ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (catIcon == iconName) Color(0xFFE0263B) else Color(0xFF2C1A1E))
                                    .clickable { catIcon = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(getIconForName(iconName), contentDescription = iconName, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.createCategory(catName, catType, catColor, catIcon)
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0263B))
                ) {
                    Text("Save Category", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Dismiss", color = Color(0xFFB9A3A7))
                }
            }
        )
    }
}
