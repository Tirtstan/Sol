package com.std.sol.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.navigation.compose.rememberNavController
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
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionFilterType { RECENTS, MONTH, WEEK, CUSTOM }

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
        mutableStateOf(User(-1, "John Doe", ""))
    }
    val userId = user?.id ?: -1

    // Filtering state
    var filterType by remember { mutableStateOf(TransactionFilterType.RECENTS) }
    var customStart by remember { mutableStateOf(getStartOfDay(Date())) }
    var customEnd by remember { mutableStateOf(getEndOfDay(Date())) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }

    // NEW: Category filter state
    var selectedCustomCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    // NEW: Image preview state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePreview by remember { mutableStateOf(false) }

    // Date range for fetching transactions
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

    // This week start/end (respect locale first day of week)
    val thisWeekRange = Calendar.getInstance().let { cal ->
        cal.time = now
        // Move to first day of week
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val end = getEndOfDay(cal.time)
        Pair(start, end)
    }
    val thisWeekStart = thisWeekRange.first
    val thisWeekEnd = thisWeekRange.second

    val (queryStart, queryEnd) = when (filterType) {
        TransactionFilterType.RECENTS -> Pair(
            Date(0),
            Date(Long.MAX_VALUE)
        ) // all for now, but will group by day
        TransactionFilterType.MONTH -> Pair(thisMonthStart, thisMonthEnd)
        TransactionFilterType.WEEK -> Pair(thisWeekStart, thisWeekEnd)
        TransactionFilterType.CUSTOM -> Pair(customStart, customEnd)
    }

    // All transactions in the period
    val allTransactions by transactionViewModel.getTransactionsByPeriod(
        userId,
        queryStart,
        queryEnd
    )
        .collectAsState(initial = emptyList())
    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    // Filter transactions by category if custom filter is selected and category is chosen
    val filteredTransactions =
        if (filterType == TransactionFilterType.CUSTOM && selectedCustomCategory != null) {
            allTransactions.filter { it.categoryId == selectedCustomCategory!!.id }
        } else {
            allTransactions
        }

    // Group or filter transactions as needed
    val groupedTransactions = when (filterType) {
        TransactionFilterType.RECENTS -> groupTransactionsByDay(allTransactions)
        TransactionFilterType.MONTH -> groupTransactionsByDay(allTransactions)
        TransactionFilterType.WEEK -> groupTransactionsByDay(allTransactions)
        TransactionFilterType.CUSTOM -> mapOf("" to filteredTransactions.sortedByDescending { it.date })
    }
    var expenseSum =
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    if (LocalInspectionMode.current)
        expenseSum = 99999.0

    // NEW: Determine circle color based on selected category
    val circleColor =
        if (filterType == TransactionFilterType.CUSTOM && selectedCustomCategory != null) {
            getCategoryColor(selectedCustomCategory!!.name)
        } else {
            null // Use default gradient
        }

    // New state for adding and editing
    var showAddScreen by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editTransaction = null
                    showAddScreen = true
                },
                modifier = Modifier
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSACTIONS",
                    color = Color(0xFFFFFDF0),
                    fontSize = 28.sp,
                    fontFamily = SpaceMonoFont,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

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

            LazyRow(
                modifier = Modifier
                    .height(44.dp),
                contentPadding = PaddingValues(start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterCard(
                        selected = filterType == TransactionFilterType.RECENTS,
                        label = "Recent",
                        onClick = {
                            filterType = TransactionFilterType.RECENTS
                            selectedCustomCategory = null // Reset category filter
                        },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
                item {
                    FilterCard(
                        selected = filterType == TransactionFilterType.MONTH,
                        label = "This Month",
                        onClick = {
                            filterType = TransactionFilterType.MONTH
                            selectedCustomCategory = null // Reset category filter
                        },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
                item {
                    FilterCard(
                        selected = filterType == TransactionFilterType.WEEK,
                        label = "This Week",
                        onClick = {
                            filterType = TransactionFilterType.WEEK
                            selectedCustomCategory = null
                        },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
                item {
                    FilterCard(
                        selected = filterType == TransactionFilterType.CUSTOM,
                        label = "Custom",
                        onClick = { filterType = TransactionFilterType.CUSTOM },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // Custom date pickers and category selector
            if (filterType == TransactionFilterType.CUSTOM) {
                // Date pickers row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CustomDatePickerButton(
                        text = "Start: ${
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                customStart
                            )
                        }",
                        onClick = { showCustomStartPicker = true }
                    )
                    CustomDatePickerButton(
                        text = "End: ${
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                customEnd
                            )
                        }",
                        onClick = { showCustomEndPicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // UPDATED: Category selector dropdown with entity colors and icons
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
                        // "All Categories" option
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

                        // Individual categories
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

            // Date pickers for custom filter
            if (showCustomStartPicker) {
                val datePickerState =
                    rememberDatePickerState(initialSelectedDateMillis = customStart.time)
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
                val datePickerState =
                    rememberDatePickerState(initialSelectedDateMillis = customEnd.time)
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

            // Transactions List with group headers
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                groupedTransactions.forEach { (header, txns) ->
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

    }

    // Image Preview Dialog
    if (showImagePreview && selectedImageUri != null) {
        ImagePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                showImagePreview = false
                selectedImageUri = null
            }
        )
    }

    if (showAddScreen) {
        ModalBottomSheet(
            onDismissRequest = { showAddScreen = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(brush = Brush.verticalGradient(listOf(Indigo, IndigoLight)))
            ) {
                AddTransactionScreen(
                    navController = navController,
                    userViewModel = userViewModel,
                    transactionToEdit = editTransaction,
                    onTransactionDeleted = { transaction ->
                        transactionViewModel.deleteTransaction(transaction)
                        showAddScreen = false
                    },
                    onClose = { showAddScreen = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        }
    }
}

// NEW: Image Preview Dialog
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
                // Header with close button
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

                // Image
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
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Amber else Plum
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            Text(
                text = label,
                color = if (selected) DeepSpaceBase else Ivory,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

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

fun getStartOfDay(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun getEndOfDay(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.time
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
                color = Color(0xFFFFFDF0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
            Text(
                text = "Expenses",
                color = Color(0xFFF4C047),
                fontSize = 12.sp,
                fontFamily = SpaceMonoFont
            )
        }

    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onImageClick: (Uri) -> Unit = {} // NEW: Callback for image click
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
            // Main transaction info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // NEW: Image icon if image exists
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

                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${String.format("%.2f", transaction.amount)}",
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF57c52b) else Color(0xFFb42313),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
            }
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${
                    String.format(
                        "%.2f",
                        transaction.amount
                    )
                }",
                color = if (transaction.type == TransactionType.INCOME) Color(0xFF57c52b) else Color(
                    0xFFb42313
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
        }
    }
}

            // Display note if available
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

@Preview(showBackground = true, backgroundColor = 0xFF25315E)
@Composable
fun TransactionsScreenPreview() {
    SolTheme { TransactionsScreen(rememberNavController(), null) }
}