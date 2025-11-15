package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.Screen
import com.std.sol.SessionManager
import com.std.sol.components.BudgetComponent
import com.std.sol.entities.Category
import com.std.sol.entities.User
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import com.std.sol.ui.theme.*
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.components.RecentBudgetsWidget
import androidx.compose.foundation.lazy.items
import com.std.sol.components.DashboardWidgetType
import com.std.sol.viewmodels.DashboardViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, userViewModel: UserViewModel?) {
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
    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)

    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = "", username = "John Doe"))
    }
    val userId = user?.id ?: ""

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val widgets by dashboardViewModel.dashboardWidgets.collectAsState()

    var showBudgetSheet by remember { mutableStateOf(false) }
    var editBudgetId by remember { mutableStateOf<String?>(null) }

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
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFFFFFDF0),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Please log in to view dashboard.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ivory
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    //navigate to the new screen
                    navController.navigate(Screen.CustomizeDashboard.route)
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Customize Dashboard",
                        tint = Color(0xFFF4C047)
                    )
                }
            }
            val budgetDao = db.budgetDao()

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp) //adds space between widget
            ) {
                items(widgets) { widgetType ->
                    //checks the list and builds the UI
                    when (widgetType) {

                        DashboardWidgetType.RECENT_BUDGETS -> {
                            RecentBudgetsWidget(
                                navController = navController,
                                budgetViewModel = budgetViewModel,
                                categoryViewModel = categoryViewModel,
                                budgetDao = budgetDao,
                                userId = userId,
                                onEditBudget = { budgetID ->
                                    editBudgetId = budgetID
                                    showBudgetSheet = true
                                }
                            )
                        }
                        //when adding new widgets, you'll add them here
                    }
                }
            }
        }
    }

    val currentBudgetId = editBudgetId
    if (showBudgetSheet && currentBudgetId != null && currentBudgetId.isNotBlank()) {
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
                        budgetId = currentBudgetId,
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