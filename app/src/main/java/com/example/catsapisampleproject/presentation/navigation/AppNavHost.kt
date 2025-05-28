package com.example.catsapisampleproject.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.catsapisampleproject.presentation.screens.CatDetailsScreen
import com.example.catsapisampleproject.presentation.screens.MainScreen
import com.example.catsapisampleproject.presentation.screens.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationItem.Splash.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash screen
        composable(
            route = NavigationItem.Splash.route
        ) {
            SplashScreen { navController.navigate(NavigationItem.Main.route) {
                popUpTo(NavigationItem.Splash.route) { inclusive = true }
            }
            }
        }

        // Main navigation graph (with bottom navigation)
        navigation(
            startDestination = BottomNavScreen.LIST.name,
            route = NavigationItem.Main.route

        ) {
            // List screen
            composable(BottomNavScreen.LIST.name) {
                MainScreen(navController = navController)
            }
            // Favorites screen
            composable(BottomNavScreen.FAVORITES.name) {
                MainScreen(navController = navController)
            }
        }

        // Details screen (outside the bottom navigation)
        composable(
            route = NavigationItem.Details.route,
            arguments = listOf(navArgument("breedId") { type = NavType.StringType })
        ) { backStackEntry ->
            val breedId = backStackEntry.arguments?.getString("breedId") ?: ""
            CatDetailsScreen(breedId = breedId)
        }
    }
}
