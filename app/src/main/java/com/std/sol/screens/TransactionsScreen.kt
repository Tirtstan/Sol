package com.std.sol.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.databases.DatabaseProvider
import com.std.sol.SessionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    val currentUser by userViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: return

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.ALL) }
    var selectedType by remember { mutableStateOf(TypeFilter.ALL) }

    // Date range states
    val calendar = Calendar.getInstance()
    val startDate = remember { mutableStateOf(Date()) }
    val endDate = remember { mutableStateOf(Date()) }

    // Set default date range based on selected period
    LaunchedEffect(selectedPeriod) {
        when (selectedPeriod) {
            PeriodFilter.TODAY -> {
                calendar.time = Date()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                startDate.value = calendar.time
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                endDate.value = calendar.time
            }
            PeriodFilter.THIS_WEEK -> {
                calendar.time = Date()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                startDate.value = calendar.time
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                endDate.value = calendar.time
            }
            PeriodFilter.THIS_MONTH -> {
                calendar.time = Date()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate.value = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate.value = calendar.time
            }
            PeriodFilter.ALL -> {
                calendar.set(2020, 0, 1) // Far past date
                startDate.value = calendar.time
                endDate.value = Date() // Current date
            }
        }
    }

    // Get filtered transactions
    val transactions by when {
        selectedPeriod != PeriodFilter.ALL -> {
            transactionViewModel.getTransactionsByPeriod(userId, startDate.value, endDate.value)
        }
        else -> {
            transactionViewModel.getAllTransactions(userId)
        }
    }.collectAsState(initial = emptyList())

    // Filter by type
    val filteredTransactions = when (selectedType) {
        TypeFilter.INCOME -> transactions.filter { it.type == TransactionType.INCOME }
        TypeFilter.EXPENSE -> transactions.filter { it.type == TransactionType.EXPENSE }
        TypeFilter.ALL -> transactions
    }

    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Period Filter
            FilterChip(
                onClick = {
                    selectedPeriod = when (selectedPeriod) {
                        PeriodFilter.ALL -> PeriodFilter.TODAY
                        PeriodFilter.TODAY -> PeriodFilter.THIS_WEEK
                        PeriodFilter.THIS_WEEK -> PeriodFilter.THIS_MONTH
                        PeriodFilter.THIS_MONTH -> PeriodFilter.ALL
                    }
                },
                label = { Text(selectedPeriod.label) },
                selected = selectedPeriod != PeriodFilter.ALL
            )

            // Type Filter
            FilterChip(
                onClick = {
                    selectedType = when (selectedType) {
                        TypeFilter.ALL -> TypeFilter.INCOME
                        TypeFilter.INCOME -> TypeFilter.EXPENSE
                        TypeFilter.EXPENSE -> TypeFilter.ALL
                    }
                },
                label = { Text(selectedType.label) },
                selected = selectedType != TypeFilter.ALL
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalIncome = filteredTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpense = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            SummaryCard(
                title = "Income",
                amount = totalIncome,
                color = Color.Green,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Expense",
                amount = totalExpense,
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Balance",
                amount = totalIncome - totalExpense,
                color = if (totalIncome >= totalExpense) Color.Green else Color.Red,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    category = categories.find { it.id == transaction.categoryId },
                    onEdit = { /* TODO: Implement edit */ },
                    onDelete = { transactionViewModel.deleteTransaction(transaction) }
                )
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { transaction ->
                transactionViewModel.addTransaction(transaction.copy(userId = userId))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "R${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = category?.name ?: "Unknown Category",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormat.format(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}R${String.format("%.2f", transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME) Color.Green else Color.Red
                )

                // Show image indicator if present
                if (!transaction.imagePath.isNullOrBlank()) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Has attachment",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Transaction Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("Income") },
                        selected = selectedType == TransactionType.INCOME,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("Expense") },
                        selected = selectedType == TransactionType.EXPENSE,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Date Field
                OutlinedTextField(
                    value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Note Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Image Attachment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Attach Image:")
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            imagePickerLauncher.launch(intent)
                        }
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach Image")
                    }
                }

                // Show selected image
                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank() && selectedCategory != null) {
                        onConfirm(
                            Transaction(
                                userId = 0, // Will be set in the screen
                                categoryId = selectedCategory!!.id,
                                name = name,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                date = selectedDate,
                                note = note.ifBlank { null },
                                type = selectedType,
                                imagePath = imageUri?.toString()
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date Picker (you'll need to implement this based on your preferred date picker library)
    if (showDatePicker) {
        // TODO: Implement date picker dialog
        // For now, we'll just close it
        showDatePicker = false
    }
}

enum class PeriodFilter(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

enum class TypeFilter(val label: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense")
}