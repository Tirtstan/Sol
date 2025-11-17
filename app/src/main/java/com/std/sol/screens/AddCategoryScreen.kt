package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.std.sol.components.SpaceButton
import com.std.sol.components.SpaceTextField
import com.std.sol.components.StarryBackground
import com.std.sol.entities.Category
import com.std.sol.entities.User
import com.std.sol.ui.theme.*
import com.std.sol.utils.*
import com.std.sol.viewmodels.CategoryViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory
import com.std.sol.SessionManager
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import androidx.core.graphics.toColorInt

@Composable
fun AddCategoryScreen(
    navController: NavController,
    userViewModel: UserViewModel?,
    categoryToEdit: Category? = null,
    onClose: (() -> Unit)? = null
) {
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
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = "", username = "John Doe"))
    }
    val userId = user?.id ?: ""

    // STATE (pre-filled in edit mode)
    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColor by remember {
        mutableStateOf(categoryToEdit?.color?.let { Color(it.toColorInt()) } ?: predefinedColors[0])
    }
    var selectedIcon by remember {
        mutableStateOf(categoryToEdit?.icon?.let { getIconFromString(it) } ?: predefinedIcons[0])
    }

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
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = if (categoryToEdit == null) "NEW CATEGORY" else "EDIT CATEGORY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFFFFDF0)
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Category Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(selectedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Name Input
                SpaceTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = "Category Name",
                    placeholder = "Enter category name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Color Selection
                Text(
                    text = "SELECT COLOR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFFFFDF0),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(predefinedColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = Color(0xFFF4C047),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Icon Selection
                Text(
                    text = "SELECT ICON",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFFFFDF0),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(predefinedIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (selectedIcon == icon) Color(0xFF3a5c85) else Color.White.copy(
                                        alpha = 0.1f
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (selectedIcon == icon) 2.dp else 0.dp,
                                    color = Color(0xFFF4C047),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFFFFFDF0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save/Update and Delete Buttons (if editing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (categoryToEdit != null) Arrangement.spacedBy(16.dp) else Arrangement.Center
                ) {
                    // Delete button (only shown when editing)
                    if (categoryToEdit != null) {
                        SpaceButton(
                            text = "DELETE",
                            onClick = {
                                categoryViewModel.deleteCategory(userId, categoryToEdit)
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            },
                            modifier = Modifier.weight(1f),
                            gradientColors = listOf(Color(0xFFf45d92), Color(0xFFb42313)),
                            shadowColor = Color(0xFFf45d92),
                            borderColor = Color(0xFFb42313)
                        )
                    }

                    // Save/Update Button
                    SpaceButton(
                        text = if (categoryToEdit == null) "CREATE CATEGORY" else "UPDATE CATEGORY",
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                // Fix the color conversion
                                val colorInt = selectedColor.toArgb()
                                val hexColor = String.format("#%08X", colorInt)

                                val category = Category(
                                    id = categoryToEdit?.id ?: "",
                                    userId = userId,
                                    name = categoryName,
                                    color = hexColor,
                                    icon = getStringFromIcon(selectedIcon)
                                )
                                if (categoryToEdit == null) {
                                    categoryViewModel.addCategory(userId, category)
                                } else {
                                    categoryViewModel.updateCategory(userId, category)
                                }
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            }
                        },
                        enabled = categoryName.isNotBlank(),
                        modifier = if (categoryToEdit != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}