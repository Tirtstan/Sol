package com.std.sol.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.std.sol.entities.Category

// Convert stored color string to Color object
@Composable
fun getCategoryColorFromEntity(category: Category?): Color {
    return if (category?.color != null) {
        try {
            Color(android.graphics.Color.parseColor(category.color))
        } catch (e: Exception) {
            // Fallback to default color if parsing fails
            getFallbackCategoryColor(category.name)
        }
    } else {
        getFallbackCategoryColor(category?.name ?: "")
    }
}

// Convert stored icon string to ImageVector
@Composable
fun getCategoryIconFromEntity(category: Category?): ImageVector {
    return if (category?.icon != null) {
        getIconFromString(category.icon)
    } else {
        getFallbackCategoryIcon(category?.name ?: "")
    }
}

// Fallback color function for legacy categories or when no color is stored
@Composable
fun getFallbackCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Color(0xFFE74C3C)
        "fuel" -> Color(0xFF280b26)
        "entertainment" -> Color(0xFF8f1767)
        "other" -> Color(0xFF465be7)
        else -> Color(0xFF465be7)
    }
}

// Fallback icon function for legacy categories or when no icon is stored
@Composable
fun getFallbackCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "fuel" -> Icons.Default.LocalGasStation
        "entertainment" -> Icons.Default.Movie
        "other" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Convert icon string to ImageVector (moved from AddCategoryScreen)
fun getIconFromString(iconString: String): ImageVector {
    return when (iconString) {
        "shopping_cart" -> Icons.Default.ShoppingCart
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "local_gas_station" -> Icons.Default.LocalGasStation
        "school" -> Icons.Default.School
        "local_hospital" -> Icons.Default.LocalHospital
        "movie_filter" -> Icons.Default.MovieFilter
        "sports_esports" -> Icons.Default.SportsEsports
        "fitness_center" -> Icons.Default.FitnessCenter
        "flight" -> Icons.Default.Flight
        "pets" -> Icons.Default.Pets
        "work" -> Icons.Default.Work
        "account_balance" -> Icons.Default.AccountBalance
        "credit_card" -> Icons.Default.CreditCard
        "savings" -> Icons.Default.Savings
        "monetization_on" -> Icons.Default.MonetizationOn
        "business_center" -> Icons.Default.BusinessCenter
        // Legacy icon mappings
        "movie" -> Icons.Default.Movie
        else -> Icons.Default.Category
    }
}

// Convert ImageVector to string (moved from AddCategoryScreen)
fun getStringFromIcon(icon: ImageVector): String {
    return when (icon) {
        Icons.Default.ShoppingCart -> "shopping_cart"
        Icons.Default.Restaurant -> "restaurant"
        Icons.Default.DirectionsCar -> "directions_car"
        Icons.Default.Home -> "home"
        Icons.Default.LocalGasStation -> "local_gas_station"
        Icons.Default.School -> "school"
        Icons.Default.LocalHospital -> "local_hospital"
        Icons.Default.MovieFilter -> "movie_filter"
        Icons.Default.SportsEsports -> "sports_esports"
        Icons.Default.FitnessCenter -> "fitness_center"
        Icons.Default.Flight -> "flight"
        Icons.Default.Pets -> "pets"
        Icons.Default.Work -> "work"
        Icons.Default.AccountBalance -> "account_balance"
        Icons.Default.CreditCard -> "credit_card"
        Icons.Default.Savings -> "savings"
        Icons.Default.MonetizationOn -> "monetization_on"
        Icons.Default.BusinessCenter -> "business_center"
        // Legacy icon mappings
        Icons.Default.Movie -> "movie"
        else -> "category"
    }
}

// Predefined colors for category selection
val predefinedColors = listOf(
    Color(0xFFE74C3C), // Red
    Color(0xFF3498DB), // Blue
    Color(0xFF2ECC71), // Green
    Color(0xFFF39C12), // Orange
    Color(0xFF9B59B6), // Purple
    Color(0xFF1ABC9C), // Teal
    Color(0xFFE67E22), // Dark Orange
    Color(0xFF34495E), // Dark Blue Gray
    Color(0xFFF1C40F), // Yellow
    Color(0xFFE91E63), // Pink
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Gray
)

// Predefined icons for category selection
val predefinedIcons = listOf(
    Icons.Default.ShoppingCart,
    Icons.Default.Restaurant,
    Icons.Default.DirectionsCar,
    Icons.Default.Home,
    Icons.Default.LocalGasStation,
    Icons.Default.School,
    Icons.Default.LocalHospital,
    Icons.Default.MovieFilter,
    Icons.Default.SportsEsports,
    Icons.Default.FitnessCenter,
    Icons.Default.Flight,
    Icons.Default.Pets,
    Icons.Default.Work,
    Icons.Default.AccountBalance,
    Icons.Default.CreditCard,
    Icons.Default.Savings,
    Icons.Default.MonetizationOn,
    Icons.Default.BusinessCenter,
)