package com.example.catsapisampleproject.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailsScreenContent(
    uiState: CatDetailsUIState,
    onFavoriteClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    // Display any favorite operation errors via snackbar
    LaunchedEffect(uiState.favouriteOperationError) {
        if (uiState.favouriteOperationError.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.favouriteOperationError)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                uiState.breed?.let { breed ->
                    TopAppBar(title = { Text(breed.breed.name) } )
                } },
            floatingActionButton = {
                if (uiState.error.isEmpty()) {
                    uiState.breed?.let { breed ->
                        ExtendedFloatingActionButton(
                            onClick = { onFavoriteClick() },
                            icon = {
                                Icon(
                                    imageVector = if (breed.isFavourite) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (breed.isFavourite)
                                        stringResource(R.string.remove_favorites)
                                    else
                                        stringResource(R.string.add_favorites)
                                )
                            },
                            text = {
                                Text(
                                    text = if (breed.isFavourite)
                                        stringResource(R.string.remove_favorites)
                                    else
                                        stringResource(R.string.add_favorites)
                                )
                            },
                            containerColor = if (breed.isFavourite) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            contentColor = if (breed.isFavourite) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.error.isNotEmpty()) {
                    ErrorState(uiState.error)
                } else {
                    uiState.breed?.let { breed ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 88.dp) // Space for FAB
                        ) {
                            BreedContent(breed)
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            FullScreenLoadingOverlay(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            )
        }
    }

}



@OptIn(ExperimentalGlideComposeApi::class, ExperimentalLayoutApi::class)
@Composable
fun BreedContent(
    breed: BreedWithImageAndDetails) {
    Column (
        modifier = Modifier
            .padding(16.dp)
    ){

        GlideImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(24.dp)
                .clip(RoundedCornerShape(8.dp)),
            model = breed.image?.url,
            loading = placeholder(R.drawable.ic_generic_cat_drawable),
            failure = placeholder(R.drawable.ic_generic_cat_drawable),
            contentScale = ContentScale.Crop,
            contentDescription = breed.breed.name + " Image",
        )

        // Key Details
        breed.details?.origin?.let {
            InfoRow(
                icon = Icons.Default.LocationOn,
                title = "Origin",
                value = it
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Temperament",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breed.details?.temperament?.split(", ")?.forEach { trait ->
                SuggestionChip(onClick = {}, label = { Text(trait) })
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Description",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        breed.details?.description?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 24.sp
            )
        }
    }
}


@Composable
private fun ErrorState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 8.dp) // Add vertical padding
            .fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp)) // Increase spacing
        Column {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp)) // Add spacing between title and value
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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

