package com.std.sol.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.std.sol.Screen
import com.std.sol.components.BudgetComponent
import com.std.sol.entities.Category
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SpaceMonoFont
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.entities.Budget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import com.std.sol.entities.Transaction
import com.std.sol.viewmodels.TransactionViewModel
import androidx.compose.foundation.Canvas
import com.std.sol.ui.theme.SpaceMonoFont

enum class DashboardWidgetType{
    RECENT_BUDGETS,
    CATEGORY_SUMMARY_CIRCLE,
    RECENT_TRANSACTIONS
    //SAVING_GOALS
}

@Composable
fun RecentBudgetsWidget(
    navController: NavController,
    budgetViewModel: BudgetViewModel,
    categoryViewModel: CategoryViewModel,
    userId: String,
    onEditBudget: (String) -> Unit //new callback
) {
    val budgets by budgetViewModel.getAllBudgets(
        userId = userId,
        descending = true
    ).collectAsState(initial = emptyList<Budget>())

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList<Category>())

    val recentBudgets = remember(budgets) {
        budgets.take(3)
    }

    Column {
        if (recentBudgets.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Budgets",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = SpaceMonoFont,
                    color = Color(0xFFFFFDF0)
                )
                TextButton(
                    onClick = {navController.navigate(Screen.Budgets.route)}
                ) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color(0xFFF4C047)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            //lazy column to a simple column, as widget is a child to LazyColumn on the dashboard. cannot nest scrollable items in the same direction

            Column (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentBudgets.forEach { budget ->
                    val category = categories.find { it.id == budget.categoryId }
                    BudgetItemWidget(
                        budget = budget,
                        category = category,
                        budgetViewModel = budgetViewModel,
                        userId = userId,
                        onNavigate = {
                            onEditBudget(budget.id)
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No budgets yet. Create one to get started!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ivory,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("${Screen.AddEditBudget.route}/0") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFf4c047),
                            contentColor = Color(0xFF0c1327)
                        )
                    ) {
                        Text(
                            "Create Budget",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItemWidget(
    budget: Budget,
    category: Category?,
    budgetViewModel: BudgetViewModel,
    userId: String,
    onNavigate: () -> Unit
) {
    // Update current amount when budget changes
    LaunchedEffect(budget.id, budget.categoryId, budget.startDate, budget.endDate) {
        budgetViewModel.updateCurrentAmount(
            userId = userId,
            categoryId = budget.categoryId,
            start = budget.startDate,
            end = budget.endDate
        )
    }

    val currentAmount by budgetViewModel.currentAmount.collectAsState()

    BudgetComponent(
        budget = budget,
        category = category,
        currentAmount = currentAmount,
        onClick = onNavigate
    )
}

@Composable
fun RecentTransactionWidget(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    userId: String
) {
    //fetch recent transactions
    val recentTransactions by transactionViewModel.getRecentTransactions(userId)
        .collectAsState(initial = emptyList())

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = SpaceMonoFont,
                color = Color(0xFFFFFDF0)
            )
            TextButton(
                onClick = { navController.navigate(Screen.Transactions.route) }
            ) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFFF4C047)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (recentTransactions.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentTransactions.forEach { transaction ->
                    //can create TransactionItem composable, but for now, display text
                    TransactionItemWidget(transaction = transaction)
                }
            }
        } else {
            Text(
                "No transactions recorded yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = Ivory,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )
        }
    }
}

//helper composable for transaction item
@Composable
fun TransactionItemWidget(transaction: com.std.sol.entities.Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transaction.name,
            style = MaterialTheme.typography.bodyMedium,
            color = Ivory
        )

        val transactionTypeString = transaction.type
        val isExpense = transactionTypeString == com.std.sol.entities.TransactionType.EXPENSE.name
        Text(
            text = "€${"%.2f".format(transaction.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) Color.Red else Color.Green
        )
    }
}

@Composable
fun ExpenseCircle(
    totalSpent: Double,
    categoryColor: Color? = null
) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth: Float = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = center

            if (categoryColor != null) {
                drawCircle(
                    color = categoryColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFf4680b),
                            Color(0xFFf4c047),
                            Color(0xFFb42313),
                            Color(0xFFf45d92),
                            Color(0xFFf4680b)
                        ),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "R${String.format("%.2f", totalSpent)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = SpaceMonoFont,
                color = Color(0xFFFFFDF0)
            )
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFFF4C047)
            )
        }

    }
}

@Composable
fun ExpenseSummaryWidget(
    transactionViewModel: TransactionViewModel,
    userId: String
) {
    val  allTransactions by transactionViewModel.getAllTransactions(userId)
        .collectAsState(initial = emptyList())

    val expenseSum = allTransactions
        .filter { it.type == com.std.sol.entities.TransactionType.EXPENSE.name }
        .sumOf { it.amount }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Expenses", //idk if i should name it piechart or something
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = SpaceMonoFont,
            color = Color(0xFFFFFDF0)
        )
        Spacer(modifier = Modifier.height(12.dp))

        ExpenseCircle(
            totalSpent = expenseSum,
            categoryColor = null //use default gradient
        )
    }
}