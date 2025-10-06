package com.std.sol.screens

import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.std.sol.SessionManager
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.databases.DatabaseProvider
import com.std.sol.entities.Budget
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.SpaceMonoFont
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    navController: NavController,
    userId: Int,
    budgetId: Int,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DatabaseProvider.getDatabase(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { ViewModelFactory(db, sessionManager) }

    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)

    val isEditing = budgetId != 0

    // Form state
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var minGoal by rememberSaveable { mutableStateOf("") }
    var maxGoal by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(Date()) }
    var endDate by rememberSaveable { mutableStateOf(Date()) }

    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    val selectedCategory = remember(selectedCategoryId, categories) {
        categories.find { it.id == selectedCategoryId }
    }

    // Load budget when editing
    LaunchedEffect(budgetId, userId) {
        if (isEditing) {
            val existing = budgetViewModel.getBudgetById(budgetId)
            existing?.let { b ->
                name = b.name
                description = b.description ?: ""
                minGoal = "%.2f".format(b.minGoalAmount)
                maxGoal = "%.2f".format(b.maxGoalAmount)
                startDate = b.startDate
                endDate = b.endDate
                // 3. Set the ID instead of the object
                selectedCategoryId = b.categoryId
            } ?: run {
                onClose?.invoke() ?: navController.popBackStack()
            }
        }
        isLoading = false
    }

    // Set category default after categories load
    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formValid =
        name.isNotBlank() && maxGoal.toDoubleOrNull() != null && maxGoal.toDouble()!! > 0.0 && selectedCategory != null

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Header and other fields are unchanged)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "EDIT BUDGET" else "NEW BUDGET",
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            // Name
            SpaceTextField(
                value = name,
                onValueChange = { name = it },
                label = "Budget Name",
                placeholder = "Groceries",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    // Uses derived object for display
                    value = selectedCategory?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category", color = Color(0xFFFFFDF0)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCategory = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF56a1bf),
                        unfocusedBorderColor = Color(0xFFFFFDF0),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                // 4. Update the ID on selection
                                selectedCategoryId = cat.id
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // ... (Other fields are unchanged)
            Spacer(modifier = Modifier.height(12.dp))

            // Amounts row: min, max
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpaceTextField(
                    value = minGoal,
                    onValueChange = { v ->
                        if (v.isEmpty() || Regex("^\\d*\\.?\\d{0,2}$").matches(v)) minGoal = v
                    },
                    label = "Min",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                SpaceTextField(
                    value = maxGoal,
                    onValueChange = { v ->
                        if (v.isEmpty() || Regex("^\\d*\\.?\\d{0,2}$").matches(v)) maxGoal = v
                    },
                    label = "Max *",
                    placeholder = "1000.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            SpaceTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                placeholder = "Monthly food budget",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date pickers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showStartPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF465be7)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Start: ${dateFormat.format(startDate)}",
                        color = Ivory,
                        fontFamily = SpaceMonoFont,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = { showEndPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF465be7)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "End: ${dateFormat.format(endDate)}", color = Ivory,
                        fontFamily = SpaceMonoFont,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Date picker dialogs
            if (showStartPicker) {
                val state = rememberDatePickerState(initialSelectedDateMillis = startDate.time)
                DatePickerDialog(
                    onDismissRequest = { showStartPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let {
                                startDate = Date(it)
                            }
                            showStartPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = state) }
            }
            if (showEndPicker) {
                val state = rememberDatePickerState(initialSelectedDateMillis = endDate.time)
                DatePickerDialog(
                    onDismissRequest = { showEndPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let {
                                endDate = Date(it)
                            }
                            showEndPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
                    }
                ) { DatePicker(state = state) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing) {
                    SpaceButton(
                        text = "DELETE",
                        onClick = {
                            scope.launch {
                                budgetViewModel.getBudgetById(budgetId)
                                    ?.let { budgetViewModel.deleteBudget(it) }
                                onClose?.invoke() ?: navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        gradientColors = listOf(Color(0xFFf45d92), Color(0xFFb42313)),
                        shadowColor = Color(0xFFf45d92),
                        borderColor = Color(0xFFb42313)
                    )
                }
                SpaceButton(
                    text = if (isEditing) "UPDATE" else "SAVE",
                    onClick = {
                        if (!formValid) return@SpaceButton
                        scope.launch {
                            val budget = Budget(
                                id = if (isEditing) budgetId else 0,
                                userId = userId,
                                // 5. Use the ID when creating the Budget object
                                categoryId = selectedCategoryId ?: 0,
                                name = name,
                                description = description.ifBlank { null },
                                minGoalAmount = minGoal.toDoubleOrNull() ?: 0.0,
                                maxGoalAmount = maxGoal.toDoubleOrNull() ?: 0.0,
                                startDate = startDate,
                                endDate = endDate
                            )
                            if (isEditing) budgetViewModel.updateBudget(budget) else budgetViewModel.addBudget(
                                budget
                            )
                            onClose?.invoke() ?: navController.popBackStack()
                        }
                    },
                    enabled = formValid,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C1327)
@Composable
fun AddEditBudgetScreenModalPreview() {
    SolTheme { AddEditBudgetScreen(rememberNavController(), 0, 0) }
}
