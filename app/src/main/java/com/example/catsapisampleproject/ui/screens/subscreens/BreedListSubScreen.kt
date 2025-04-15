package com.example.catsapisampleproject.ui.screens.subscreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.ui.components.viewmodels.BreedListUIState
import com.example.catsapisampleproject.ui.components.viewmodels.BreedListViewModel
import com.example.catsapisampleproject.ui.misc.FullScreenLoadingOverlay
import com.example.catsapisampleproject.ui.screens.subscreens.common.CatBreedGridItem
import com.example.catsapisampleproject.ui.screens.subscreens.common.CatBreedList

@Composable
fun BreedListSubScreen(
    viewModel: BreedListViewModel,
    onClickedCard: (String) -> Unit
) {
    //Collecting state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    //workaround to avoid breaking the preview (it happens when a viewmodel is passed as parameter)
    BreedListSubScreenContent(
        uiState,
        onSearchTextChange = { viewModel.updateSearchText(it) },
        onClickedFavouriteButton = viewModel::clickedFavouriteButton,
        onClickedCard = onClickedCard
    )

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedListSubScreenContent(
    uiState: BreedListUIState,
    onSearchTextChange: (String) -> Unit = {},
    onClickedFavouriteButton: (String?, Boolean) -> Unit,
    onClickedCard: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.searchText,
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        onQueryChange = onSearchTextChange
                    )
                },
                expanded = false,
                onExpandedChange = { expanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            CatBreedList(
                breedList = uiState.filteredBreedList,
                onClickedFavouriteButton = onClickedFavouriteButton,
                onClickedCard = onClickedCard
            )

        }

        if (uiState.isLoading) {
            FullScreenLoadingOverlay(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}


@Preview
@Composable
fun BreedListSubScreenContentPreview() {
    BreedListSubScreenContent(
        uiState = BreedListUIState(
            searchText = "Testing search",
            isLoading = false,
            fullBreedList = List(5) {
                BreedWithImage(
                    breed = CatBreed(
                        id = "id",
                        name = "name",
                        referenceImageId = "refImgId",
                        minLifeSpan = 1,
                        maxLifeSpan = 2
                    ),
                    image = CatBreedImage(
                        imageId = "",
                        url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                        breedId = "id",
                    ),
                    isFavourite = true
                )
            },
            filteredBreedList = List(5) {
                BreedWithImage(
                    breed = CatBreed(
                        id = "id",
                        name = "name",
                        referenceImageId = "refImgId",
                        minLifeSpan = 1,
                        maxLifeSpan = 2
                    ),
                    image = CatBreedImage(
                        imageId = "",
                        url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                        breedId = "id",
                    ),
                    isFavourite = true
                )
            }
        ),
        onSearchTextChange = {},
        onClickedFavouriteButton = { _, _ -> },
        onClickedCard = {}
    )
}

@Preview
@Composable
fun BreedListItemPreview() {
    CatBreedGridItem(
        breedWithImage = BreedWithImage(
            breed = CatBreed(
                id = "id",
                name = "name",
                referenceImageId = "refImgId",
                minLifeSpan = 1,
                maxLifeSpan = 2
            ),
            image = CatBreedImage(
                imageId = "",
                url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                breedId = "id",
            ),
            isFavourite = true
        ),
        onClickedFavouriteButton = { _, _ -> },
        onClickedCard = {}
    )
}