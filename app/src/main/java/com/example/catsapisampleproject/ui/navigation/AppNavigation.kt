package com.example.catsapisampleproject.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

// Define the screens for the bottom navigation
enum class BottomNavScreen {
    LIST,
    FAVORITES
}

// Define the screens for the entire app
enum class AppScreen {
    MAIN,
    DETAILS
}

// Data class for bottom navigation items
data class BottomNavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// Sealed class for navigation items (for type safety)
sealed class NavigationItem(val route: String) {
    object Main : NavigationItem("main")
    object Details : NavigationItem("details/{breedId}") {
        fun createRoute(breedId: String) = "details/$breedId"
    }
}