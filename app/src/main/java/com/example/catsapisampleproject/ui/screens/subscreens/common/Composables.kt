package com.example.catsapisampleproject.ui.screens.subscreens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.catsapisampleproject.R
import com.example.catsapisampleproject.domain.model.BreedWithImage

@Composable
fun CatBreedList(
    breedList: List<BreedWithImage>,
    onClickedFavouriteButton: (String?, Boolean) -> Unit,
    onClickedCard: (String) -> Unit
) {
    if (breedList.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(breedList) { breed ->
                CatBreedGridItem(
                    breedWithImage = breed,
                    onClickedFavouriteButton = onClickedFavouriteButton,
                    onClickedCard = onClickedCard
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CatBreedGridItem(
    breedWithImage: BreedWithImage,
    modifier: Modifier = Modifier,
    onClickedFavouriteButton: (String?, Boolean) -> Unit,
    onClickedCard: (String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .clickable {
                onClickedCard(breedWithImage.breed.id)
            }, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // Main column for image + text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                GlideImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    model = breedWithImage.image?.url,
                    loading = placeholder(R.drawable.ic_generic_cat_drawable),
                    failure = placeholder(R.drawable.ic_generic_cat_drawable),
                    contentScale = ContentScale.Crop,
                    contentDescription = breedWithImage.breed.name
                )

                if(breedWithImage.breed.name.isNotEmpty()) {
                    // Breed name below the image
                    Text(
                        text = breedWithImage.breed.name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // Favorite icon in the top-right
            IconButton(
                onClick = {
                    onClickedFavouriteButton(
                        breedWithImage.image?.imageId,
                        breedWithImage.isFavourite
                    )
                },
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (breedWithImage.isFavourite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (breedWithImage.isFavourite) {
                        stringResource(R.string.unmark_favorite)
                    } else {
                        stringResource(R.string.mark_favorite)
                    },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}