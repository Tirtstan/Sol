package com.std.sol.screens

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Budgets : Screen("budgets")
    object AddEditBudget : Screen("add_edit_budget")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object AddCategory : Screen("add_category")
    object More : Screen("more")
    object CustomizeDashboard : Screen("customize_dashboard")
}