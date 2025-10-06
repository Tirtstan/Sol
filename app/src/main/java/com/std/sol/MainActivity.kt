package com.std.sol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.std.sol.components.AddOptionsDialog
import com.std.sol.components.StarryBackground
import com.std.sol.databases.DatabaseProvider
import com.std.sol.screens.AddTransactionScreen
import com.std.sol.screens.BudgetsScreen
import com.std.sol.screens.DashboardScreen
import com.std.sol.screens.LoginScreen
import com.std.sol.screens.MoreScreen
import com.std.sol.screens.RegisterScreen
import com.std.sol.screens.TransactionsScreen
import com.std.sol.ui.theme.AuthGradient
import com.std.sol.ui.theme.MoreScreenGradient
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

    val authScreens = listOf(Screen.Login.route, Screen.Register.route)
    val mainScreens = listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Budgets.route,
        Screen.More.route
    )
    val showBottomBar = currentRoute in mainScreens

    // Add options dialog state
    var showAddOptionsDialog by remember { mutableStateOf(false) }

    val targetGradient = if (currentRoute in authScreens) AuthGradient else MoreScreenGradient

    val animatedGradientColors = List(targetGradient.size) { index ->
        animateColorAsState(
            targetValue = targetGradient[index],
            animationSpec = tween(durationMillis = 1000),
            label = "gradient_color_$index"
        ).value
    }

    // This effect handles the initial navigation logic
    LaunchedEffect(currentUser) {
        val startRoute = if (currentUser != null) Screen.Dashboard.route else Screen.Register.route
        navController.navigate(startRoute) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = animatedGradientColors))
    ) {
        StarryBackground()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    // UPDATED: Use BottomNavigationBarWithFAB instead of BottomNavigationBar
                    BottomNavigationBarWithFAB(
                        navController = navController,
                        onAddClick = { showAddOptionsDialog = true }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                userViewModel = userViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    // Add Options Dialog
    if (showAddOptionsDialog) {
        AddOptionsDialog(
            onDismiss = { showAddOptionsDialog = false },
            onAddTransaction = {
                navController.navigate("add_transaction")
            },
            onAddCategory = {
                // For now, navigate to add transaction (you can create AddCategoryScreen later)
                navController.navigate("add_category")
            }
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    val animationSpec1 = tween<IntOffset>(durationMillis = 400)
    val animationSpec2 = tween<Float>(durationMillis = 400)

    NavHost(
        navController = navController,
        startDestination = Screen.Register.route,
        modifier = modifier
    ) {
        val authEnterTransition =
            slideInVertically(animationSpec = animationSpec1) { it } + fadeIn(animationSpec2)
        val authExitTransition =
            slideOutVertically(animationSpec = animationSpec1) { it } + fadeOut(animationSpec2)

        composable(
            route = Screen.Register.route,
            enterTransition = { authEnterTransition },
            exitTransition = { authExitTransition }
        ) { RegisterScreen(navController, userViewModel) }

        composable(
            route = Screen.Login.route,
            enterTransition = { authEnterTransition },
            exitTransition = { authExitTransition }
        ) { LoginScreen(navController, userViewModel) }

        val mainEnterTransition =
            slideInHorizontally(animationSpec = animationSpec1) { it } + fadeIn(animationSpec2)
        val mainExitTransition =
            slideOutHorizontally(animationSpec = animationSpec1) { -it } + fadeOut(animationSpec2)
        val mainPopEnterTransition =
            slideInHorizontally(animationSpec = animationSpec1) { -it } + fadeIn(animationSpec2)
        val mainPopExitTransition =
            slideOutHorizontally(animationSpec = animationSpec1) { it } + fadeOut(animationSpec2)

        composable(
            route = Screen.Dashboard.route,
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainPopEnterTransition },
            popExitTransition = { mainPopExitTransition }
        ) { DashboardScreen(navController, userViewModel) }

        composable(
            route = Screen.Transactions.route,
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainPopEnterTransition },
            popExitTransition = { mainPopExitTransition }
        ) { TransactionsScreen(navController, userViewModel) }

        composable(
            route = Screen.Budgets.route,
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainPopEnterTransition },
            popExitTransition = { mainPopExitTransition }
        ) { BudgetsScreen(navController, userViewModel) }

        composable(
            route = Screen.More.route,
            enterTransition = { mainEnterTransition },
            exitTransition = { mainExitTransition },
            popEnterTransition = { mainPopEnterTransition },
            popExitTransition = { mainPopExitTransition }
        ) { MoreScreen(navController, userViewModel) }

        // Add Transaction Screen
        composable("add_transaction") {
            AddTransactionScreen(
                navController = navController,
                userViewModel = userViewModel,
                onClose = { navController.navigateUp() }
            )
        }

        // Add Category Screen (placeholder for now)
        composable("add_category") {
            // TODO: Create AddCategoryScreen
            // For now, navigate to add transaction as placeholder
            AddTransactionScreen(
                navController = navController,
                userViewModel = userViewModel,
                onClose = { navController.navigateUp() }
            )
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    SolTheme(darkTheme = true) { Main() }
}