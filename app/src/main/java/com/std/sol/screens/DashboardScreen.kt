package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun DashboardScreen(navController: NavController, userViewModel: UserViewModel?) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }
    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)

    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = -1, username = "John Doe", passwordHash = ""))
    }
    val userId = user?.id ?: 0

    val budgets by budgetViewModel.getAllBudgets(
        userId = userId,
        descending = true
    ).collectAsState(initial = emptyList())

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    val recentBudgets = remember(budgets) {
        budgets.take(3)
    }

    var showBudgetSheet by remember { mutableStateOf(false) }
    var editBudgetId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DASHBOARD",
                color = Color(0xFFFFFDF0),
                fontSize = 28.sp,
                fontFamily = SpaceMonoFont,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please log in to view dashboard.", color = Ivory, fontFamily = InterFont)
            }
        } else {
            if (recentBudgets.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Budgets",
                        color = Color(0xFFFFFDF0),
                        fontSize = 20.sp,
                        fontFamily = SpaceMonoFont,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Budgets.route) }
                    ) {
                        Text(
                            text = "View All",
                            color = Color(0xFFF4C047),
                            fontFamily = InterFont,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentBudgets) { budget ->
                        val category = categories.find { it.id == budget.categoryId }
                        BudgetItem(
                            budget = budget,
                            category = category,
                            budgetDao = db.budgetDao(),
                            onNavigate = {
                                editBudgetId = budget.id
                                showBudgetSheet = true
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
                            fontFamily = InterFont,
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
                            Text("Create Budget", fontFamily = SpaceMonoFont)
                        }
                    }
                }
            }
        }
    }

    if (showBudgetSheet && editBudgetId != null && editBudgetId != 0) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
        
        ModalBottomSheet(
            onDismissRequest = { 
                showBudgetSheet = false
                editBudgetId = null
            },
            sheetState = sheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            },
            containerColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(brush = Brush.verticalGradient(listOf(Indigo, IndigoLight)))
            ) {
                Column {
                    AddEditBudgetScreen(
                        navController = navController,
                        userId = userId,
                        budgetId = editBudgetId ?: 0,
                        onClose = {
                            showBudgetSheet = false
                            editBudgetId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0C1327)
@Composable
fun DashboardScreenPreview() {
    SolTheme {
        DashboardScreen(rememberNavController(), null)
    }
}