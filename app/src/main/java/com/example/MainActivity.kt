package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FinanceRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                val viewModel: FinanceViewModel by viewModels {
                    FinanceViewModelFactory(application, repository)
                }

                val authState by viewModel.authState.collectAsState()
                var currentScreen by remember { mutableStateOf("login") }
                var activeTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Ledger, 2: Debts, 3: Store Tabs, 4: Reports, 5: Settings
                var isNavigatingToSettings by remember { mutableStateOf(false) }
                var isNavigatingToCategories by remember { mutableStateOf(false) }

                val context = LocalContext.current

                // Listen to Share Sheet Uri emission from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.exportedUri.collect { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = if (uri.toString().endsWith(".csv")) "text/csv" else "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Statement"))
                    }
                }

                // Global toast channel for backend errors/confirmations from any screen
                LaunchedEffect(Unit) {
                    viewModel.events.collect { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                when (val state = authState) {
                    is AuthState.Unauthenticated, is AuthState.Error -> {
                        if (currentScreen == "register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    currentScreen = "dashboard"
                                    activeTab = 0
                                },
                                onNavigateToLogin = { currentScreen = "login" },
                                viewModel = viewModel
                            )
                        } else {
                            LoginScreen(
                                onLoginSuccess = {
                                    currentScreen = "dashboard"
                                    activeTab = 0
                                },
                                onNavigateToRegister = { currentScreen = "register" },
                                viewModel = viewModel
                            )
                        }
                    }
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF120A0C)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFE0263B))
                        }
                    }
                    is AuthState.Authenticated -> {
                        val showingSubScreen = isNavigatingToSettings || isNavigatingToCategories
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("main_scaffold"),
                            containerColor = Color(0xFF120A0C),
                            topBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF120A0C))
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "PLANETARY FINANCE",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE0263B)
                                            )
                                            Text(
                                                text = "Hello, ${state.profile.name}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF5EAEC)
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    if (showingSubScreen) {
                                                        isNavigatingToSettings = false
                                                        isNavigatingToCategories = false
                                                    } else {
                                                        isNavigatingToSettings = true
                                                    }
                                                },
                                                modifier = Modifier.testTag("settings_button")
                                            ) {
                                                Icon(
                                                    imageVector = if (showingSubScreen) Icons.Default.Close else Icons.Default.Settings,
                                                    contentDescription = "Settings",
                                                    tint = Color(0xFFB9A3A7)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            bottomBar = {
                                if (!showingSubScreen) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E1215))
                                            .navigationBarsPadding()
                                            .height(80.dp)
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(
                                            Triple("Overview", Icons.Default.Dashboard, 0),
                                            Triple("Ledger", Icons.Default.ReceiptLong, 1),
                                            Triple("Debts", Icons.Default.Handshake, 2),
                                            Triple("Store Tabs", Icons.Default.LocalMall, 3),
                                            Triple("Reports", Icons.Default.BarChart, 4)
                                        ).forEach { (label, icon, index) ->
                                            val isSelected = activeTab == index
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        activeTab = index
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(if (isSelected) Color(0xFF3A131A) else Color.Transparent)
                                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = label,
                                                        tint = if (isSelected) Color(0xFFFFC4CA) else Color(0xFFB9A3A7),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color(0xFFFFC4CA) else Color(0xFFB9A3A7),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (isNavigatingToCategories) {
                                    CategoriesScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { isNavigatingToCategories = false }
                                    )
                                } else if (isNavigatingToSettings) {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onNavigateToCategories = { isNavigatingToCategories = true }
                                    )
                                } else {
                                    when (activeTab) {
                                        0 -> DashboardScreen(
                                            viewModel = viewModel,
                                            onNavigateToTransactions = { activeTab = 1 }
                                        )
                                        1 -> TransactionsScreen(viewModel = viewModel)
                                        2 -> DebtsScreen(viewModel = viewModel)
                                        3 -> StoreTabsScreen(viewModel = viewModel)
                                        4 -> ReportsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
