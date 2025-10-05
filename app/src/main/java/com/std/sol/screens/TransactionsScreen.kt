package com.std.sol.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.components.StarryBackground
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.databases.DatabaseProvider
import com.std.sol.SessionManager
import com.std.sol.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel?) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    val user by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(null)
    }
    val userId = user?.id ?: return

    // DATE STATE
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    val startOfDay = calendar.time
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    val endOfDay = calendar.time

    val transactions by transactionViewModel.getTransactionsByPeriod(userId, startOfDay, endOfDay)
        .collectAsState(initial = emptyList())
    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    // TOTAL EXPENSE FOR SELECTED DATE
    val totalSpent = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    var showAddScreen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepSpaceBase, Indigo, PlumDeep)
                )
            )
    ) {
        StarryBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Ivory
                    )
                }
                Text(
                    text = "SPENDING",
                    color = Ivory,
                    fontSize = 20.sp,
                    fontFamily = SpaceMonoFont
                )
                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // DATE PICKER / NAVIGATION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.time = selectedDate
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        selectedDate = cal.time
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Day", tint = Ivory)
                }
                Text(
                    text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(selectedDate),
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Ivory,
                    fontSize = 16.sp,
                    fontFamily = SpaceMonoFont
                )
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.time = selectedDate
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        selectedDate = cal.time
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Day", tint = Ivory)
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedDate = Date(it)
                                }
                                showDatePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // EXPENSE CIRCLE (NO LIMIT, FULL CIRCLE)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                ExpenseCircle(totalSpent = totalSpent)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(transactions) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        category = categories.find { it.id == transaction.categoryId }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddScreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp),
            containerColor = Sky,
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(30.dp)
            )
        }
    }

    if (showAddScreen) {
        AddTransactionScreen(navController, userViewModel)
    }
}

@Composable
fun ExpenseCircle(totalSpent: Double) {
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = center

            // Full arc (since there's no progress/limit)
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(Orange, Amber, Ember, Rose, Orange),
                    center = center
                ),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "R${String.format("%.2f", totalSpent)}",
                color = Ivory,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
            Text(
                text = "Expenses",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    category: Category?
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = getCategoryColor(category?.name ?: ""),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category?.name ?: ""),
                    contentDescription = category?.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.name.uppercase(),
                    color = Ivory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = SpaceMonoFont
                )
            }

            // Amount
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${String.format("%.2f", transaction.amount)}",
                color = if (transaction.type == TransactionType.INCOME) Leaf else Rose,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

@Composable
fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Ocean
        "fuel" -> LeafDark
        "entertainment" -> Magenta
        "other" -> RoyalBright
        else -> RoyalBright
    }
}

@Composable
fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "fuel" -> Icons.Default.LocalGasStation
        "entertainment" -> Icons.Default.Movie
        "other" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}