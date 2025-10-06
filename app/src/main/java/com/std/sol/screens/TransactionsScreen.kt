package com.std.sol.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import com.std.sol.components.StarryBackground
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.utils.getCategoryColorFromEntity
import com.std.sol.utils.getCategoryIconFromEntity
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.databases.DatabaseProvider
import com.std.sol.SessionManager
import com.std.sol.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enum class defining different transaction filter types
 * RECENTS BUTTON: Shows recent transactions grouped by day
 * MONTH BUTTON: Shows transactions from current month
 * CUSTOM BUTTON: Shows transactions within custom date range with optional category filter
 */
enum class TransactionFilterType { RECENTS, MONTH, CUSTOM }

/**
 * Main Transactions Screen composable that displays user transactions with filtering options
 *
 * Features:
 * - Expense circle visualization showing total spent amount
 * - Three filter types: Recents, Month, and Custom
 * - Category-based filtering in custom mode
 * - Transaction list with edit functionality
 * - Image attachment support with preview
 * - Floating action button to add new transactions
 *
 * @param navController Navigation controller for screen transitions
 * @param userViewModel ViewModel containing current user data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel?) {
    // Get context and set up ViewModels
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    // Get current user, return early if no user is logged in
    val user by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(null)
    }
    val userId = user?.id ?: return

    // State for managing filter types and date ranges
    var filterType by remember { mutableStateOf(TransactionFilterType.RECENTS) }
    var customStart by remember { mutableStateOf(getStartOfDay(Date())) }
    var customEnd by remember { mutableStateOf(getEndOfDay(Date())) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    // State for category filtering in custom mode
    var selectedCustomCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    // State for image preview functionality
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }

    // Calculate date ranges based on filter type
    val now = Date()
    val thisMonthStart = Calendar.getInstance().apply {
        time = now
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    val thisMonthEnd = getEndOfDay(Date())

    // Determine query date range based on selected filter
    val (queryStart, queryEnd) = when (filterType) {
        TransactionFilterType.RECENTS -> Pair(Date(0), Date(Long.MAX_VALUE)) // All transactions for grouping
        TransactionFilterType.MONTH -> Pair(thisMonthStart, thisMonthEnd)
        TransactionFilterType.CUSTOM -> Pair(customStart, customEnd)
    }

    // Fetch transactions and categories from database
    val allTransactions by transactionViewModel.getTransactionsByPeriod(userId, queryStart, queryEnd)
        .collectAsState(initial = emptyList())
    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    // Apply category filter for custom mode
    val filteredTransactions = if (filterType == TransactionFilterType.CUSTOM && selectedCustomCategory != null) {
        allTransactions.filter { it.categoryId == selectedCustomCategory!!.id }
    } else {
        allTransactions
    }

    // Group transactions by day for display
    val groupedTransactions = when (filterType) {
        TransactionFilterType.RECENTS -> groupTransactionsByDay(allTransactions)
        TransactionFilterType.MONTH -> groupTransactionsByDay(allTransactions)
        TransactionFilterType.CUSTOM -> mapOf("" to filteredTransactions.sortedByDescending { it.date })
    }

    // Calculate total expenses for the circle display
    val expenseSum = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    // Determine circle color based on selected category
    val circleColor = if (filterType == TransactionFilterType.CUSTOM && selectedCustomCategory != null) {
        getCategoryColorFromEntity(selectedCustomCategory)
    } else {
        null // Use default gradient
    }

    // State for add or edit transaction functionality
    var showAddScreen by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Main UI layout with gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0c1327), Color(0xFF25315e), Color(0xFF19102e))
                )
            )
    ) {
        // Starry background component for visual appeal to communicate outer space theme
        StarryBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Screen header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSACTIONS",
                    color = Color(0xFFFFFDF0),
                    fontSize = 20.sp,
                    fontFamily = SpaceMonoFont
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Centered expense circle with dynamic color based on category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                ExpenseCircle(
                    totalSpent = expenseSum,
                    categoryColor = circleColor
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Filter type selection cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterCard(
                    selected = filterType == TransactionFilterType.RECENTS,
                    label = "Recents",
                    onClick = {
                        filterType = TransactionFilterType.RECENTS
                        selectedCustomCategory = null // Reset category filter
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterCard(
                    selected = filterType == TransactionFilterType.MONTH,
                    label = "Month",
                    onClick = {
                        filterType = TransactionFilterType.MONTH
                        selectedCustomCategory = null // Reset category filter
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterCard(
                    selected = filterType == TransactionFilterType.CUSTOM,
                    label = "Custom",
                    onClick = { filterType = TransactionFilterType.CUSTOM },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Custom filter controls for date pickers and category selector
            if (filterType == TransactionFilterType.CUSTOM) {
                // Date range selection buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CustomDatePickerButton(
                        text = "Start: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(customStart)}",
                        onClick = { showCustomStartPicker = true }
                    )
                    CustomDatePickerButton(
                        text = "End: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(customEnd)}",
                        onClick = { showCustomEndPicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category filter dropdown with category colors and icons
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCustomCategory?.name ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter by Category", color = Color(0xFFFFFDF0)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expandedCategoryDropdown
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedCategoryDropdown = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF56a1bf),
                            unfocusedBorderColor = Color(0xFFFFFDF0),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF56a1bf),
                            focusedLabelColor = Color(0xFFFFFDF0),
                            unfocusedLabelColor = Color(0xFFFFFDF0)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                Color(0xFF465be7),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Category,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("All Categories")
                                }
                            },
                            onClick = {
                                selectedCustomCategory = null
                                expandedCategoryDropdown = false
                            }
                        )

                        // Individual category options with custom colors and icons
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(
                                                    getCategoryColorFromEntity(category),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIconFromEntity(category),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(category.name)
                                    }
                                },
                                onClick = {
                                    selectedCustomCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Date picker dialogs for custom date range selection
            if (showCustomStartPicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customStart.time)
                DatePickerDialog(
                    onDismissRequest = { showCustomStartPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    customStart = getStartOfDay(Date(it))
                                }
                                showCustomStartPicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomStartPicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            if (showCustomEndPicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = customEnd.time)
                DatePickerDialog(
                    onDismissRequest = { showCustomEndPicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    customEnd = getEndOfDay(Date(it))
                                }
                                showCustomEndPicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomEndPicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Scrollable transactions list with group headers
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                groupedTransactions.forEach { (header, txns) ->
                    // Add date group header if not empty
                    if (header.isNotBlank()) {
                        item {
                            Text(
                                text = header,
                                color = Color(0xFFf4c047),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)
                            )
                        }
                    }
                    // Add transaction cards for each group
                    items(txns) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            category = categories.find { it.id == transaction.categoryId },
                            onEdit = {
                                editTransaction = transaction
                                showAddScreen = true
                            },
                            onImageClick = { imageUri ->
                                selectedImageUri = imageUri
                                showImagePreview = true
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button for adding new transactions
        FloatingActionButton(
            onClick = {
                editTransaction = null
                showAddScreen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(60.dp),
            containerColor = Color(0xFFf4c047),
            contentColor = Color(0xFF0c1327)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(30.dp)
            )
        }
    }

    // Image preview modal dialog
    if (showImagePreview && selectedImageUri != null) {
        ImagePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                showImagePreview = false
                selectedImageUri = null
            }
        )
    }

    // Add and Edit transaction screen overlay
    if (showAddScreen) {
        AddTransactionScreen(
            navController = navController,
            userViewModel = userViewModel,
            transactionToEdit = editTransaction,
            onTransactionDeleted = { transaction ->
                transactionViewModel.deleteTransaction(transaction)
                showAddScreen = false
            },
            onClose = { showAddScreen = false }
        )
    }
}

/**
 * Modal dialog for previewing transaction images in full screen
 *
 * @param imageUri URI of the image to display
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun ImagePreviewDialog(
    imageUri: Uri,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Dialog header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Image display with aspect ratio preservation
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Transaction image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(
                            Color.Black.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// Helper Functions

/**
 * Filter selection card component
 *
 * @param selected Whether this filter is currently selected
 * @param label Display text for the filter
 * @param onClick Callback when filter is tapped
 * @param modifier Modifier for styling
 */
