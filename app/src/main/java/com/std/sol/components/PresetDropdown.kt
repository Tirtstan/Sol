package com.std.sol.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.std.sol.entities.Category
import com.std.sol.entities.Transaction
import com.std.sol.ui.theme.InterFont
import com.std.sol.ui.theme.SpaceMonoFont
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CombinedPresetsDropdown(
    recentTransactions: List<Transaction>,
    categories: List<Category>,
    onPresetSelected: (name: String, category: Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3a5c85)
            ),
            shape = RoundedCornerShape(15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use Preset or Recent",
                    color = Color(0xFFFFFDF0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = InterFont
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Presets",
                    tint = Color(0xFFF4C047),
                    modifier = Modifier.then(
                        if (expanded) Modifier.rotate(180f) else Modifier
                    )
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .background(Color(0xFF1a2a3a)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1a2a3a)
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Recent Transactions Section
                    if (recentTransactions.isNotEmpty()) {
                        item {
                            Text(
                                text = "RECENT EXPENSES",
                                color = Color(0xFFF4C047),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceMonoFont,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        items(recentTransactions) { transaction ->
                            RecentTransactionButton(
                                transaction = transaction,
                                onSelected = {
                                    val matchingCategory = categories.find {
                                        it.id == transaction.categoryId
                                    }
                                    onPresetSelected(transaction.name, matchingCategory)
                                    expanded = false
                                }
                            )
                        }

                        item {
                            HorizontalDivider(
                                color = Color(0xFF3a5c85),
                                thickness = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Quick Presets Section
                    item {
                        Text(
                            text = "QUICK PRESETS",
                            color = Color(0xFFF4C047),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceMonoFont,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }

                    items(PRESET_EXPENSES) { preset ->
                        PresetExpenseButton(
                            preset = preset,
                            onSelected = {
                                val matchingCategory = categories.find {
                                    it.name.equals(preset.category, ignoreCase = true)
                                }
                                onPresetSelected(preset.name, matchingCategory)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionButton(
    transaction: Transaction,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a3f57)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // History icon background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF56A1BF), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recent",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.name,
                    color = Color(0xFFFFFDF0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SpaceMonoFont
                )
                Text(
                    text = "R${String.format("%.2f", transaction.amount)} • ${
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(transaction.getDateAsDate())
                    }",
                    color = Color(0xFFFFFDF0).copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = InterFont
                )
            }

            // Arrow indicator
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFFF4C047),
                modifier = Modifier.rotate(270f)
            )
        }
    }
}

@Composable
fun PresetExpenseButton(
    preset: PresetExpenseItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF243550)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(preset.color, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = preset.name,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = preset.name,
                    color = Color(0xFFFFFDF0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SpaceMonoFont
                )
                Text(
                    text = preset.category,
                    color = Color(0xFFFFFDF0).copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = InterFont
                )
            }

            // Arrow indicator
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFFF4C047),
                modifier = Modifier.rotate(270f)
            )
        }
    }
}