package com.example.catsapisampleproject.domain.model

import com.example.catsapisampleproject.data.local.entities.CatBreedEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.data.local.entities.isEffectiveFavourite
import com.example.catsapisampleproject.data.mappers.CatBreedImageMapper
import com.example.catsapisampleproject.data.mappers.CatBreedMapper

object BreedWithImageListMapper {

    fun createBreedWithImageList(
        breeds: List<CatBreedEntity>,
        images: List<CatBreedImageEntity>?,
        favourites: List<FavouriteEntity>
    ): List<BreedWithImage> {

        return breeds.map { breed ->
            val imageId = breed.referenceImageId
            val relevantFavourite = favourites.find { fav ->
                fav.imageId == imageId && fav.isEffectiveFavourite()
            }

            val isFavourite = relevantFavourite != null
            val image = images?.find { it.breed_id == breed.id }

            BreedWithImage(
                breed = CatBreedMapper.fromEntity(breed),
                image = image?.let { CatBreedImageMapper.fromEntity(it) },
                isFavourite = isFavourite
            )
        }
    }

}