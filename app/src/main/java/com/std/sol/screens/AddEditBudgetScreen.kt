package com.std.sol.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.SessionManager
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.databases.DatabaseProvider
import com.std.sol.entities.Budget
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    navController: NavController,
    budgetId: Int //0 for new budget, >0 for editing
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }

    //initialise require ViewModels
    val budgetViewModel: BudgetViewModel = viewModel (factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel (factory = factory)
    val userIdFlow = sessionManager.userIdFlow.collectAsState(initial = null)
    val userId = userIdFlow.value ?: 0

    val isEditing = budgetId != 0
    val title = if (isEditing) "Edit Budget #$budgetId" else "Create New Budget"

    // Form state variables
    var budgetName by rememberSaveable { mutableStateOf("") }
    var goalAmount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(isEditing) }

    val formIsValid = budgetName.isNotBlank() && goalAmount.toDoubleOrNull() != null && goalAmount.toDouble() > 0

    // Load existing budget data for editing
    LaunchedEffect(budgetId, userId) {
        if (isEditing && userId != 0) {
            val budget = budgetViewModel.getBudgetById(budgetId)

            budget?.let {
                budgetName = it.name
                // This sets the goalAmount STATE VARIABLE (String) from the Budget entity (Double)
                goalAmount = "%.2f".format(it.maxGoalAmount)
                description = it.description ?: ""
            } ?: run {
                // Budget not found, navigate back
                navController.popBackStack()
            }
        }
        isLoading = false
    }
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SpaceTextField(
                        value = budgetName,
                        onValueChange = { budgetName = it },
                        label = "Budget Name",
                        placeholder = "e.g., Monthly Groceries"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SpaceTextField(
                        value = goalAmount,
                        onValueChange = { goalAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = "Goal Amount (\$)",
                        placeholder = "500.00",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SpaceTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description (Optional)",
                        placeholder = "Budget for household spending.",
                        singleLine = false
                    )
                    // Note: Category selection UI is omitted for brevity but would go here
                }

                SpaceButton(
                    text = if (isEditing) "Save Changes" else "Create Budget",
                    onClick = {
                        val amount = goalAmount.toDoubleOrNull() ?: return@SpaceButton
                        scope.launch {
                            val newBudget = Budget(
                                id = if (isEditing) budgetId else 0, // Ensure ID is correct for update
                                userId = userId,
                                categoryId = 1, // Placeholder
                                name = budgetName,
                                description = description.ifBlank { null },
                                currentAmount = 0.0, // Should be calculated in real logic, but 0 for new
                                minGoalAmount = amount,
                                maxGoalAmount = amount,
                                startDate = Date(), // Placeholder
                                endDate = Date() // Placeholder
                            )

                            if (isEditing) {
                                budgetViewModel.updateBudget(newBudget)
                            } else {
                                budgetViewModel.addBudget(newBudget)
                            }
                            navController.popBackStack()
                        }
                    },
                    enabled = formIsValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}