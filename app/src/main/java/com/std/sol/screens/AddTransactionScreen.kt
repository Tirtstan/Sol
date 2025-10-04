package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavController, userViewModel: UserViewModel?) {
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

    var amount by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val categories by categoryViewModel.getAllCategories(userId).collectAsState(initial = emptyList())

    // Set default category
    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Ivory
                    )
                }
                Text(
                    text = "NEW TRANSACTION",
                    color = Ivory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
                Box(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Amount Display
            Text(
                text = "R${if (amount.isBlank()) "0.00" else String.format("%.2f", amount.toDoubleOrNull() ?: 0.0)}",
                color = Ivory,
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
                    // Only allow numbers and one decimal point with max 2 decimal places
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

            // Category Selection
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .menuAnchor()
                        .clickable { expandedCategory = !expandedCategory },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.2f)
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
                                .background(
                                    getCategoryColor(selectedCategory?.name ?: ""),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(selectedCategory?.name ?: ""),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Category: ${selectedCategory?.name ?: "Select Category"}",
                            color = Ivory,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SpaceMonoFont,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Ivory
                        )
                    }
                }

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
                                            .size(24.dp)
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

            // Date Selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.2f)
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
                            .background(Color.Gray, CircleShape),
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
                        color = Ivory,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = SpaceMonoFont,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                calendar.add(Calendar.DAY_OF_YEAR, -1)
                                selectedDate = calendar.time
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous day",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                                selectedDate = calendar.time
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next day",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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

            // Save Button
            SpaceButton(
                text = "SAVE",
                onClick = {
                    if (amount.isNotBlank() && transactionName.isNotBlank() && selectedCategory != null) {
                        val transaction = Transaction(
                            userId = userId,
                            categoryId = selectedCategory!!.id,
                            name = transactionName,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = selectedDate,
                            note = note.ifBlank { null },
                            type = selectedType
                        )
                        transactionViewModel.addTransaction(transaction)
                        navController.navigateUp()
                    }
                },
                enabled = amount.isNotBlank() && transactionName.isNotBlank() && selectedCategory != null,
                modifier = Modifier.fillMaxWidth()
            )
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
                            selectedDate = Date(millis)
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
            containerColor = if (isSelected) RoyalBright else Color.White.copy(alpha = 0.1f)
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
@Preview
@Composable
fun AddTransactionScreenPreview()
{
    SolTheme { AddTransactionScreen(rememberNavController(),null) }
}