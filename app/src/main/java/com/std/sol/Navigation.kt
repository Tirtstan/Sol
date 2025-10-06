package com.std.sol

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.std.sol.ui.theme.Amber
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SolTheme

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Dashboard, Screen.Transactions, Screen.Budgets, Screen.More
    )
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = Ivory
    ) {
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
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Amber,
                    selectedTextColor = Amber,
                    unselectedIconColor = Ivory,
                    unselectedTextColor = Ivory,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0C1327
)
@Composable
fun BottomNavigationBarPreview() {
    SolTheme() { BottomNavigationBar(navController = rememberNavController()) }
}
