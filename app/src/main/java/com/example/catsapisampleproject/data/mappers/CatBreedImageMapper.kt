package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.domain.model.CatBreedImage

object CatBreedImageMapper {
    fun fromEntity(entity: CatBreedImageEntity) = CatBreedImage(
        imageId = entity.image_id,
        breedId = entity.breed_id,
        url = entity.url
    )

}