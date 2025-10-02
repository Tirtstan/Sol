package com.std.sol

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavController
import com.std.sol.screens.BudgetsScreen
import com.std.sol.screens.DashboardScreen
import com.std.sol.screens.MoreScreen
import com.std.sol.screens.TransactionsScreen

@Composable
fun NavScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard, Screen.Transactions, Screen.Budgets, Screen.More
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            }
                        })
                }
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(navController) }
            composable(Screen.Transactions.route) { TransactionsScreen(navController) }
            composable(Screen.Budgets.route) { BudgetsScreen(navController) }
            composable(Screen.More.route) { MoreScreen(navController) }
        }
    }
}


@Preview()
@Composable
fun NavScreenPreview() {
    NavScreen()
}


