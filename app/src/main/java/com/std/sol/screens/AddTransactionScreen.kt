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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    userViewModel: UserViewModel?,
    transactionToEdit: Transaction? = null,
    onTransactionDeleted: ((Transaction) -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
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

    // STATE (pre-filled in edit mode)
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var transactionName by remember { mutableStateOf(transactionToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var selectedDate by remember { mutableStateOf(transactionToEdit?.date ?: Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(transactionToEdit?.imagePath?.let { Uri.parse(it) }) }

    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    // Set default category or one corresponding to edit transaction
    LaunchedEffect(categories, transactionToEdit) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = transactionToEdit?.let { t -> categories.find { it.id == t.categoryId } } ?: categories.first()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
        }
    }

    // ---- TIME PICKER ----
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // SCROLL STATE - Added for scrolling functionality
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0c1327), Color(0xFF25315e), Color(0xFF19102e))
                )
            )
    ) {
        StarryBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Fixed Header (outside of scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = if (transactionToEdit == null) "NEW TRANSACTION" else "EDIT TRANSACTION",
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
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

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState) // Added scrolling here
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp), // Added bottom padding for save button space
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Amount Display
                Text(
                    text = "R${if (amount.isBlank()) "0.00" else String.format("%.2f", amount.toDoubleOrNull() ?: 0.0)}",
                    color = Color(0xFFFFFDF0),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Transaction Type Buttons
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

                // Amount Input
                SpaceTextField(
                    value = amount,
                    onValueChange = { newValue ->
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

                // Transaction Name
                SpaceTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    label = "Transaction Name",
                    placeholder = "Enter name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection - UPDATED to use entity colors and icons
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
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
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
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Image Picker Row
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
                        // Remove image button (if image is selected)
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

                // Display selected image
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

                // Date and Time Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { showDatePicker = true },
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
                        Text(
                            text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)}",
                            color = Color(0xFFFFFDF0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SpaceMonoFont,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showTimePicker = true }
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Pick time",
                                tint = Color(0xFFF4C047)
                            )
                        }
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

                // Note Field
                SpaceTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note (Optional)",
                    placeholder = "Add a note",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Save & Delete Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                    SpaceButton(
                        text = if (transactionToEdit == null) "SAVE" else "UPDATE",
                        onClick = {
                            if (amount.isNotBlank() && transactionName.isNotBlank() && selectedCategory != null) {
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
                                if (transactionToEdit == null) {
                                    transactionViewModel.addTransaction(transaction)
                                } else {
                                    transactionViewModel.updateTransaction(transaction)
                                }
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            }
                        },
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
                            // preserve time of day
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
                val cal = Calendar.getInstance().apply { time = selectedDate }
                cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                cal.set(Calendar.MINUTE, selectedMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDate = cal.time
                showTimePicker = false
            },
            hour, minute, true
        ).apply { show() }
    }
}

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
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMonoFont
            )
        }
    }
}