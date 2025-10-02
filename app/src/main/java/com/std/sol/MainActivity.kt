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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.std.sol.databases.DatabaseProvider
import com.std.sol.screens.LoginScreen
import com.std.sol.screens.RegisterScreen
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
            val navController = rememberNavController()
            AppNavHost(navController)
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Register.route
) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(DatabaseProvider.getDatabase(context))

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            Screen.Register.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { 500 }, animationSpec = tween(500)) +
                        fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { -500 }, animationSpec = tween(500)) +
                        fadeOut(animationSpec = tween(500))
            }
        ) {
            val userViewModel: UserViewModel = viewModel(factory = viewModelFactory)
            RegisterScreen(navController, userViewModel)
        }

        composable(
            Screen.Login.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { -500 }, animationSpec = tween(500)) +
                        fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { 500 }, animationSpec = tween(500)) +
                        fadeOut(animationSpec = tween(500))
            }
        ) {
            val userViewModel: UserViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(navController, userViewModel)
        }

        composable(
            Screen.NavScreen.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { 500 }, animationSpec = tween(500)) +
                        fadeIn(animationSpec = tween(500))
            },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { 500 }, animationSpec = tween(500)) +
                        fadeOut(animationSpec = tween(500))
            }
        ) {
            NavScreen()
        }
    }
}

@Preview
@Composable
fun MainPreview() {
    Main()
}
