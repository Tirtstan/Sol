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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.std.sol.databases.DatabaseProvider
import com.std.sol.SessionManager

/**
 * Composable function for adding a new category or editing an existing one.
 * Features a space-themed UI with color and icon selection.
 *
 * @param navController Navigation controller for screen navigation
 * @param userViewModel ViewModel containing current user data
 * @param categoryToEdit Optional category to edit (null for creating new category)
 * @param onClose Optional close callback (if null, uses navController.navigateUp())
 */
@Composable
fun AddCategoryScreen(
    navController: NavController,
    userViewModel: UserViewModel?,
    categoryToEdit: Category? = null,
    onClose: (() -> Unit)? = null
) {
    // Get context and set up ViewModels
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        DatabaseProvider.getDatabase(context),
        SessionManager(context)
    )
    val categoryViewModel: CategoryViewModel = viewModel(factory = viewModelFactory)

    // Get current user from UserViewModel or provide default fallback
    val user: User? by userViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(User(id = -1, username = "John Doe", passwordHash = ""))
    }
    val userId = user?.id ?: return // Exit early if no valid user ID


    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColor by remember {
        mutableStateOf(
            categoryToEdit?.color?.let {
                Color(android.graphics.Color.parseColor(it))
            } ?: predefinedColors[0]
        )
    }
    var selectedIcon by remember {
        mutableStateOf(
            categoryToEdit?.icon?.let {
                getIconFromString(it)
            } ?: predefinedIcons[0]
        )
    }

    // Scroll state for the content area
    val scrollState = rememberScrollState()

    // Main container with space-themed gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0c1327), Color(0xFF25315e), Color(0xFF19102e))
                )
            )
    ) {
        // Animated starry background component for space theme
        StarryBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed header that has a title and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Empty spacer for visual balance
                Spacer(modifier = Modifier.size(24.dp))

                // Dynamic title based on edit/create mode
                Text(
                    text = if (categoryToEdit == null) "NEW CATEGORY" else "EDIT CATEGORY",
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )

                // Close button with callback handling
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

            // For Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // CCategory Preview which shows current selected color and icon
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

                // Category Names
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
                    color = Color(0xFFFFFDF0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of predefined color with selection highlighting
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6), // 6 colors per row
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(predefinedColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, CircleShape)
                                // Golden border for selected color
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

                // ICON SELECTION SECTION
                Text(
                    text = "SELECT ICON",
                    color = Color(0xFFFFFDF0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of predefined icons with selection highlighting
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6), // 6 icons per row
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(predefinedIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                // Different background color for selected icon
                                .background(
                                    if (selectedIcon == icon) Color(0xFF3a5c85) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                // Golden border for selected icon
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

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // Different arrangement based on edit/create mode
                    horizontalArrangement = if (categoryToEdit != null) Arrangement.spacedBy(16.dp) else Arrangement.Center
                ) {
                    // Delete Button Ui which is only shown when editing existing category
                    if (categoryToEdit != null) {
                        SpaceButton(
                            text = "DELETE",
                            onClick = {
                                categoryViewModel.deleteCategory(categoryToEdit)
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            },
                            modifier = Modifier.weight(1f),
                            // Red gradient for delete action
                            gradientColors = listOf(Color(0xFFf45d92), Color(0xFFb42313)),
                            shadowColor = Color(0xFFf45d92),
                            borderColor = Color(0xFFb42313)
                        )
                    }

                    // Save/Update Button
                    SpaceButton(
                        text = if (categoryToEdit == null) "CREATE CATEGORY" else "UPDATE CATEGORY",
                        onClick = {
                            // Only proceed if category name is not blank
                            if (categoryName.isNotBlank()) {
                                // Convert Color to hex string for database storage
                                val colorInt = selectedColor.toArgb()
                                val hexColor = String.format("#%08X", colorInt)

                                // Create category object with current selections
                                val category = Category(
                                    id = categoryToEdit?.id ?: 0, // Use existing ID or 0 for new
                                    userId = userId,
                                    name = categoryName,
                                    color = hexColor,
                                    icon = getStringFromIcon(selectedIcon)
                                )

                                // Call appropriate ViewModel method based on mode
                                if (categoryToEdit == null) {
                                    categoryViewModel.addCategory(category)
                                } else {
                                    categoryViewModel.updateCategory(category)
                                }

                                // Navigate back using provided callback or navigation controller
                                if (onClose != null) onClose()
                                else navController.navigateUp()
                            }
                        },
                        enabled = categoryName.isNotBlank(), // Enabled only when name is provided
                        modifier = if (categoryToEdit != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                    )
                }

                // Bottom spacing for better visual layout
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}