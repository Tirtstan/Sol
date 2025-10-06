package com.std.sol.screens

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.std.sol.SessionManager
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.databases.DatabaseProvider
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.entities.TransactionType
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.TransactionViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    userViewModel: UserViewModel?,
    modifier: Modifier = Modifier,
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
    var selectedType by remember {
        mutableStateOf(
            transactionToEdit?.type ?: TransactionType.EXPENSE
        )
    }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var selectedDate by remember { mutableStateOf(transactionToEdit?.date ?: Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf(transactionToEdit?.imagePath?.toUri()) }

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    // Set default category or one corresponding to edit transaction
    LaunchedEffect(categories, transactionToEdit) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory =
                transactionToEdit?.let { t -> categories.find { it.id == t.categoryId } }
                    ?: categories.first()
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

    // ---- TIME PICKER STATE ----
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    // ---- DIALOGS ----

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // Keep the time, only change the date
                        val newDateCalendar = Calendar.getInstance().apply { timeInMillis = it }
                        val currentCalendar = Calendar.getInstance().apply { time = selectedDate }
                        currentCalendar.set(
                            newDateCalendar.get(Calendar.YEAR),
                            newDateCalendar.get(Calendar.MONTH),
                            newDateCalendar.get(Calendar.DAY_OF_MONTH)
                        )
                        selectedDate = currentCalendar.time
                    }
                    showDatePicker = false
                }) { Text("OK", style = TextStyle(fontFamily = InterFont)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Cancel",
                        style = TextStyle(fontFamily = InterFont)
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        BasicAlertDialog(
            onDismissRequest = { showTimePicker = false },
            modifier = Modifier.fillMaxWidth(),
            properties = DialogProperties(), content = {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(
                                "Cancel",
                                style = TextStyle(fontFamily = InterFont)
                            )
                        }
                        TextButton(
                            onClick = {
                                val newTimeCalendar =
                                    Calendar.getInstance().apply { time = selectedDate }
                                newTimeCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                newTimeCalendar.set(Calendar.MINUTE, timePickerState.minute)
                                selectedDate = newTimeCalendar.time
                                showTimePicker = false
                            }
                        ) { Text("OK", style = TextStyle(fontFamily = InterFont)) }
                    }
                }
            })
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (keep SpaceMonoFont per request)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (transactionToEdit == null) "NEW TRANSACTION" else "EDIT TRANSACTION",
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
            }

            val formattedAmount = if (amount.isBlank()) "0.00" else String.format(
                "%.2f",
                amount.toDoubleOrNull() ?: 0.0
            )
            val sign = if (selectedType == TransactionType.INCOME) "+" else "-"
            val amountColor =
                if (selectedType == TransactionType.INCOME) Color(0xFF6BE051) else Color(
                    0xFFE05E51
                )

            Text(
                text = "${sign}R${formattedAmount}",
                color = amountColor,
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
            
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                val fillMaxWidth = Modifier
                    .fillMaxWidth()
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            text = "Category",
                            style = TextStyle(fontFamily = SpaceMonoFont, color = Ivory)
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = fillMaxWidth
                        .clickable { expandedCategory = true },
                    textStyle = TextStyle(fontFamily = InterFont, color = Color.White)
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
                                                getCategoryColor(category.name),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(category.name),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        category.name,
                                        style = TextStyle(
                                            fontFamily = InterFont,
                                            color = Color.White
                                        )
                                    )
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
                    fontFamily = InterFont
                )
                IconButton(
                    onClick = {
                        val intent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 4.dp)
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
                        text = "Date: ${
                            SimpleDateFormat(
                                "MMM dd, yyyy",
                                Locale.getDefault()
                            ).format(selectedDate)
                        }",
                        color = Color(0xFFFFFDF0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFont,
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
                        fontFamily = InterFont,
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
        }
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

@Preview(showBackground = true, backgroundColor = 0xFF0C1327)
@Composable
fun AddTransactionScreenPreview() {
    SolTheme {
        AddTransactionScreen(rememberNavController(), null)
    }
}
