package com.example.catsapisampleproject.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat.getString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.presentation.viewmodels.BreedListViewModel
import com.example.catsapisampleproject.presentation.navigation.BottomNavScreen
import com.example.catsapisampleproject.presentation.navigation.BottomNavigationBar
import com.example.catsapisampleproject.presentation.navigation.NavigationItem
import com.example.catsapisampleproject.presentation.screens.subscreens.BreedListSubScreen
import com.example.catsapisampleproject.presentation.screens.subscreens.FavouriteBreedListSubScreen

@Composable
fun MainScreen(
    navController: NavHostController
) {

    // Create a new NavHostController for the inner NavHost
    val innerNavController = rememberNavController()

    var showExitDialog by remember { mutableStateOf(false) }

    val activity = LocalActivity.current

    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitAppAlertDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                activity?.finish()
            }
        )
    }

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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExitAppAlertDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties()
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = stringResource(R.string.exit_app_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.exit_app_dialog_message))
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.exit_app_dialog_cancel))
                    }
                    TextButton(onClick =
                        onConfirmExit
                    ) {
                        Text(stringResource(R.string.exit_app_dialog_confirm))
                    }
                }
            }
        }
    }
}