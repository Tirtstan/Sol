package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.SessionManager
import com.std.sol.Screen
import com.std.sol.components.StaggeredItem
import com.std.sol.databases.DatabaseProvider
import com.std.sol.entities.Budget
import com.std.sol.ui.theme.SolTheme
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.ui.text.font.FontWeight
import com.std.sol.ui.theme.Leaf
import com.std.sol.ui.theme.Ember
import com.std.sol.ui.theme.Ocean
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(navController: NavController, userViewModel: UserViewModel)
{
    val context = LocalContext.current

    //view model initialisation
    val db = remember { DatabaseProvider.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val factory = remember { ViewModelFactory(db, sessionManager) }
    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)

    //state collection
    val currentUser by userViewModel.currentUser.collectAsState()
    val userId = currentUser?.id

    //fetch budgets based on userId
    val budgetsFlow = remember(userId) {
        if (userId != null) budgetViewModel.getAllBudgets(userId) else emptyFlow()
    }
    val budgets by budgetsFlow.collectAsState(initial = emptyList())

    //UI imple
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
            floatingActionButton = {
                //plus button to add new budget accounts
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEditBudget.createRoute(0)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Budget")
                }
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                ) {
                    if (userId == null) {
                        Text(
                            text = "Please log in to view budgets.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (budgets.isEmpty()) {
                        Text(
                            text = "No budgets found. Tap '+' to add a new one.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(budgets) { budget ->
                                BudgetListItem(budget = budget, onClick = {
                                    // Navigate to the Add/Edit screen with the existing budget's ID
                                    navController.navigate(Screen.AddEditBudget.createRoute(budget.id))
                                })
                            }
                        }
                    }
                }
            }
    )
}
//reusable comp: current balance card
@Composable
fun BudgetCard(currentBalance: Double){
    StaggeredItem(index = 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                )
                Text(
                    text = "$%.2f".format(currentBalance),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (currentBalance >= 0) Leaf else Ember
                )
            }
        }
    }
}

//resuable comp: Budget List
@Composable
fun BudgetCategoryList(budgets: List<Budget>) {
    Text(
        text = "Budget Categories",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        itemsIndexed(budgets) { index, budget ->
            StaggeredItem(index = index + 1) {
                BudgetListItem(budget) {
                    //MUST ADD: implement edit budget screen
                    println("Opening edit screen for budget: ${budget.name}")
                }
            }
        }
    }
}

//reusable comp: Single budget item
@Composable
fun BudgetListItem(budget: Budget, onClick: () -> Unit) {
    val isUnderGoal = budget.currentAmount < budget.minGoalAmount
    val isOverGoal = budget.currentAmount > budget.maxGoalAmount

    //determine status colour
    val statusColor = when {
        isOverGoal -> Ember //red for overspent (exceeds maxGoalAmount)
        isUnderGoal -> Ocean //blue for under goal (below minGoalAmount)
        else -> Leaf //green for on track
    }

    //date formatting logic
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val dateRange = remember(budget.startDate, budget.endDate) {
        "${dateFormat.format(budget.startDate)} - ${dateFormat.format(budget.endDate)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Spent: $%.2f | Goal: $%.2f - $%.2f".format(
                        budget.currentAmount,
                        budget.minGoalAmount, // NEW FIELD
                        budget.maxGoalAmount  // NEW FIELD
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
           Text(
               text = when {
                   isOverGoal -> "OVERSPENT"
                   isUnderGoal -> "UNDER GOAL"
                   else -> "ON TRACK"
               },
               style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
               color = statusColor
           )
        }
    }
}