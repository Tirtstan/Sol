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
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
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
import com.std.sol.SessionManager
import com.std.sol.entities.User
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import com.std.sol.ui.theme.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

enum class TransactionFilterType { RECENTS, MONTH, WEEK, CUSTOM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel?) {
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }
    val transactionRepository = remember { TransactionRepository() }
    val categoryRepository = remember { CategoryRepository() }
    val budgetRepository = remember { BudgetRepository() }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val viewModelFactory = remember { 
        ViewModelFactory(
            userRepository,
            transactionRepository,
            categoryRepository,
            budgetRepository,
            sessionManager
        )
    }
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    val user by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = "", username = "John Doe"))
    }
    val userId = user?.id ?: ""

    var filterType by remember { mutableStateOf(TransactionFilterType.RECENTS) }
    var customStart by remember { mutableStateOf(getStartOfDay(Date())) }
    var customEnd by remember { mutableStateOf(getEndOfDay(Date())) }
    var showCustomStartPicker by remember { mutableStateOf(false) }
    var showCustomEndPicker by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
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
            Timestamp(0, 0),
            Timestamp(253402300799, 999999999) // Max valid Timestamp (year 9999)
        ) // all for now, but will group by day
        TransactionFilterType.MONTH -> Pair(Timestamp(thisMonthStart), Timestamp(thisMonthEnd))
        TransactionFilterType.WEEK -> Pair(Timestamp(thisWeekStart), Timestamp(thisWeekEnd))
        TransactionFilterType.CUSTOM -> Pair(Timestamp(customStart), Timestamp(customEnd))
    }

    val allTransactions by transactionViewModel.getTransactionsByPeriod(
        userId,
        queryStart,
        queryEnd
    )
        .collectAsState(initial = emptyList())
    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    // Apply category filter to all transactions (works in all filter modes)
    val filteredTransactions = if (selectedCategory != null) {
        allTransactions.filter { it.categoryId == selectedCategory!!.id }
    } else {
        allTransactions
    }

    // Validate custom date range
    val validCustomRange = if (filterType == TransactionFilterType.CUSTOM) {
        customStart <= customEnd
    } else {
        true
    }

    val groupedTransactions = when (filterType) {
        TransactionFilterType.RECENTS -> groupTransactionsByDay(filteredTransactions)
        TransactionFilterType.MONTH -> groupTransactionsByDay(filteredTransactions)
        TransactionFilterType.WEEK -> groupTransactionsByDay(filteredTransactions)
        TransactionFilterType.CUSTOM -> {
            if (validCustomRange) {
                groupTransactionsByDay(filteredTransactions)
            } else {
                mapOf("Invalid Date Range" to emptyList<Transaction>())
            }
        }
    }
    var expenseSum =
        filteredTransactions.filter { it.getTransactionType() == TransactionType.EXPENSE }.sumOf { it.amount }
    if (LocalInspectionMode.current)
        expenseSum = 99999.0

    val circleColor =
        if (selectedCategory != null) {
            getCategoryColor(selectedCategory!!.name)
        } else {
            null // Use default gradient
        }

    var showAddScreen by remember { mutableStateOf(false) }
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        ),
                        color = Color(0xFFFFFDF0),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Expense Circle Chart
            item {
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
            }

            // Filter buttons
            item {
                LazyRow(
                    modifier = Modifier.height(44.dp),
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
            }

            // Category filter - available in all modes
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "All Categories",
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
                            .menuAnchor(),
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
                                selectedCategory = null
                                expandedCategoryDropdown = false
                            }
                        )

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
                                    selectedCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Custom date range picker - only shown in CUSTOM mode, simplified
            if (filterType == TransactionFilterType.CUSTOM) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showCustomStartPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF465be7),
                                contentColor = Color(0xFFFFFDF0)
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                "Start: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(customStart)}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Button(
                            onClick = { showCustomEndPicker = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF465be7),
                                contentColor = Color(0xFFFFFDF0)
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                "End: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(customEnd)}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Validation error message
                if (!validCustomRange) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFb42313),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start date must be before end date",
                                fontSize = 12.sp,
                                color = Color(0xFFb42313),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Transactions list
            groupedTransactions.forEach { (header, txns) ->
                if (header.isNotBlank()) {
                    item {
                        Text(
                            text = header,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = SpaceMonoFont,
                            color = Color(0xFFf4c047),
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

    // Date picker dialogs
    if (showCustomStartPicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = customStart.time,
                yearRange = IntRange(2020, 2100)
            )
        DatePickerDialog(
            onDismissRequest = { showCustomStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newStart = getStartOfDay(Date(it))
                            customStart = newStart
                            // Auto-adjust end date if it's before start date
                            if (customEnd < newStart) {
                                customEnd = getEndOfDay(newStart)
                            }
                        }
                        showCustomStartPicker = false
                    }
                ) { 
                    Text(
                        "OK",
                        color = Color(0xFF56a1bf),
                        fontWeight = FontWeight.SemiBold
                    ) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStartPicker = false }) { 
                    Text(
                        "Cancel",
                        color = Color(0xFFFFFDF0)
                    ) 
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showCustomEndPicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = customEnd.time,
                yearRange = IntRange(2020, 2100)
            )
        DatePickerDialog(
            onDismissRequest = { showCustomEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newEnd = getEndOfDay(Date(it))
                            customEnd = newEnd
                            // Auto-adjust start date if it's after end date
                            if (customStart > newEnd) {
                                customStart = getStartOfDay(newEnd)
                            }
                        }
                        showCustomEndPicker = false
                    }
                ) { 
                    Text(
                        "OK",
                        color = Color(0xFF56a1bf),
                        fontWeight = FontWeight.SemiBold
                    ) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomEndPicker = false }) { 
                    Text(
                        "Cancel",
                        color = Color(0xFFFFFDF0)
                    ) 
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

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
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
        
        ModalBottomSheet(
            onDismissRequest = { showAddScreen = false },
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
                    AddTransactionScreen(
                        navController = navController,
                        userViewModel = userViewModel,
                        transactionToEdit = editTransaction,
                        onTransactionDeleted = { transaction ->
                            transactionViewModel.deleteTransaction(userId, transaction)
                            showAddScreen = false
                        },
                        onClose = { showAddScreen = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )
                    // Add bottom padding for safe area
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (selected) DeepSpaceBase else Ivory
            )
        }
    }
}


