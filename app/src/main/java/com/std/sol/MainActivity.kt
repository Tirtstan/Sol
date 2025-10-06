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
// import androidx.compose.foundation.layout.safeContent
// import androidx.compose.foundation.layout.safeDrawing
// import androidx.compose.foundation.layout.safeGestures
// import androidx.compose.foundation.layout.waterfall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.std.sol.screens.AddEditBudgetScreen
import com.std.sol.databases.DatabaseProvider
import com.std.sol.screens.BudgetsScreen
import com.std.sol.screens.DashboardScreen
import com.std.sol.screens.LoginScreen
import com.std.sol.screens.MoreScreen
import com.std.sol.screens.RegisterScreen
import com.std.sol.screens.TransactionsScreen
import com.std.sol.screens.WelcomeScreen
import com.std.sol.ui.theme.SolTheme
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    SolTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B1426)
        ) {
            val context = LocalContext.current
           /* val viewModelFactory = ViewModelFactory(
                DatabaseProvider.getDatabase(context.applicationContext),
                SessionManager(context.applicationContext)
            )
            */
            val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
            val sessionManager = remember { SessionManager(context.applicationContext) }
            val factory = remember { ViewModelFactory(db, sessionManager) }
            val userViewModel: UserViewModel = viewModel(factory = factory)

            val isLoading by userViewModel.isLoading.collectAsState()
            val currentUser by userViewModel.currentUser.collectAsState()

            val navController = rememberNavController()

            //helper method, determine initial route once loading is complete
            LaunchedEffect(isLoading) {
                if(!isLoading) {
                    val startRoute = if (currentUser != null) {
                        Screen.Dashboard.route
                    } else {
                        Screen.Welcome.route
                    }
                    if (navController.currentDestination?.route == null) {
                        navController.navigate(startRoute) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true}
                            launchSingleTop = true
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    AppContent(navController, userViewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(navController: NavHostController, userViewModel: UserViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainScreens = listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Budgets.route,
        Screen.More.route
    )
    val showBottomBar = currentRoute in mainScreens

    Scaffold (
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            userViewModel = userViewModel,
            startDestination = "loading_placeholder",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
/*
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

    //determine correct start destination
    val startDestination = if (currentUser != null) {
        Screen.Dashboard.route
    } else {
        Screen.Register.route
    }
    // This effect will run once when App is first composed.
    // It will navigate to the correct initial screen.
    /*
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
     */

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
            //startDestination = Screen.Register.route,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

 */

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
        composable("loading_placeholder") {
            Box(Modifier.fillMaxSize())
        }

        composable(Screen.Welcome.route) { WelcomeScreen(navController) }

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

        composable (
            route = Screen.AddEditBudget.route + "/{budgetId}",
            arguments = listOf(navArgument("budgetId") {
                type = NavType.IntType
                defaultValue = 0
            }),
            enterTransition = {
                slideInVertically (initialOffsetY = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val budgetId = backStackEntry.arguments?.getInt("budgetId") ?: 0
            AddEditBudgetScreen(navController, userViewModel, budgetId)
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    MainApp()
}
