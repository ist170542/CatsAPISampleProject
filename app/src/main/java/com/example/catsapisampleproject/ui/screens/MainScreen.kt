package com.example.catsapisampleproject.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catsapisampleproject.ui.components.viewmodels.BreedListViewModel
import com.example.catsapisampleproject.ui.navigation.BottomNavScreen
import com.example.catsapisampleproject.ui.navigation.BottomNavigationBar
import com.example.catsapisampleproject.ui.navigation.NavigationItem
import com.example.catsapisampleproject.ui.screens.subscreens.BreedListSubScreen
import com.example.catsapisampleproject.ui.screens.subscreens.CatImagesListPaginatedSubScreen
import com.example.catsapisampleproject.ui.screens.subscreens.FavouriteBreedListSubScreen

@Composable
fun MainScreen(
    navController: NavHostController
) {

    // Create a new NavHostController for the inner NavHost
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = innerNavController) }
    ) { innerPadding ->
        MainScreenContent(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            innerNavController = innerNavController
        )
    }
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    navController: NavHostController, // Receive the outer NavHostController
    innerNavController: NavHostController
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = innerNavController, // Use the inner NavHostController
        startDestination = BottomNavScreen.LIST.name
    ) {
        composable(BottomNavScreen.LIST.name) {
            // Get the parent nav back stack entry for ViewModel scoping
            //logic to mitigate the issues in between the
            // "ViewModelStore should be set before setGraph call",
            val parentEntry = remember(innerNavController.currentBackStackEntry) {
                navController.getBackStackEntry(NavigationItem.Main.route)
            }
            val viewModel: BreedListViewModel = hiltViewModel(parentEntry)

            BreedListSubScreen(
                viewModel = viewModel,
                onClickedCard = { breedId ->
                    navController.navigate(NavigationItem.Details.createRoute(breedId))
                }
            )
        }
        composable(BottomNavScreen.FAVORITES.name) {
            FavouriteBreedListSubScreen(
                viewModel = hiltViewModel(),
                onClickedCard = { breedId ->
                    navController.navigate(NavigationItem.Details.createRoute(breedId))
                }
            )
        }

        composable(BottomNavScreen.IMAGES.name) {
            CatImagesListPaginatedSubScreen(
                onClickedCard = { breedId ->
                    if (breedId.isNotBlank()) {
                        navController.navigate(NavigationItem.Details.createRoute(breedId))
                    }
                }
            )
        }
    }
}