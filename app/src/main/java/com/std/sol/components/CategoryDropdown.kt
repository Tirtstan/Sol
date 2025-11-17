package com.std.sol.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.std.sol.entities.Category
import com.std.sol.ui.theme.Ivory
import com.std.sol.ui.theme.InterFont
import com.std.sol.ui.theme.SpaceMonoFont
import com.std.sol.utils.getCategoryColorFromEntity
import com.std.sol.utils.getCategoryIconFromEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Category",
    placeholder: String = "Select Category"
) {
    var expanded by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val valueTextColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    val placeholderColor =
        if (isDarkTheme) Ivory.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val displayTextColor = if (selectedCategory == null) placeholderColor else valueTextColor

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: placeholder,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = SpaceMonoFont,
                        color = if (isSystemInDarkTheme()) Ivory else MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            textStyle = TextStyle(
                fontFamily = InterFont,
                color = displayTextColor
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF56a1bf),
                unfocusedBorderColor = if (isDarkTheme) Color(0xFFFFFDF0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedTextColor = displayTextColor,
                unfocusedTextColor = displayTextColor,
                cursorColor = Color(0xFF56a1bf),
                focusedLabelColor = if (isDarkTheme) Color(0xFFFFFDF0) else MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = if (isDarkTheme) Color(0xFFFFFDF0) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                if (isDarkTheme) Color(0xFF1a2a3a) else MaterialTheme.colorScheme.surface
            )
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        getCategoryColorFromEntity(category),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIconFromEntity(category),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                category.name,
                                style = TextStyle(
                                    fontFamily = InterFont,
                                    color = if (isDarkTheme) Color(0xFFFFFDF0) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

