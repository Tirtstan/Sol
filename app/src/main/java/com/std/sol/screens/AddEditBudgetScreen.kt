package com.std.sol.screens

import android.adservices.adid.AdId
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.Screen
import com.std.sol.SessionManager
import com.std.sol.databases.DatabaseProvider
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    budgetId: Int //0 for new budget, >0 for editing
) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }

    //initialise require ViewModels
    val budgetViewModel: BudgetViewModel = viewModel (factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel (factory = factory)

    //MUST: add things

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(if (budgetId == 0) "Create New Budget" else "Edit Budget")},
                //add nav icon for going back
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        //using simple icon t ext for now update to Icons.AutoMirrored.Filled.ArrowBack
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background //make sure scaffold matches app theme
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Budget ID: $budgetId") //placeholder for form
        }
    }
}