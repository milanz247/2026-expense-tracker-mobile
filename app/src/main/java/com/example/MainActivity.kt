package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_AUTH = "auth"
private const val ROUTE_APP = "app"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplashScreenVisible = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreenVisible }

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
                    val onboardingSeen = dataStoreManager.onboardingSeenFlow.firstOrNull() ?: false
                    startDestination = when {
                        !token.isNullOrBlank() -> ROUTE_APP
                        !onboardingSeen -> ROUTE_ONBOARDING
                        else -> ROUTE_AUTH
                    }
                    keepSplashScreenVisible = false
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // The splash screen (installSplashScreen) covers this entire resolution
                    // window, so there's nothing to draw here while startDestination is null.
                    if (startDestination != null) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = startDestination!!) {
                            composable(ROUTE_ONBOARDING) {
                                OnboardingScreen(
                                    onFinished = {
                                        scope.launch { dataStoreManager.setOnboardingSeen() }
                                        navController.navigate(ROUTE_AUTH) {
                                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                        }
                                    }
                                )
                            }
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
