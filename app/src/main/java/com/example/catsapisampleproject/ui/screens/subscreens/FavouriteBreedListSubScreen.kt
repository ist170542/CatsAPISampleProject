package com.example.catsapisampleproject.ui.screens.subscreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.ui.components.viewmodels.BreedFavouriteListViewModel
import com.example.catsapisampleproject.ui.components.viewmodels.FavouriteListUIState
import com.example.catsapisampleproject.ui.misc.FullScreenLoadingOverlay
import com.example.catsapisampleproject.ui.screens.subscreens.common.CatBreedGridItem

//todo: increase reusability from the list cat breeds viewModel. Also a little for the composable
@Composable
fun FavouriteBreedListSubScreen(
    viewModel: BreedFavouriteListViewModel,
    onClickedCard: (String) -> Unit
) {
    //Collecting state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    //workaround to avoid breaking the preview (it happens when a viewmodel is passed as parameter)
    FavouriteBreedListSubScreenContent(
        uiState,
        onClickedFavouriteButton = viewModel::clickedFavouriteButton,
        onClickedCard = onClickedCard
    )
}
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
    fun FavouriteBreedListSubScreenContent(
        uiState: FavouriteListUIState,
        onClickedFavouriteButton: (String?, Boolean) -> Unit,
        onClickedCard: (String) -> Unit
    ) {

        Box(modifier = Modifier.fillMaxSize()){

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    TopAppBar(title = { Text(stringResource(R.string.favourite_breeds)) })

                    if (uiState.favouriteList.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            modifier = Modifier.fillMaxSize().weight(1f),
                        ) {
                            items(uiState.favouriteList) { breed ->
                                CatBreedGridItem(
                                    breedWithImage = breed,
                                    onClickedFavouriteButton = onClickedFavouriteButton,
                                    onClickedCard = onClickedCard
                                )
                            }
                        }

                        Text(modifier = Modifier.padding(16.dp),
                            text = stringResource
                                (R.string.average_min_life_span,
                                uiState.averageMinLifeSpan.toString())
                        )

                        Text(
                            text = "Average min life span: ${uiState.averageMinLifeSpan}",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.no_favourites_yet),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

            }
            if (uiState.isLoading) {
                FullScreenLoadingOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
    }

@Preview
@Composable
fun FavouriteBreedListSubScreenContentPreview() {
    FavouriteBreedListSubScreenContent(
        uiState = FavouriteListUIState(
            favouriteList = listOf(
            ),
            averageMinLifeSpan = 12.0,
            isLoading = false
        ),
        onClickedFavouriteButton = { _, _ -> },
        onClickedCard = {}
    )
}
