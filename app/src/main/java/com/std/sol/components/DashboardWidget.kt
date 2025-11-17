package com.std.sol.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.std.sol.Screen
import com.std.sol.entities.Category
import com.std.sol.entities.Budget
import com.std.sol.ui.theme.*
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

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
                        onClick = { navController.navigate("${Screen.AddEditBudget.route}/") },
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
            budgetId = budget.id,
            userId = userId,
            categoryId = budget.categoryId,
            start = budget.startDate,
            end = budget.endDate
        )
    }

    // Get current amount for this specific budget from the map
    val currentAmounts by budgetViewModel.currentAmounts.collectAsState()
    val currentAmount = currentAmounts[budget.id] ?: 0.0

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
            text = "R${"%.2f".format(transaction.amount)}",
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

enum class PeriodType {
    WEEK,
    MONTH,
    CUSTOM
}

@Composable
fun ExpenseSummaryWidget(
    transactionViewModel: TransactionViewModel,
    budgetViewModel: BudgetViewModel,
    categoryViewModel: CategoryViewModel,
    userId: String
) {
    val context = LocalContext.current
    
    // Period selection state
    var selectedPeriod by remember { mutableStateOf(PeriodType.MONTH) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }
    
    // Date calculations
    val now = Date()
    val calendar = Calendar.getInstance()
    
    val thisMonthStart = remember {
        calendar.apply {
            time = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    val thisMonthEnd = remember {
        calendar.apply {
            time = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
    
    val thisWeekStart = remember {
        calendar.apply {
            time = now
            set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    val thisWeekEnd = remember {
        calendar.apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
    
    var customStart by remember { mutableStateOf(thisMonthStart) }
    var customEnd by remember { mutableStateOf(thisMonthEnd) }
    
    // Calculate date range based on selected period
    val (startDate, endDate) = when (selectedPeriod) {
        PeriodType.WEEK -> Pair(Timestamp(thisWeekStart), Timestamp(thisWeekEnd))
        PeriodType.MONTH -> Pair(Timestamp(thisMonthStart), Timestamp(thisMonthEnd))
        PeriodType.CUSTOM -> Pair(Timestamp(customStart), Timestamp(customEnd))
    }
    
    // Fetch data
    val transactions by transactionViewModel.getTransactionsByPeriod(
        userId,
        startDate,
        endDate
    ).collectAsState(initial = emptyList())
    
    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    val budgets by budgetViewModel.getAllBudgets(userId)
        .collectAsState(initial = emptyList())

    // Filter expenses and group by category
    val expenseTransactions = transactions.filter { 
        it.type == com.std.sol.entities.TransactionType.EXPENSE.name 
    }
    
    val categorySpending = remember(expenseTransactions, categories) {
        categories.map { category ->
            val spent = expenseTransactions
                .filter { it.categoryId == category.id }
        .sumOf { it.amount }
            category to spent
        }.filter { it.second > 0.0 }
            .sortedByDescending { it.second }
    }
    
    // Get budgets for the period
    val activeBudgets = remember(budgets, startDate, endDate) {
        budgets.filter { budget ->
            val budgetStart = budget.startDate.toDate().time
            val budgetEnd = budget.endDate.toDate().time
            val periodStart = startDate.toDate().time
            val periodEnd = endDate.toDate().time
            // Check if budget overlaps with selected period
            budgetStart <= periodEnd && budgetEnd >= periodStart
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title and period selector
        Row(
        modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = "Category Spending",
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = SpaceMonoFont,
                color = Ivory
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodButton(
                text = "Week",
                selected = selectedPeriod == PeriodType.WEEK,
                onClick = { selectedPeriod = PeriodType.WEEK },
                modifier = Modifier.weight(1f)
            )
            PeriodButton(
                text = "Month",
                selected = selectedPeriod == PeriodType.MONTH,
                onClick = { selectedPeriod = PeriodType.MONTH },
                modifier = Modifier.weight(1f)
            )
            PeriodButton(
                text = "Custom",
                selected = selectedPeriod == PeriodType.CUSTOM,
                onClick = { selectedPeriod = PeriodType.CUSTOM },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Custom date pickers
        if (selectedPeriod == PeriodType.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCustomStartPicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    Text(
                        text = SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(customStart),
                        fontSize = 12.sp,
                        color = Ivory
                    )
                }
                Button(
                    onClick = { showCustomEndPicker = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    Text(
                        text = SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(customEnd),
                        fontSize = 12.sp,
                        color = Ivory
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chart
        if (categorySpending.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses in this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ivory.copy(alpha = 0.7f)
                )
            }
        } else {
            CategorySpendingChart(
                categorySpending = categorySpending,
                activeBudgets = activeBudgets,
                categories = categories
            )
        }
    }
    
    // Date picker dialogs
    if (showCustomStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStart.time,
            yearRange = IntRange(2020, 2100)
        )
        DatePickerDialog(
            onDismissRequest = { showCustomStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            customStart = Date(it)
                            if (customEnd < customStart) {
                                customEnd = customStart
                            }
                        }
                        showCustomStartPicker = false
                    }
                ) {
                    Text("OK", color = Ocean, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartPicker = false }) {
                    Text("Cancel", color = Ivory)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showCustomEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customEnd.time,
            yearRange = IntRange(2020, 2100)
        )
        DatePickerDialog(
            onDismissRequest = { showCustomEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            customEnd = Date(it)
                            if (customStart > customEnd) {
                                customStart = customEnd
                            }
                        }
                        showCustomEndPicker = false
                    }
                ) {
                    Text("OK", color = Ocean, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndPicker = false }) {
                    Text("Cancel", color = Ivory)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PeriodButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Indigo else IndigoDark
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Ivory
        )
    }
}

@Composable
fun CategorySpendingChart(
    categorySpending: List<Pair<Category, Double>>,
    activeBudgets: List<Budget>,
    categories: List<Category>
) {
    val maxSpending = categorySpending.maxOfOrNull { it.second } ?: 1.0
    val chartHeight = 300.dp
    val barWidth = 40.dp
    val barSpacing = 16.dp
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val padding = 40.dp.toPx()
                val bottomPadding = 60.dp.toPx() // Extra padding for category labels
                val chartWidth = canvasWidth - padding * 2
                val chartHeightPx = canvasHeight - padding - bottomPadding
                
                // Draw grid lines
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = padding + (chartHeightPx / gridLines) * i
                    drawLine(
                        color = Ivory.copy(alpha = 0.1f),
                        start = androidx.compose.ui.geometry.Offset(padding, y),
                        end = androidx.compose.ui.geometry.Offset(canvasWidth - padding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                // Adjust bar drawing to account for bottom padding
                val adjustedChartHeightPx = chartHeightPx
                
                // Draw bars and goal lines
                val totalBarWidth = barWidth.toPx() + barSpacing.toPx()
                val startX = (padding + (chartWidth - totalBarWidth * categorySpending.size) / 2)
                
                categorySpending.forEachIndexed { index, (category, spent) ->
                    val x = (startX + index * totalBarWidth)
                    val barHeight = ((spent / maxSpending) * adjustedChartHeightPx).toFloat()
                    val barTop = (padding + adjustedChartHeightPx - barHeight)
                    
                    // Get category color - parse color string safely
                    val categoryColor = if (category.color.isNotEmpty()) {
                        try {
                            Color(category.color.toColorInt())
                        } catch (e: Exception) {
                            // Fallback to a default color if parsing fails
                            Color(0xFF465BE7)
                        }
                    } else {
                        // Use fallback color logic without composable
                        when (category.name.lowercase()) {
                            "food" -> Color(0xFFE74C3C)
                            "fuel" -> Color(0xFF280b26)
                            "entertainment" -> Color(0xFF8f1767)
                            "other" -> Color(0xFF465be7)
                            else -> Color(0xFF465be7)
                        }
                    }
                    
                    // Find budget for this category
                    val budget = activeBudgets.find { it.categoryId == category.id }
                    
                    // Draw min/max goal lines if budget exists
                    if (budget != null) {
                        val minHeight = ((budget.minGoalAmount / maxSpending) * adjustedChartHeightPx).toFloat()
                        val maxHeight = ((budget.maxGoalAmount / maxSpending) * adjustedChartHeightPx).toFloat()
                        
                        val minY = (padding + adjustedChartHeightPx - minHeight).toFloat()
                        val maxY = (padding + adjustedChartHeightPx - maxHeight).toFloat()
                        
                        // Draw min goal line (green)
                        drawLine(
                            color = Leaf.copy(alpha = 0.7f),
                            start = androidx.compose.ui.geometry.Offset((x - barSpacing.toPx() / 2).toFloat(), minY),
                            end = androidx.compose.ui.geometry.Offset((x + barWidth.toPx() + barSpacing.toPx() / 2).toFloat(), minY),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // Draw max goal line (red)
                        drawLine(
                            color = Ember.copy(alpha = 0.7f),
                            start = androidx.compose.ui.geometry.Offset((x - barSpacing.toPx() / 2).toFloat(), maxY),
                            end = androidx.compose.ui.geometry.Offset((x + barWidth.toPx() + barSpacing.toPx() / 2).toFloat(), maxY),
                            strokeWidth = 2.dp.toPx()
                        )
                        
                        // Fill area between min and max (goal zone)
                        if (maxHeight > minHeight) {
                            drawRect(
                                color = Lime.copy(alpha = 0.2f),
                                topLeft = androidx.compose.ui.geometry.Offset((x - barSpacing.toPx() / 2).toFloat(), minY),
                                size = androidx.compose.ui.geometry.Size(
                                    (barWidth.toPx() + barSpacing.toPx()).toFloat(),
                                    (maxY - minY).toFloat()
                                )
                            )
                        }
                    }
                    
                    // Determine bar color based on budget status
                    val barColor = if (budget != null) {
                        when {
                            spent < budget.minGoalAmount -> Amber.copy(alpha = 0.7f) // Below min
                            spent > budget.maxGoalAmount -> Ember.copy(alpha = 0.7f) // Above max
                            else -> Leaf.copy(alpha = 0.7f) // Within range
                        }
                    } else {
                        categoryColor.copy(alpha = 0.8f)
                    }
                    
                    // Draw bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = androidx.compose.ui.geometry.Offset(x, barTop),
                        size = androidx.compose.ui.geometry.Size(barWidth.toPx(), barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Category labels with better spacing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            categorySpending.forEach { (category, spent) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = category.name.take(10),
                        fontSize = 10.sp,
                        color = Ivory.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "R${"%.0f".format(spent)}",
                        fontSize = 9.sp,
                        color = Ivory.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        if (activeBudgets.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Amber, label = "Below Min")
                Spacer(modifier = Modifier.width(12.dp))
                LegendItem(color = Leaf, label = "In Range")
                Spacer(modifier = Modifier.width(12.dp))
                LegendItem(color = Ember, label = "Above Max")
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Ivory.copy(alpha = 0.7f)
        )
    }
}