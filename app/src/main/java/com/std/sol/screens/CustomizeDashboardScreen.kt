package com.std.sol.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.SessionManager
import com.std.sol.entities.User
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import com.std.sol.ui.theme.Indigo
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.Lime
import com.std.sol.viewmodels.CustomizeDashboardViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeDashboardScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }
    val transactionRepository = remember { TransactionRepository() }
    val categoryRepository = remember { CategoryRepository() }
    val budgetRepository = remember { BudgetRepository() }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember {
        ViewModelFactory(
            userRepository,
            transactionRepository,
            categoryRepository,
            budgetRepository,
            sessionManager
        )
    }

    //get user and ViewModel
    val user: User? by userViewModel.currentUser.collectAsState()
    val userId = user?.id ?: ""
    val viewModel: CustomizeDashboardViewModel = viewModel(factory = factory)

    //state for UI
    val allWidgets = viewModel.allWidgets
    val enabledWidgets = viewModel.enabledWidgets //this is the SnapshotStateList

    //load settings once the screen starts
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadSettings(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customise Dashboard", color = Ivory) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Indigo),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Ivory)
                    }
                },
                actions = {
                    //save button
                    IconButton(onClick = {
                        if (userId.isNotBlank()) {
                            viewModel.saveSettings(userId)
                        }
                        navController.popBackStack() //go to dashboard
                    }) {
                        Icon(Icons.Default.Done, "Save", tint = Lime)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allWidgets) { widget ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //format name to be more readable
                    val widgetName = widget.name
                        .replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.titlecase() }

                    Text(
                        text = widgetName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ivory
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = widget in enabledWidgets,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                //add to list
                                enabledWidgets.add(widget)
                            } else {
                                //remove from list
                                enabledWidgets.remove(widget)
                            }
                        }
                    )
                }
            }
        }
    }
}
