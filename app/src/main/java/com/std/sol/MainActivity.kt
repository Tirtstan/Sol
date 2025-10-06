package com.std.sol

import android.content.Context
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.std.sol.components.StarryBackground
import com.std.sol.databases.DatabaseProvider
import com.std.sol.screens.BudgetsScreen
import com.std.sol.screens.DashboardScreen
import com.std.sol.screens.LoginScreen
import com.std.sol.screens.MoreScreen
import com.std.sol.screens.RegisterScreen
import com.std.sol.screens.TransactionsScreen
import com.std.sol.screens.WelcomeScreen
import com.std.sol.ui.theme.DeepSpaceBase
import com.std.sol.ui.theme.SolTheme
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSpaceBase
                ) {
                    AppScreen()
                }
            }
        }
    }
}
@Composable
fun AppScreen() {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val sessionManager = rememberSessionManager(context)
    val factory = ViewModelFactory(db, sessionManager)

    // UserViewModel is used at the top level to manage session/login state
    val userViewModel: UserViewModel = viewModel(factory = factory)
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define the start destination based on session state
    val startDestination = if (isLoading) {
        // A placeholder route while loading
        "loading"
    } else if (currentUser == null) {
        Screen.Welcome.route
    } else {
        Screen.Dashboard.route
    }

    // Effect to navigate once loading is complete
    LaunchedEffect(isLoading, currentUser) {
        if (!isLoading) {
            val destination = if (currentUser == null) {
                Screen.Welcome.route
            } else {
                Screen.Dashboard.route
            }
            // Navigate only if we are not already at the correct destination
            if (currentRoute != destination) {
                navController.navigate(destination) {
                    // This clears the back stack up to the initial destination
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Makes StarryBackground visible
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentUser != null && listOf(
                    Screen.Dashboard.route,
                    Screen.Transactions.route,
                    Screen.Budgets.route,
                    Screen.More.route
                ).contains(currentRoute)
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StarryBackground()

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AppNavHost(navController, userViewModel)
            }
        }
    }
}
/*
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

 */
@Composable
fun rememberSessionManager(context: Context): SessionManager {
    // Check if we are in Android Studio Preview mode
    val inPreview = LocalInspectionMode.current
    return remember(context, inPreview) {
        SessionManager(context.applicationContext)
    }
}

/*
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
fun AppNavHost(navController: androidx.navigation.NavHostController, userViewModel: UserViewModel) {
    val startDestination = Screen.Welcome.route // Will be overridden by LaunchedEffect if logged in
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // Auth Screens
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
    }
}

@Preview
@Composable
fun AppScreenPreview() {
    SolTheme {
        AppScreen()
    }
}
