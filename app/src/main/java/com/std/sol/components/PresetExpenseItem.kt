package com.std.sol.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PresetExpenseItem(
    val name: String,
    val category: String,
    val suggestedAmount: Double = 0.0,
    val icon: ImageVector,
    val color: Color
)

// Hardcoded preset expenses using Material Design Icons
val PRESET_EXPENSES = listOf(
    PresetExpenseItem(
        name = "Grocery Shopping",
        category = "Food",
        suggestedAmount = 0.0,
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFF57C52B) // Leaf
    ),
    PresetExpenseItem(
        name = "Restaurant",
        category = "Food",
        suggestedAmount = 0.0,
        icon = Icons.Default.Restaurant,
        color = Color(0xFFB9ED5E) // Lime
    ),
    PresetExpenseItem(
        name = "Gas / Fuel",
        category = "Transportation",
        suggestedAmount = 0.0,
        icon = Icons.Default.LocalGasStation,
        color = Color(0xFFFEB58B) // Peach
    ),
    PresetExpenseItem(
        name = "Car Maintenance",
        category = "Transportation",
        suggestedAmount = 0.0,
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFFF4680B) // Orange
    ),
    PresetExpenseItem(
        name = "Movie / Entertainment",
        category = "Entertainment",
        suggestedAmount = 0.0,
        icon = Icons.Default.MovieFilter,
        color = Color(0xFF8F1767) // Magenta
    ),
    PresetExpenseItem(
        name = "Gaming",
        category = "Entertainment",
        suggestedAmount = 0.0,
        icon = Icons.Default.SportsEsports,
        color = Color(0xFF465BE7) // RoyalBright
    ),
    PresetExpenseItem(
        name = "Sports / Gym",
        category = "Sports",
        suggestedAmount = 0.0,
        icon = Icons.Default.FitnessCenter,
        color = Color(0xFF2AC0F2) // Sky
    ),
    PresetExpenseItem(
        name = "Sports Equipment",
        category = "Sports",
        suggestedAmount = 0.0,
        icon = Icons.Default.FitnessCenter,
        color = Color(0xFF56A1BF) // Ocean
    ),
    PresetExpenseItem(
        name = "Coffee",
        category = "Food",
        suggestedAmount = 0.0,
        icon = Icons.Default.Restaurant,
        color = Color(0xFF431E1E) // EmberDark
    ),
    PresetExpenseItem(
        name = "Shopping",
        category = "Retail",
        suggestedAmount = 0.0,
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFFF45D92) // Rose
    ),
    PresetExpenseItem(
        name = "Electricity Bill",
        category = "Utilities",
        suggestedAmount = 0.0,
        icon = Icons.Default.FlightTakeoff,
        color = Color(0xFFF4C047) // Amber
    ),
    PresetExpenseItem(
        name = "Internet Bill",
        category = "Utilities",
        suggestedAmount = 0.0,
        icon = Icons.Default.Savings,
        color = Color(0xFF97DBD2) // Mist
    ),
    PresetExpenseItem(
        name = "Phone Bill",
        category = "Utilities",
        suggestedAmount = 0.0,
        icon = Icons.Default.CreditCard,
        color = Color(0xFF25315E) // Indigo
    ),
    PresetExpenseItem(
        name = "Healthcare",
        category = "Health",
        suggestedAmount = 0.0,
        icon = Icons.Default.LocalHospital,
        color = Color(0xFFB42313) // Ember
    ),
    PresetExpenseItem(
        name = "Medications",
        category = "Health",
        suggestedAmount = 0.0,
        icon = Icons.Default.LocalHospital,
        color = Color(0xFFE05E51) // Red
    ),
    PresetExpenseItem(
        name = "Work",
        category = "Work",
        suggestedAmount = 0.0,
        icon = Icons.Default.Work,
        color = Color(0xFF3498DB) // Blue
    ),
    PresetExpenseItem(
        name = "Travel / Flight",
        category = "Transportation",
        suggestedAmount = 0.0,
        icon = Icons.Default.Flight,
        color = Color(0xFF2ECC71) // Green
    ),
    PresetExpenseItem(
        name = "Pets",
        category = "Pets",
        suggestedAmount = 0.0,
        icon = Icons.Default.Pets,
        color = Color(0xFFF39C12) // Orange
    ),
    PresetExpenseItem(
        name = "School / Education",
        category = "Education",
        suggestedAmount = 0.0,
        icon = Icons.Default.School,
        color = Color(0xFF9B59B6) // Purple
    ),
    PresetExpenseItem(
        name = "Business",
        category = "Business",
        suggestedAmount = 0.0,
        icon = Icons.Default.BusinessCenter,
        color = Color(0xFF1ABC9C) // Teal
    ),
    PresetExpenseItem(
        name = "Bank / Finance",
        category = "Financial",
        suggestedAmount = 0.0,
        icon = Icons.Default.AccountBalance,
        color = Color(0xFFE67E22) // Dark Orange
    ),
    PresetExpenseItem(
        name = "Home",
        category = "Home",
        suggestedAmount = 0.0,
        icon = Icons.Default.Home,
        color = Color(0xFF34495E) // Dark Blue Gray
    )
)