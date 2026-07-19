package com.example.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.ui.AppViewModelFactory
import com.example.ui.about.AboutScreen
import com.example.ui.categories.CategoriesScreen
import com.example.ui.categories.CategoriesViewModel
import com.example.ui.common.FloatingBottomNavBar
import com.example.ui.common.FloatingNavItem
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.data.ExportScreen
import com.example.ui.debts.DebtsScreen
import com.example.ui.debts.DebtsViewModel
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profile.ProfileViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.statistics.StatisticsScreen
import com.example.ui.storetabs.StoreDetailScreen
import com.example.ui.storetabs.StoreTabsScreen
import com.example.ui.storetabs.StoreTabsViewModel
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.ThemeMode
import com.example.ui.transactions.TransactionsScreen
import com.example.ui.transactions.TransactionsViewModel
import com.example.ui.wallets.WalletsScreen
import com.example.ui.wallets.WalletsViewModel

private object Routes {
    const val DASHBOARD = "dashboard"
    const val WALLETS = "wallets"
    const val WALLET_DETAIL = "wallet_detail/{accountId}"
    const val TRANSACTIONS = "transactions"
    const val CATEGORIES = "categories"
    const val DEBTS = "debts"
    const val STORE_TABS = "storetabs"
    const val STORE_TAB_DETAIL = "storetab_detail/{creditorId}"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val STATISTICS = "statistics"
    const val NOTIFICATIONS = "notifications"
    const val ABOUT = "about"
    const val EXPORT = "export"
}

private val bottomItems = listOf(
    FloatingNavItem(Routes.DASHBOARD, "Home", Icons.Default.Home),
    FloatingNavItem(Routes.WALLETS, "Wallets", Icons.Default.AccountBalanceWallet),
    FloatingNavItem(Routes.DEBTS, "Debts", Icons.Default.Receipt),
    FloatingNavItem(Routes.STORE_TABS, "Tabs", Icons.Default.Store),
    FloatingNavItem(Routes.PROFILE, "Profile", Icons.Default.Person)
)

/** The authenticated part of the app: bottom-nav shell + every feature screen. */
@Composable
fun AppShell(
    apiService: ApiService,
    dataStoreManager: DataStoreManager,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLoggedOut: () -> Unit
) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(apiService, dataStoreManager)
    val colors = LocalAppColors.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 }
            ) {
                FloatingBottomNavBar(
                    items = bottomItems,
                    selectedRoute = currentRoute,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { it / 24 }) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { it / 24 }) }
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(
                    viewModel = vm,
                    onSeeAllTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                    onAddTransaction = { navController.navigate(Routes.TRANSACTIONS) },
                    onSearch = { navController.navigate(Routes.SEARCH) },
                    onSeeStatistics = { navController.navigate(Routes.STATISTICS) }
                )
            }
            composable(Routes.SEARCH) {
                val vm: TransactionsViewModel = viewModel(factory = factory)
                SearchScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.STATISTICS) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                StatisticsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.EXPORT) {
                val vm: TransactionsViewModel = viewModel(factory = factory)
                ExportScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.WALLETS) {
                val vm: WalletsViewModel = viewModel(factory = factory)
                WalletsScreen(
                    viewModel = vm,
                    onWalletClick = { id -> navController.navigate("wallet_detail/$id") }
                )
            }
            composable(
                route = Routes.WALLET_DETAIL,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) { entry ->
                val accountId = entry.arguments?.getLong("accountId") ?: return@composable
                val vm: TransactionsViewModel = viewModel(factory = factory)
                TransactionsScreen(
                    viewModel = vm,
                    fixedAccountId = accountId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.TRANSACTIONS) {
                val vm: TransactionsViewModel = viewModel(factory = factory)
                TransactionsScreen(viewModel = vm, fixedAccountId = null, onBack = null)
            }
            composable(Routes.CATEGORIES) {
                val vm: CategoriesViewModel = viewModel(factory = factory)
                CategoriesScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Routes.DEBTS) {
                val vm: DebtsViewModel = viewModel(factory = factory)
                DebtsScreen(viewModel = vm)
            }
            composable(Routes.STORE_TABS) {
                val vm: StoreTabsViewModel = viewModel(factory = factory)
                StoreTabsScreen(
                    viewModel = vm,
                    onShopClick = { id -> navController.navigate("storetab_detail/$id") }
                )
            }
            composable(
                route = Routes.STORE_TAB_DETAIL,
                arguments = listOf(navArgument("creditorId") { type = NavType.LongType })
            ) { entry ->
                val creditorId = entry.arguments?.getLong("creditorId") ?: return@composable
                val vm: StoreTabsViewModel = viewModel(factory = factory)
                StoreDetailScreen(
                    viewModel = vm,
                    creditorId = creditorId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                val vm: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = vm,
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onExportData = { navController.navigate(Routes.EXPORT) },
                    onAbout = { navController.navigate(Routes.ABOUT) },
                    onLoggedOut = onLoggedOut
                )
            }
        }
    }
}
