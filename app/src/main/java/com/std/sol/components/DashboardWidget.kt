package com.std.sol.components

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

enum class DashboardWidgetType{
    RECENT_BUDGETS,
    //CATEGORY_SUMMARY_PIECHART
    //RECENT_TRANSACTIONS
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