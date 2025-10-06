package com.std.sol

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp, // UPDATED: Made text smaller (was default ~12sp)
                        fontWeight = FontWeight.Medium
                    )
                },
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

// UPDATED: Bottom Navigation with Circular Add Button (adjusted spacing and positioning)
@Composable
fun BottomNavigationBarWithFAB(
    navController: NavController,
    onAddClick: () -> Unit
) {
    val items = listOf(
        Screen.Dashboard, Screen.Transactions, Screen.Budgets, Screen.More
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Navigation bar with adjusted spacing to accommodate the FAB
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Ivory,
            modifier = Modifier.padding(horizontal = 16.dp) // Add padding to make space for FAB
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Dashboard - normal spacing
            NavigationBarItem(
                icon = { Icon(items[0].icon, contentDescription = items[0].label) },
                label = {
                    Text(
                        text = items[0].label,
                        fontSize = 10.sp, // UPDATED: Made text smaller
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = currentRoute == items[0].route,
                onClick = {
                    if (currentRoute != items[0].route) {
                        navController.navigate(items[0].route) {
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

            // Transactions - wider spacing for FAB
            NavigationBarItem(
                icon = { Icon(items[1].icon, contentDescription = items[1].label) },
                label = {
                    Text(
                        text = items[1].label,
                        fontSize = 10.sp, // UPDATED: Made text smaller
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = currentRoute == items[1].route,
                onClick = {
                    if (currentRoute != items[1].route) {
                        navController.navigate(items[1].route) {
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
                ),
                modifier = Modifier.padding(end = 20.dp) // Extra space for FAB
            )

            // Budgets - wider spacing for FAB
            NavigationBarItem(
                icon = { Icon(items[2].icon, contentDescription = items[2].label) },
                label = {
                    Text(
                        text = items[2].label,
                        fontSize = 10.sp, // UPDATED: Made text smaller
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = currentRoute == items[2].route,
                onClick = {
                    if (currentRoute != items[2].route) {
                        navController.navigate(items[2].route) {
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
                ),
                modifier = Modifier.padding(start = 20.dp) // Extra space for FAB
            )

            // More - normal spacing
            NavigationBarItem(
                icon = { Icon(items[3].icon, contentDescription = items[3].label) },
                label = {
                    Text(
                        text = items[3].label,
                        fontSize = 10.sp, // UPDATED: Made text smaller
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = currentRoute == items[3].route,
                onClick = {
                    if (currentRoute != items[3].route) {
                        navController.navigate(items[3].route) {
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

        // Floating Action Button - moved down and icon changed to white
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp) // Moved down (was -28dp, now -16dp)
                .size(56.dp),
            containerColor = Amber,
            contentColor = Color.White, // White plus icon
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
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

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0C1327
)
@Composable
fun BottomNavigationBarWithFABPreview() {
    SolTheme() {
        BottomNavigationBarWithFAB(
            navController = rememberNavController(),
            onAddClick = {}
        )
    }
}