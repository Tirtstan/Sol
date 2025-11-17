package com.std.sol.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.std.sol.SessionManager
import com.std.sol.entities.User
import com.std.sol.repositories.BudgetRepository
import com.std.sol.repositories.CategoryRepository
import com.std.sol.repositories.TransactionRepository
import com.std.sol.repositories.UserRepository
import com.std.sol.ui.theme.*
import com.std.sol.viewmodels.CustomizeDashboardViewModel
import com.std.sol.viewmodels.UserViewModel
import com.std.sol.viewmodels.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeDashboardScreen(
    userViewModel: UserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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

    //get user and ViewModel
    val user: User? by userViewModel.currentUser.collectAsState()
    val userId = user?.id ?: ""
    val viewModel: CustomizeDashboardViewModel = viewModel(factory = factory)

    //state for UI
    val allWidgets = viewModel.allWidgets
    val enabledWidgets = viewModel.enabledWidgets

    //load settings once the screen starts
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadSettings(userId)
        }
    }


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Text(
                        text = "CUSTOMIZE DASHBOARD",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Ivory
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Ivory
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Toggle widgets to show or hide them on your dashboard. Changes save automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ivory.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Widget list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allWidgets) { widget ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    IndigoDark.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val widgetName = widget.name
                                .replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.titlecase() }

                            Text(
                                text = widgetName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Ivory,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = widget in enabledWidgets,
                                onCheckedChange = { isChecked ->
                                    // Prevent disabling the last widget
                                    if (!isChecked && enabledWidgets.size == 1) {
                                        return@Switch // Don't allow disabling the last widget
                                    }
                                    
                                    if (isChecked) {
                                        enabledWidgets.add(widget)
                                    } else {
                                        enabledWidgets.remove(widget)
                                    }
                                    // Save immediately when toggle changes
                                    if (userId.isNotBlank() && enabledWidgets.isNotEmpty()) {
                                        viewModel.saveSettings(userId)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Lime,
                                    checkedTrackColor = Leaf.copy(alpha = 0.5f),
                                    uncheckedThumbColor = Ivory.copy(alpha = 0.5f),
                                    uncheckedTrackColor = IndigoDark
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
