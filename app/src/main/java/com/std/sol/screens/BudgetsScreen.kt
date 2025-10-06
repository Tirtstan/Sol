package com.std.sol.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.Screen
import com.std.sol.SessionManager
import com.std.sol.components.BudgetComponent
import com.std.sol.databases.DatabaseProvider
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(navController: NavController, userViewModel: UserViewModel) {
    // 1. Initialise ViewModels & Dependencies
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }
    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)

    // 2. Fetch data (current user and budgets)
    val currentUser by userViewModel.currentUser.collectAsState()

    // Fetch budgets for the current user ID. 
    val budgets by budgetViewModel.getAllBudgets(
        userId = currentUser?.id ?: 0,
        descending = true // Show newest/latest first
    ).collectAsState(initial = emptyList())


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Budgets") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to the AddEditBudget route with budgetId=0 for a NEW budget
                    navController.navigate("${Screen.AddEditBudget.route}/0")
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Budget")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content based on state
            if (currentUser == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please log in to view budgets.")
                }
            } else if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No budgets set up. Tap the '+' to create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Final Budget List UI
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(budgets) { budget ->
                        BudgetComponent(
                            budget = budget,
                            onClick = {
                                // Navigate to the AddEditBudget route for EDITING the specific budget
                                navController.navigate("${Screen.AddEditBudget.route}/${budget.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}