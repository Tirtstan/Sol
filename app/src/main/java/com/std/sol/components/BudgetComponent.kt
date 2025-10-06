package com.std.sol.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.std.sol.entities.Budget
import com.std.sol.ui.theme.SolTheme
import com.std.sol.ui.theme.DeepSpaceBase
import com.std.sol.ui.theme.Ocean
import java.util.Date
import kotlin.random.Random


@Composable
fun BudgetComponent(
    budget: Budget,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Optional click handler for navigating to details
) {
    val progress = (budget.currentAmount / budget.maxGoalAmount).coerceIn(0.0, 1.0).toFloat()
    val progressColor = if (progress >= 1.0f) Color.Red else Ocean // Highlight if over budget

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepSpaceBase,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title and Goal Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$${String.format("%.2f", budget.maxGoalAmount)} Goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Amount Spent/Saved
            Text(
                text = "$${String.format("%.2f", budget.currentAmount)} Spent",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Text (e.g., X% reached or X remaining)
            Text(
                text = if (progress < 1.0f) {
                    val remaining = budget.maxGoalAmount - budget.currentAmount
                    "$${String.format("%.2f", remaining)} remaining"
                } else {
                    "Budget exceeded!"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (progress >= 1.0f) Color.Red.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// Preview Composables
@Preview(showBackground = false)
@Composable
fun BudgetComponentPreview_UnderBudget() {
    SolTheme {
        BudgetComponent(
            budget = Budget(
                id = 1,
                userId = 1,
                categoryId = 1,
                name = "Groceries",
                description = "Monthly food budget",
                currentAmount = 150.0,
                minGoalAmount = 300.0,
                maxGoalAmount = 400.0,
                startDate = Date(),
                endDate = Date()
            )
        )
    }
}

@Preview(showBackground = false)
@Composable
fun BudgetComponentPreview_OverBudget() {
    SolTheme {
        BudgetComponent(
            budget = Budget(
                id = 2,
                userId = 1,
                categoryId = 2,
                name = "Dining Out",
                description = "Weekend restaurant spending",
                currentAmount = 150.0,
                minGoalAmount = 300.0,
                maxGoalAmount = 400.0,
                startDate = Date(),
                endDate = Date()
            )
        )
    }
}