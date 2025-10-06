package com.std.sol.screens

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.components.StarryBackground
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import com.std.sol.utils.getCategoryColorFromEntity
import com.std.sol.utils.getCategoryIconFromEntity
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.databases.DatabaseProvider
import com.std.sol.SessionManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable function that displays the Add/Edit Transaction screen
 * This screen allows users to create new transactions or edit existing ones
 *
 * @param navController Navigation controller for screen navigation
 * @param userViewModel ViewModel for user-related operations
 * @param transactionToEdit Optional transaction to edit (null for new transaction)
 * @param onTransactionDeleted Callback when transaction is deleted
 * @param onClose Callback when screen is closed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    userViewModel: UserViewModel?,
    transactionToEdit: Transaction? = null,
    onTransactionDeleted: ((Transaction) -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    // Get current context and initialize ViewModels
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    // Get current user from userViewModel or provide default
    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = -1, username = "John Doe", passwordHash = ""))
    }
    val userId = user?.id ?: return

    // Pre-filled with existing transaction data if in edit mode

    // Transaction amount
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }

    // Transaction name/description
    var transactionName by remember { mutableStateOf(transactionToEdit?.name ?: "") }

    // Transaction type
    var selectedType by remember { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }

    // Selected category for the transaction
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // Optional note for the transaction
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }

    // Date and time for the transaction
    var selectedDate by remember { mutableStateOf(transactionToEdit?.date ?: Date()) }

    // UI state for date or time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Category dropdown state
    var expandedCategory by remember { mutableStateOf(false) }

    // Image attachment
    var imageUri by remember { mutableStateOf<Uri?>(transactionToEdit?.imagePath?.let { Uri.parse(it) }) }

    // Get all categories for the current user
    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    // Set default category when categories are loaded
    // In edit mode, find the category that matches the transaction's categoryId
    LaunchedEffect(categories, transactionToEdit) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = transactionToEdit?.let { t ->
                categories.find { it.id == t.categoryId }
            } ?: categories.first()
        }
    }

    // Date picker state initialization
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    // Image picker launcher which handles results from image selection
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
        }
    }

    // Time picker
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Scroll state for the main content area
    val scrollState = rememberScrollState()

    // The main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Gradient background matching the app's space theme
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0c1327), Color(0xFF25315e), Color(0xFF19102e))
                )
            )
    ) {
        // Starry background component for visual appeal that pushes idea of outer space theme
        StarryBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Header remains at top while content scrolls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp)) // Balance for close button
                Text(
                    text = if (transactionToEdit == null) "NEW TRANSACTION" else "EDIT TRANSACTION",
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
                // Close button
                IconButton(onClick = {
                    if (onClose != null) onClose()
                    else navController.navigateUp()
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFFFFDF0)
                    )
                }
            }

            // Makes content scrollable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Enables scrolling for long content
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp), // Extra padding for save button accessibility
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))


                // Prominent display of the transaction amount
                Text(
                    text = "R${if (amount.isBlank()) "0.00" else String.format("%.2f", amount.toDoubleOrNull() ?: 0.0)}",
                    color = Color(0xFFFFFDF0),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selecting Transaction Type
                // Toggle buttons for EXPENSE vs INCOME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TransactionTypeButton(
                        text = "EXPENSE",
                        isSelected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        modifier = Modifier.weight(1f)
                    )
                    TransactionTypeButton(
                        text = "INCOME",
                        isSelected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Amount Input Field
                // Validates decimal input with regex pattern
                SpaceTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Regex ensures valid decimal format (e.g., 123.45)
                        val regex = Regex("^\\d*\\.?\\d{0,2}$")
                        if (newValue.isEmpty() || regex.matches(newValue)) {
                            amount = newValue
                        }
                    },
                    label = "Amount",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction Name Input
                SpaceTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    label = "Transaction Name",
                    placeholder = "Enter name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection Dropdown

                // Shows category color and icon
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .clickable { expandedCategory = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF56a1bf),
                            unfocusedBorderColor = Color(0xFFFFFDF0),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF56a1bf)
                        )
                    )
                    // Dropdown menu with category options
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Category color indicator
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(
                                                    getCategoryColorFromEntity(category),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Category icon
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
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Attachment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Attach Image (optional):",
                        color = Color(0xFFFFFDF0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = SpaceMonoFont
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Remove image button which is only shown if image is selected
                        if (imageUri != null) {
                            IconButton(
                                onClick = { imageUri = null }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Image",
                                    tint = Color(0xFFf45d92)
                                )
                            }
                        }
                        // Add image button which launches image picker
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                imagePickerLauncher.launch(intent)
                            }
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Pick Image",
                                tint = Color(0xFF56a1bf)
                            )
                        }
                    }
                }

                // Display selected image preview
                imageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selecting Date and Time
                // Combined date and time picker in a single card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { showDatePicker = true }, // Click to open date picker
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3a5c85)
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date icon
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF118337), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Formatted date display
                        Text(
                            text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)}",
                            color = Color(0xFFFFFDF0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SpaceMonoFont,
                            modifier = Modifier.weight(1f)
                        )
                        // Time picker button
                        IconButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Pick time",
                                tint = Color(0xFFF4C047)
                            )
                        }
                        // Current time display
                        Text(
                            text = timeFormat.format(selectedDate),
                            color = Color(0xFFF4C047),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = SpaceMonoFont,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Note Input Field
                // Optional note field for additional transaction details
                SpaceTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note (Optional)",
                    placeholder = "Add a note",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Delete button which is only shown in edit mode
                    if (transactionToEdit != null) {
                        SpaceButton(
                            text = "DELETE",
                            onClick = {
                                onTransactionDeleted?.invoke(transactionToEdit)
                            },
                            modifier = Modifier.weight(1f),
                            gradientColors = listOf(Color(0xFFf45d92), Color(0xFFb42313)),
                            shadowColor = Color(0xFFf45d92),
                            borderColor = Color(0xFFb42313)
                        )
                    }
                    // Save/Update button
                    SpaceButton(
                        text = if (transactionToEdit == null) "SAVE" else "UPDATE",
                        onClick = {
                            // Validate required fields before saving
                            if (amount.isNotBlank() && transactionName.isNotBlank() && selectedCategory != null) {
                                // Create transaction object with current form data
                                val transaction = Transaction(
                                    id = transactionToEdit?.id ?: 0,
                                    userId = userId,
                                    categoryId = selectedCategory!!.id,
                                    name = transactionName,
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    date = selectedDate,
                                    note = note.ifBlank { null },
                                    type = selectedType,
                                    imagePath = imageUri?.toString()
                                )
                                // Save or update transaction based on mode
                                if (transactionToEdit == null) {
                                    transactionViewModel.addTransaction(transaction)
                                } else {
                                    transactionViewModel.updateTransaction(transaction)
                                }
                                // Close screen after successful save
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            }
                        },
                        // Enable button only when required fields are filled
                        enabled = amount.isNotBlank() && transactionName.isNotBlank() && selectedCategory != null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Extra bottom spacing to ensure save button is always accessible
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance()
                            cal.time = selectedDate
                            cal.timeInMillis = millis
                            // Preserve existing time when updating date
                            selectedDate = cal.time
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, selectedHour: Int, selectedMinute: Int ->
                // Update time while preserving date
                val cal = Calendar.getInstance().apply { time = selectedDate }
                cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                cal.set(Calendar.MINUTE, selectedMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDate = cal.time
                showTimePicker = false
            },
            hour, minute, true // 24-hour format
        ).apply { show() }
    }
}

/**
 * Custom composable for transaction type selection buttons (EXPENSE/INCOME)
 * Creates a toggleable button with visual feedback for selection state
 *
 * @param text Button text to display
 * @param isSelected Whether this button is currently selected
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for styling and layout
 */
@Composable
fun TransactionTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            // Different colors for selected vs unselected state
            containerColor = if (isSelected) Color(0xFF465be7) else Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                // Different text opacity for selected vs unselected state
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
        }
    }
}