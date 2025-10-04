package com.std.sol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.waterfall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.std.sol.databases.DatabaseProvider
import com.std.sol.screens.AddTransactionScreen
import com.std.sol.screens.BudgetsScreen
import com.std.sol.screens.DashboardScreen
import com.std.sol.screens.LoginScreen
import com.std.sol.screens.MoreScreen
import com.std.sol.screens.RegisterScreen
import com.std.sol.screens.TransactionsScreen
import com.std.sol.ui.theme.SolTheme
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main()
        }
    }
}

@Composable
fun Main() {
    SolTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B1426)
        ) {
            val context = LocalContext.current
            val viewModelFactory = ViewModelFactory(
                DatabaseProvider.getDatabase(context.applicationContext),
                SessionManager(context.applicationContext)
            )
            val userViewModel: UserViewModel = viewModel(factory = viewModelFactory)
            val isLoading by userViewModel.isLoading.collectAsState()

            if (isLoading && !LocalInspectionMode.current) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                App(userViewModel)
            }
        }
    }
}

@Composable
fun App(userViewModel: UserViewModel) {
    val navController = rememberNavController()
    val currentUser by userViewModel.currentUser.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Budgets.route,
        Screen.More.route
    )
    val showBottomBar = currentRoute in mainScreens

    // This effect will run once when App is first composed.
    // It will navigate to the correct initial screen.
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        // This ensures content can go edge-to-edge behind the system bars.
        contentWindowInsets = WindowInsets.waterfall
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            userViewModel = userViewModel,
            startDestination = Screen.Register.route,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            Screen.Register.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { 500 },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -500 },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) {
            RegisterScreen(navController, userViewModel)
        }

        composable(
            Screen.Login.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { -500 },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { 500 },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) {
            LoginScreen(navController, userViewModel)
        }

        // Main App Screens
        composable(Screen.Dashboard.route) { DashboardScreen(navController, userViewModel) }
        composable(Screen.Transactions.route) { TransactionsScreen(navController, userViewModel) }
        composable(Screen.Budgets.route) { BudgetsScreen(navController, userViewModel) }
        composable(Screen.More.route) { MoreScreen(navController, userViewModel) }
        composable("add_transaction") { AddTransactionScreen(navController, userViewModel)
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    Main()
}

