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
import com.std.sol.components.CategoryDropdown
import com.std.sol.entities.Budget
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.SpaceMonoFont
import com.std.sol.viewmodels.BudgetViewModel
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    navController: NavController,
    userId: String,
    budgetId: String,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val transactionRepository = remember { TransactionRepository() }
    val categoryRepository = remember { CategoryRepository() }
    val budgetRepository = remember { BudgetRepository() }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val factory = remember { 
        ViewModelFactory(
            userRepository,
            transactionRepository,
            categoryRepository,
            budgetRepository,
            sessionManager
        )
    }

    val budgetViewModel: BudgetViewModel = viewModel(factory = factory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = factory)

    val isEditing = budgetId.isNotBlank()

    // Use rememberSaveable for new budgets, but regular remember for editing to ensure state updates properly
    var name by if (isEditing) remember { mutableStateOf("") } else rememberSaveable { mutableStateOf("") }
    var description by if (isEditing) remember { mutableStateOf("") } else rememberSaveable { mutableStateOf("") }
    var minGoal by if (isEditing) remember { mutableStateOf("") } else rememberSaveable { mutableStateOf("") }
    var maxGoal by if (isEditing) remember { mutableStateOf("") } else rememberSaveable { mutableStateOf("") }
    var startDate by if (isEditing) remember { mutableStateOf(Timestamp.now()) } else rememberSaveable { mutableStateOf(Timestamp.now()) }
    var endDate by if (isEditing) remember { mutableStateOf(Timestamp.now()) } else rememberSaveable { mutableStateOf(Timestamp.now()) }

    var selectedCategoryId by if (isEditing) remember { mutableStateOf<String?>(null) } else rememberSaveable { mutableStateOf<String?>(null) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditing) }

    val categories by categoryViewModel.getAllCategories(userId)
        .collectAsState(initial = emptyList())

    val selectedCategory = remember(selectedCategoryId, categories) {
        categories.find { it.id == selectedCategoryId }
    }

    LaunchedEffect(budgetId, userId) {
        if (isEditing && userId.isNotBlank() && budgetId.isNotBlank()) {
            isLoading = true
            try {
                val existing = budgetViewModel.getBudgetById(userId, budgetId)
                if (existing != null) {
                    name = existing.name
                    description = existing.description ?: ""
                    minGoal = "%.2f".format(existing.minGoalAmount)
                    maxGoal = "%.2f".format(existing.maxGoalAmount)
                    startDate = existing.startDate
                    endDate = existing.endDate
                    selectedCategoryId = existing.categoryId
                    isLoading = false
                } else {
                    isLoading = false
                    onClose?.invoke() ?: navController.popBackStack()
                }
            } catch (e: Exception) {
                isLoading = false
                // Handle error - could show error message
                onClose?.invoke() ?: navController.popBackStack()
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(categories, isEditing) {
        // Only auto-select first category when creating a new budget, not when editing
        if (!isEditing && selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formValid = remember(name, maxGoal, selectedCategoryId, isLoading) {
        !isLoading && 
        name.isNotBlank() && 
        maxGoal.toDoubleOrNull() != null && 
        maxGoal.toDoubleOrNull()!! > 0.0 && 
        selectedCategoryId != null &&
        selectedCategoryId!!.isNotBlank()
    }

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

            SpaceTextField(
                value = name,
                onValueChange = { name = it },
                label = "Budget Name",
                placeholder = "Groceries",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            CategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategoryId = category.id
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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

            SpaceTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (optional)",
                placeholder = "Monthly food budget",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        "Start: ${dateFormat.format(startDate.toDate())}",
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
                        "End: ${dateFormat.format(endDate.toDate())}", color = Ivory,
                        fontFamily = SpaceMonoFont,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (showStartPicker) {
                val state = rememberDatePickerState(initialSelectedDateMillis = startDate.toDate().time)
                DatePickerDialog(
                    onDismissRequest = { showStartPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let {
                                startDate = Timestamp(Date(it))
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
                val state = rememberDatePickerState(initialSelectedDateMillis = endDate.toDate().time)
                DatePickerDialog(
                    onDismissRequest = { showEndPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let {
                                endDate = Timestamp(Date(it))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing) {
                    SpaceButton(
                        text = "DELETE",
                        onClick = {
                            scope.launch {
                                if (userId.isNotBlank() && budgetId.isNotBlank()) {
                                    budgetViewModel.getBudgetById(userId, budgetId)
                                        ?.let { budgetViewModel.deleteBudget(userId, it) }
                                }
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
                                id = if (isEditing) budgetId else "",
                                userId = userId,
                                categoryId = selectedCategoryId ?: "",
                                name = name,
                                description = description.ifBlank { null },
                                minGoalAmount = minGoal.toDoubleOrNull() ?: 0.0,
                                maxGoalAmount = maxGoal.toDoubleOrNull() ?: 0.0,
                                startDate = startDate,
                                endDate = endDate
                            )
                            if (isEditing) {
                                budgetViewModel.updateBudget(userId, budget)
                            } else {
                                budgetViewModel.addBudget(userId, budget)
                            }
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
    SolTheme { AddEditBudgetScreen(rememberNavController(), "", "") }
}
