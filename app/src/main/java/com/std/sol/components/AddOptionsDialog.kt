package com.std.sol.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.std.sol.ui.theme.Indigo
import com.std.sol.ui.theme.IndigoLight
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.SpaceMonoFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOptionsDialog(
    onDismiss: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCategory: () -> Unit,
    onAddBudget: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    LaunchedEffect(Unit) {
        sheetState.expand()
    }
    
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
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Indigo,
                            IndigoLight
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ADD NEW",
                    color = Color(0xFFFFFDF0),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )

                Spacer(modifier = Modifier.height(20.dp))

                AddOptionItem(
                    icon = Icons.Default.AttachMoney,
                    title = "New Transaction",
                    subtitle = "Add income or expense",
                    onClick = {
                        onAddTransaction()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AddOptionItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "New Budget",
                    subtitle = "Create spending budget",
                    onClick = {
                        onAddBudget()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AddOptionItem(
                    icon = Icons.Default.Category,
                    title = "New Category",
                    subtitle = "Create expense category",
                    onClick = {
                        onAddCategory()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
                SpaceButton(text = "Cancel", onClick = onDismiss)
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AddOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3a5c85)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFFf4c047),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF0c1327),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color(0xFFFFFDF0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceMonoFont
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFFFFDF0).copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = SpaceMonoFont
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color(0xFFF4C047)
            )
        }
    }
}

@Preview
@Composable
fun AddOptionsDialogPreview() {
    SolTheme { 
        AddOptionsDialog(
            onDismiss = { }, 
            onAddTransaction = { }, 
            onAddCategory = { },
            onAddBudget = { }
        ) 
    }
}