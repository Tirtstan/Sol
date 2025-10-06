package com.std.sol


import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Create

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Filled.AddCircle)
    object Budgets : Screen("budgets", "Budgets", Icons.Filled.AccountBalanceWallet)
    object More : Screen("more", "More", Icons.Filled.MoreHoriz)

    object Welcome : Screen("welcome", "Welcome", Icons.Filled.Home)

    object Login : Screen("login", "Login", Icons.Filled.Home)
    object Register : Screen("register", "Register", Icons.Filled.Home)

    object AddEditBudget : Screen("add_edit_budget", "Budget Editor", Icons.Filled.Create)
}



