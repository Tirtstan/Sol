package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.SessionManager // From SessionManager.kt
import com.std.sol.databases.DatabaseProvider // From DatabaseProvider.kt
import com.std.sol.entities.Budget // From Budget.kt
import com.std.sol.entities.TransactionType // From TransactionType.kt
import com.std.sol.viewmodels.BudgetViewModel // From BudgetViewModel.kt
import com.std.sol.viewmodels.TransactionViewModel // From TransactionViewModel.kt
import com.std.sol.viewmodels.UserViewModel // From UserViewModel.kt
import com.std.sol.viewmodels.ViewModelFactory // From ViewModelFactory.kt
import com.std.sol.components.StarryBackground // From StarryBackground.kt
import com.std.sol.components.StaggeredItem // From StaggeredItem.kt
import com.std.sol.ui.theme.Leaf
import com.std.sol.ui.theme.Ember
import com.std.sol.ui.theme.Ocean
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    userViewModel: UserViewModel
)
{
    //dependencies & viewModels setup
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }

    val factory = remember { ViewModelFactory(db, SessionManager(context)) }

    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val transactionViewModel: TransactionViewModel = viewModel(factory = factory)

    //data state fetching
    val currentUser by userViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: return //exit if user is not logged in

    //fetch all active budgets for user
    val transactions by transactionViewModel
        .getAllTransactions(userId, descending = true)
        .collectAsState(initial = emptyList())

    val budgets by budgetViewModel
        .getAllBudgets(userId)
        .collectAsState(initial = emptyList())

    //calc current balance(wallet)
    val currentBalance = transactions.sumOf{
        when (it.type)
        {
           TransactionType.INCOME -> it.amount
           TransactionType.EXPENSE -> -it.amount
        }
    }

    //UI imple
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet", style = MaterialTheme.typography.titleLarge)},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
            floatingActionButton = {
                //plus button to add new budget accounts
                FloatingActionButton(onClick = {
                    //MUST ADD: define new route(to add/edit budget screen) & use navController.navigate
                    println("Navigate to Add/Edit Budget Screen")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add New Budget")
                }
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                ) {
                    StarryBackground()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal =16.dp)
                    ) {
                        //current balance
                        BudgetCard(currentBalance)
                        Spacer(modifier = Modifier.height(8.dp))

                        //budget categories list
                        BudgetCategoryList(budgets)
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