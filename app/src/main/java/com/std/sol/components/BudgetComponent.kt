package com.std.sol.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.std.sol.entities.Budget
import com.std.sol.entities.Category
import com.std.sol.screens.getCategoryColor
import com.std.sol.screens.getCategoryIcon
import com.std.sol.ui.theme.Indigo
import com.std.sol.ui.theme.IndigoLight
import com.std.sol.ui.theme.InterFont
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.Mist
import com.std.sol.ui.theme.Ocean
import com.std.sol.ui.theme.Sky
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.SpaceMonoFont
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetComponent(
    budget: Budget,
    category: Category?,
    currentAmount: Double = 0.0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val progress = (currentAmount / budget.maxGoalAmount).coerceIn(0.0, 1.0).toFloat()

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                listOf(Indigo, IndigoLight)
                            )
                    )
                    .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(category?.name ?: "other")),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category?.name ?: ""),
                        contentDescription = category?.name,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = budget.name,
                            color = Color(0xFFFFFDF0),
                            fontFamily = SpaceMonoFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val descriptionText = budget.description
                    if (!descriptionText.isNullOrBlank()) {
                        Text(
                            text = descriptionText,
                            color = Ivory.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontFamily = InterFont,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Ocean.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = dateFormat.format(budget.startDate.toDate()),
                                    color = Sky.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Arrow icon
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Ivory.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )

                        // End date chip
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Mist.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = dateFormat.format(budget.endDate.toDate()),
                                    color = Mist.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (progress >= 1.0f) Color.Red else Color(0xFF57c52b),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "R${"%.2f".format(currentAmount)} spent",
                                color = Color(0xFFFFFDF0),
                                fontFamily = SpaceMonoFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Goal R${"%.2f".format(budget.maxGoalAmount)}",
                                color = Ivory.copy(alpha = 0.9f),
                                fontFamily = SpaceMonoFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun BudgetComponentPreview_UnderBudget() {
    SolTheme {
        BudgetComponent(
            budget =
                Budget(
                    id = "1",
                    userId = "1",
                    categoryId = "1",
                    name = "Groceries",
                    description = "Monthly food budget",
                    minGoalAmount = 300.0,
                    maxGoalAmount = 400.0,
                    startDate = Timestamp.now(),
                    endDate = Timestamp.now()
                ),
            category = null,
            currentAmount = 250.0
        )
    }
}

@Preview(showBackground = false)
@Composable
fun BudgetComponentPreview_OverBudget() {
    SolTheme {
        BudgetComponent(
            budget =
                Budget(
                    id = "2",
                    userId = "1",
                    categoryId = "2",
                    name = "Dining Out",
                    description = "Weekend restaurant spending",
                    minGoalAmount = 300.0,
                    maxGoalAmount = 400.0,
                    startDate = Timestamp.now(),
                    endDate = Timestamp.now()
                ),
            category = null,
            currentAmount = 450.0
        )
    }
}
