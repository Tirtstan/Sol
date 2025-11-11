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
                        fontSize = 10.sp,
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
                colors = createNavigationBarItemColors()
            )
        }
    }
}

@Composable
private fun createNavigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Amber,
    selectedTextColor = Amber,
    unselectedIconColor = Ivory,
    unselectedTextColor = Ivory,
    indicatorColor = Color.Transparent
)

@Composable
fun BottomNavigationBarWithFAB(
    navController: NavController,
    onAddClick: () -> Unit
) {
    val items = listOf(
        Screen.Dashboard, Screen.Transactions, null, Screen.Budgets, Screen.More
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = Ivory
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEach { item ->
                if (item == null) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { },
                        enabled = false
                    )
                } else {
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
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
                        colors = createNavigationBarItemColors()
                    )
                }
            }
        }


        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .size(56.dp),
            containerColor = Amber,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
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