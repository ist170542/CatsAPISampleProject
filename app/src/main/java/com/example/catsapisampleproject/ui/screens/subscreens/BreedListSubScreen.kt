package com.example.catsapisampleproject.ui.screens.subscreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
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
                            onExpandedChange = { expanded  = it },
                            placeholder = { Text("Type breed name to search...") },
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
                        image_id = "",
                        url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                        breed_id = "id",
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
                        image_id = "",
                        url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                        breed_id = "id",
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
                image_id = "",
                url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                breed_id = "id",
            ),
            isFavourite = true
        ),
        onClickedFavouriteButton = { _, _ -> },
        onClickedCard = {}
    )
}