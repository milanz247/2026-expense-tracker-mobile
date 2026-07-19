package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.DataStoreManager
import com.example.navigation.AppShell
import com.example.network.NetworkClient
import com.example.ui.AppViewModelFactory
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private const val ROUTE_AUTH = "auth"
private const val ROUTE_APP = "app"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStoreManager = DataStoreManager(applicationContext)
        val apiService = NetworkClient.getApiService(applicationContext, dataStoreManager)
        val viewModelFactory = AppViewModelFactory(apiService, dataStoreManager)

        setContent {
            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val scope = rememberCoroutineScope()

            MyApplicationTheme(themeMode = themeMode) {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(key1 = Unit) {
                    val token = dataStoreManager.tokenFlow.firstOrNull()
                    startDestination = if (token.isNullOrBlank()) ROUTE_AUTH else ROUTE_APP
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (startDestination == null) {
                        val colors = LocalAppColors.current
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.outline, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "F",
                                    color = colors.accent,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = startDestination!!) {
                            composable(ROUTE_AUTH) {
                                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                                AuthScreen(
                                    viewModel = authViewModel,
                                    onNavigateToDashboard = {
                                        navController.navigate(ROUTE_APP) {
                                            popUpTo(ROUTE_AUTH) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(ROUTE_APP) {
                                AppShell(
                                    apiService = apiService,
                                    dataStoreManager = dataStoreManager,
                                    themeMode = themeMode,
                                    onThemeModeChange = { mode ->
                                        scope.launch { dataStoreManager.saveThemeMode(mode) }
                                    },
                                    onLoggedOut = {
                                        navController.navigate(ROUTE_AUTH) {
                                            popUpTo(ROUTE_APP) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
