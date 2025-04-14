package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.local.entities.PendingOperation
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage

fun createBreedWithImageList(breeds: List<CatBreed>,
                             images: List<CatBreedImage>?,
                             favourites: List<FavouriteEntity>): List<BreedWithImage> {

    return breeds.map { breed ->
        val imageId = breed.referenceImageId
        val relevantFavourite = favourites.find { fav ->
            fav.imageId == imageId && fav.pendingOperation != PendingOperation.Delete
        }

        val isFavourite = relevantFavourite != null
        val image = images?.find { it.breed_id == breed.id }

        BreedWithImage(
            breed = breed,
            image = image,
            isFavourite = isFavourite
        )
    }
}

fun computeAverageMinLifeSpan(breeds: List<BreedWithImage>): Double? {
    val values = breeds.mapNotNull { it.breed.minLifeSpan?.toDouble() }
    return if (values.isNotEmpty()) values.average() else null
}

