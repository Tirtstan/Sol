package com.std.sol.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.Screen
import com.std.sol.SessionManager
import com.std.sol.components.BudgetComponent
import com.std.sol.databases.DatabaseProvider
import com.std.sol.entities.Category
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(navController: NavController, userViewModel: UserViewModel?) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }
    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)

    val currentUser by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(-1, "John Doe", ""))
    }
    val userId = currentUser?.id ?: 0

    val budgets by budgetViewModel.getAllBudgets(
        userId = userId,
        descending = true
    ).collectAsState(initial = emptyList())

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val filteredBudgets = remember(budgets, selectedCategory) {
        budgets.filter { selectedCategory == null || it.categoryId == selectedCategory!!.id }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("${Screen.AddEditBudget.route}/${userId}/0")
                },
                containerColor = Color(0xFFf4c047),
                contentColor = Color(0xFF0c1327)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add New Budget")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BUDGETS",
                    color = Color(0xFFFFFDF0),
                    fontSize = 28.sp,
                    fontFamily = SpaceMonoFont,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category filter row (horizontally scrollable)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterCard(
                        selected = selectedCategory == null,
                        label = "All",
                        onClick = { selectedCategory = null },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
                items(categories) { cat ->
                    FilterCard(
                        selected = selectedCategory?.id == cat.id,
                        label = cat.name,
                        onClick = { selectedCategory = cat },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Empty state or list
            if (currentUser == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please log in to view budgets.", color = Ivory, fontFamily = InterFont)
                }
            } else if (filteredBudgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No budgets match this category. Tap the '+' to create one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ivory,
                        fontFamily = InterFont
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBudgets) { budget ->
                        val category = categories.find { it.id == budget.categoryId }
                        BudgetComponent(
                            budget = budget,
                            category = category,
                            onClick = {
                                navController.navigate("${Screen.AddEditBudget.route}/${userId}/${budget.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun BudgetsScreenPreview() {
    SolTheme { BudgetsScreen(rememberNavController(), null) }
}