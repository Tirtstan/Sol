package com.std.sol

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreHoriz

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Filled.AddCircle)
    object Budgets : Screen("budgets", "Budgets", Icons.Filled.AccountBalanceWallet)
    object More : Screen("more", "More", Icons.Filled.MoreHoriz)
}

@Composable
fun DashboardScreen() {
    Text("Dashboard")
}

@Composable
fun TransactionsScreen() {
    Text("Transactions")
}

@Composable
fun BudgetsScreen() {
    Text("Budgets")
}

@Composable
fun MoreScreen() {
    Text("More / Settings")
}
