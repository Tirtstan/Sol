package com.std.sol.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel?) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = -1, username = "John Doe", passwordHash = ""))
    }
    val userId = user?.id ?: return

    var showAddDialog by remember { mutableStateOf(false) }

    // Initialize preset categories
    LaunchedEffect(userId) {
        initializePresetCategories(categoryViewModel, userId)
    }

    // Get today's date for filtering
    val calendar = Calendar.getInstance()
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

    // Calculate spending data
    val totalSpent = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val spendingLimit = 5000.0
    val spendingPercentage = (totalSpent / spendingLimit).coerceAtMost(1.0).toFloat()

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
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Circular Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularSpendingIndicator(
                    progress = spendingPercentage,
                    totalSpent = totalSpent,
                    spendingLimit = spendingLimit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Day indicator
            Card(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = RoyalBright),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).uppercase(),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = Ivory,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = SpaceMonoFont
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
            onClick = { showAddDialog = true },
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

    // Navigate to Add Transaction Screen
    if (showAddDialog) {
        LaunchedEffect(Unit) {
            navController.navigate("add_transaction")
            showAddDialog = false
        }
    }
}

@Composable
fun CircularSpendingIndicator(
    progress: Float,
    totalSpent: Double,
    spendingLimit: Double
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = center

            // Background circle
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            val sweepAngle = 360f * animatedProgress
            val startAngle = -90f

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Orange, Amber, Ember, Rose),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Decorative dots
            repeat(8) { index ->
                val angle = (index * 45f) * (Math.PI / 180f)
                val dotRadius = radius + strokeWidth + 10.dp.toPx()
                val dotX = center.x + cos(angle).toFloat() * dotRadius
                val dotY = center.y + sin(angle).toFloat() * dotRadius

                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "R${String.format("%.0f", totalSpent)}",
                color = Ivory,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
            Text(
                text = "of R${String.format("%.0f", spendingLimit)}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
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
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${String.format("%.0f", transaction.amount)}",
                color = if (transaction.type == TransactionType.INCOME) Leaf else Rose,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

// Helper functions for category colors and icons
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

// Initialize preset categories
suspend fun initializePresetCategories(categoryViewModel: CategoryViewModel, userId: Int) {
    val presetCategories = listOf("Food", "Fuel", "Entertainment", "Other")

    presetCategories.forEach { categoryName ->
        val existingCategory = categoryViewModel.getCategoryByName(categoryName)
        if (existingCategory == null) {
            val category = Category(
                id = 0,
                userId = userId,
                name = categoryName,
                color = "#000000", // We'll use programmatic colors instead
                icon = "default"
            )
            categoryViewModel.addCategory(category)
        }
    }
}

@Preview
@Composable
fun transactionScreenPreview()
{
    SolTheme { TransactionsScreen(rememberNavController(),null) }
}