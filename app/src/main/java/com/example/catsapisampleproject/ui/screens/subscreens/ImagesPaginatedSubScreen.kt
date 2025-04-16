package com.example.catsapisampleproject.ui.screens.subscreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import com.example.catsapisampleproject.ui.components.viewmodels.CatImagesUIState
import com.example.catsapisampleproject.ui.components.viewmodels.ImagesPaginatedSubScreenViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.catsapisampleproject.ui.misc.FullScreenLoadingOverlay
import com.example.catsapisampleproject.ui.screens.subscreens.common.CatBreedGridItem

@Composable
fun CatImagesListPaginatedSubScreen(
    viewModel: ImagesPaginatedSubScreenViewModel = hiltViewModel(),
    onClickedCard: (String) -> Unit
) {

    val uiState = viewModel.uiState.collectAsState()

    CatImagesListPaginatedSubScreenContent(
        uiState = uiState.value,
        onClickedFavouriteButton = viewModel::toggleFavourite,
        onClickedCard = onClickedCard
    )
}

@Composable
fun CatImagesListPaginatedSubScreenContent(
    uiState: CatImagesUIState = CatImagesUIState(),
    onClickedFavouriteButton: (String?, Boolean) -> Unit,
    onClickedCard: (String) -> Unit
) {

    val imageItems = uiState.imagesFlow.collectAsLazyPagingItems()

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(columns = GridCells.Adaptive(128.dp)) {
            items(imageItems.itemCount) { index ->
                val item = imageItems[index]
                if (item != null) {
                    CatBreedGridItem(
                        breedWithImage = item,
                        onClickedFavouriteButton = onClickedFavouriteButton,
                        onClickedCard = onClickedCard
                    )
                }
            }

            item {
                when (imageItems.loadState.append) {
                    is LoadState.Loading -> CircularProgressIndicator()
                    is LoadState.Error -> Text("Failed to load more.")
                    else -> Unit
                }
            }
        }

        if (uiState.isLoading) {
            FullScreenLoadingOverlay()
        }
    }


}