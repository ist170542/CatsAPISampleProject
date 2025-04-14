package com.example.catsapisampleproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.ui.components.viewmodels.CatDetailsUIState
import com.example.catsapisampleproject.ui.components.viewmodels.CatViewDetailsViewModel
import com.example.catsapisampleproject.ui.misc.FullScreenLoadingOverlay

@Composable
fun CatDetailsScreen(
    breedId: String,
    viewModel: CatViewDetailsViewModel = hiltViewModel()  // SavedStateHandle provides the breedId
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    CatDetailsScreenContent(
        uiState,
        onFavoriteClick = { viewModel.toggleFavourite() },
        snackbarHostState
    )

    // Display any favorite operation errors via snackbar
    LaunchedEffect(uiState.favouriteOperationError) {
        if (uiState.favouriteOperationError.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.favouriteOperationError)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CatDetailsScreenContent(
    uiState: CatDetailsUIState,
    onFavoriteClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            uiState.breed?.let { breed ->
                TopAppBar(title = { Text(breed.breed.name) })
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.error.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error)
                }
            } else {
                uiState.breed?.let { breed ->
                    Scaffold(
                        topBar = {
                            TopAppBar(title = { Text(breed.breed.name) })
                        }
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .padding(16.dp),
                        ) {
                            GlideImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                model = breed.image?.url,
                                loading = placeholder(R.drawable.ic_generic_cat_drawable),
                                failure = placeholder(R.drawable.ic_generic_cat_drawable),
                                contentScale = ContentScale.Fit,
                                contentDescription = breed.breed.name + " Image"
                            )
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Origin: ${breed.details?.origin}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Temperament: ${breed.details?.temperament}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Description: ${breed.details?.description}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { onFavoriteClick() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.End),
                                    colors = if (breed.isFavourite) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color.Red
                                        )
                                    } else {
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color.Green,
                                        )
                                    }
                                ) {
                                    Text(text = if (breed.isFavourite) "Remove from Favorites" else "Add to Favorites")
                                }
                            }
                        }
                    }
                }
            }
            // Show full screen loading only when loading the breed details
            if (uiState.isLoading) {
                FullScreenLoadingOverlay()
            }

        }
    }
}

@Composable
@Preview
fun CatDetailsScreenContentPreview() {
    CatDetailsScreenContent(
        uiState = CatDetailsUIState(
            breed = BreedWithImageAndDetails(
                breed = CatBreed(
                    id = "id",
                    name = "name",
                    referenceImageId = "refImgId",
                    minLifeSpan = 1,
                    maxLifeSpan = 1
                ),
                image = CatBreedImage(
                    image_id = "AAAAA",
                    url = "https://cdn2.thecatapi.com/images/0SxW2SQ_S.jpg",
                    breed_id = "id",
                ),
                isFavourite = false,
                details = CatBreedDetailsEntity(
                    origin = "origin",
                    temperament = "temperament",
                    description = "description",
                    breedID = "id"
                )
            ),
            isLoading = false,
            error = ""
        ),
        onFavoriteClick = {},
        snackbarHostState = remember { SnackbarHostState() }
    )
}