fun groupTransactionsByDay(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val now = Calendar.getInstance()
    val today = getStartOfDay(now.time)
    val yesterday = getStartOfDay(Date(today.time - 24 * 60 * 60 * 1000))
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    return transactions
        .sortedByDescending { it.getDateAsDate() }
        .groupBy { txn ->
            val txnDay = getStartOfDay(txn.getDateAsDate())
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
fun TransactionCard(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onImageClick: (Uri) -> Unit = {}
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
                        text = transaction.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFFFFDF0)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateFormat.format(transaction.getDateAsDate()),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = SpaceMonoFont,
                        color = Color(0xFFF4C047)
                    )
                }

                transaction.imagePath?.let { imagePath ->
                    IconButton(
                        onClick = {
                            onImageClick(imagePath.toUri())
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
                    text = "${if (transaction.getTransactionType() == TransactionType.INCOME) "+" else "-"}${
                        String.format(
                            "%.2f",
                            transaction.amount
                        )
                    }",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SpaceMonoFont,
                    color = if (transaction.getTransactionType() == TransactionType.INCOME) Color(0xFF57c52b) else Color(
                        0xFFb42313
                    )
                )
            }

            transaction.note?.let { note ->
                if (note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        color = Color(0xFFFFFDF0).copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Color(0xFF118337)
        "fuel" -> Color(0xFF280b26)
        "entertainment" -> Color(0xFF8f1767)
        "other" -> Color(0xFF465be7)
        else -> Color(0xFF465be7)
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


@Preview(showBackground = true, backgroundColor = 0xFF25315E)
@Composable
fun TransactionsScreenPreview() {
    SolTheme { TransactionsScreen(rememberNavController(), null) }
}