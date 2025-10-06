package com.std.sol.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.std.sol.entities.Budget
import com.std.sol.ui.theme.Ember
import com.std.sol.ui.theme.Leaf
import com.std.sol.ui.theme.SpaceMonoFont
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import com.std.sol.ui.theme.Ocean

@Composable
fun BudgetListItem(budget: Budget, onClick: () -> Unit) {
    val dateFormat: SimpleDateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val dateRange = remember(budget.startDate, budget.endDate) {
        "${dateFormat.format(budget.startDate)} - ${dateFormat.format(budget.endDate)}"
    }

    val progress = (budget.currentAmount / budget.maxGoalAmount).toFloat().coerceIn(0f, 1f)
    val progressColor = when {
        progress >= 1f -> Ember
        progress >= 0.75f -> Ocean
        else -> Leaf
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            //row 1 name and amount
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = SpaceMonoFont),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${budget.currentAmount.toTwoDecimals()} / $${budget.maxGoalAmount.toTwoDecimals()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceMonoFont),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
           //row 2: date range
            Text(
                text = dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            //row 3 progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Spacer( modifier = Modifier.height(8.dp))

            Text(
                text = if (progress >= 1f) "Goal Exceeded!" else "${(progress * 100).toInt()}% Used",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = progressColor
            )
        }
    }
}
private fun Double.toTwoDecimals(): String {
    return  String.format(Locale.getDefault(), "%.2f", this)
}