@Composable
fun FilterCard(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(44.dp)
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFf4c047) else Color(0xFF291945)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                color = if (selected) Color(0xFF0c1327) else Color(0xFFFFFDF0),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

/**
 * Custom date picker button for date range selection
 *
 * @param text Display text showing current selected date
 * @param onClick Callback when button is pressed
 */
@Composable
fun CustomDatePickerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF465be7),
            contentColor = Color(0xFFFFFDF0)
        ),
        modifier = Modifier.height(38.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Groups transactions by day with smart labeling (Today, Yesterday, or formatted date)
 *
 * @param transactions List of transactions to group
 * @return Map with date labels as keys and transaction lists as values
 */
fun groupTransactionsByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val now = Calendar.getInstance()
    val today = getStartOfDay(now.time)
    val yesterday = getStartOfDay(Date(today.time - 24 * 60 * 60 * 1000))
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

    return transactions
        .sortedByDescending { it.date }
        .groupBy { txn ->
            val txnDay = getStartOfDay(txn.date)
            when (txnDay) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> dateFormat.format(txnDay)
            }
        }
}

/**
 * Utility function to get start of day (00:00:00) for a given date
 *
 * @param date Input date
 * @return Date set to start of day
 */
fun getStartOfDay(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

/**
 * Utility function to get end of day (23:59:59.999) for a given date
 *
 * @param date Input date
 * @return Date set to end of day
 */
fun getEndOfDay(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
}

/**
 * Circular expense visualization component
 *
 * @param totalSpent Total amount spent to display in center
 * @param categoryColor Optional category color for the circle border
 */
@Composable
fun ExpenseCircle(
    totalSpent: Double,
    categoryColor: Color? = null // Optional category color parameter
) {
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth: Float = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = center

            // Use category color if provided, otherwise use default gradient
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
                        colors = listOf(Color(0xFFf4680b), Color(0xFFf4c047), Color(0xFFb42313), Color(0xFFf45d92), Color(0xFFf4680b)),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Center text showing expense amount and label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "R${String.format("%.2f", totalSpent)}",
                color = Color(0xFFFFFDF0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
            Text(
                text = "Expenses",
                color = Color(0xFFF4C047),
                fontSize = 13.sp,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

/**
 * Individual transaction card component
 *
 * @param transaction Transaction data to display
 * @param category Category associated with the transaction
 * @param onEdit Callback when transaction is tapped for editing
 * @param onImageClick Callback when image icon is tapped
 */
@Composable
fun TransactionCard(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onImageClick: (Uri) -> Unit = {} // Callback for image click
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF291945)
        ),
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main transaction information row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon with colored background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = getCategoryColorFromEntity(category),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIconFromEntity(category),
                        contentDescription = category?.name,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Transaction name and time
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = transaction.name.uppercase(),
                        color = Color(0xFFFFFDF0),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceMonoFont
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateFormat.format(transaction.date),
                        color = Color(0xFFF4C047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = SpaceMonoFont
                    )
                }

                // Image icon if transaction has attached image
                transaction.imagePath?.let { imagePath ->
                    IconButton(
                        onClick = {
                            onImageClick(Uri.parse(imagePath))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "View attached image",
                            tint = Color(0xFF56a1bf),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Transaction amount with income/expense color coding
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${String.format("%.2f", transaction.amount)}",
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF57c52b) else Color(0xFFb42313),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
            }

            // Display transaction note if available
            transaction.note?.let { note ->
                if (note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note,
                        color = Color(0xFFFFFDF0).copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = SpaceMonoFont
                    )
                }
            }
        }
    }
}