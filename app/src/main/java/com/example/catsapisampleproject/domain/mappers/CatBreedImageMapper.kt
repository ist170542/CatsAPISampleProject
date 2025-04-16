package com.example.catsapisampleproject.domain.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.ImageSearchDTO
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.util.StringUtils

object CatBreedImageMapper {
    fun fromEntity(entity: CatBreedImageEntity) = CatBreedImage(
        imageId = entity.image_id,
        breedId = entity.breed_id,
        url = entity.url
    )

    fun fromDto(dto: ImageSearchDTO) = CatBreedImage(
        imageId = dto.id,
        breedId = dto.breeds.firstOrNull()?.id ?: StringUtils.EMPTY_STRING,
        url = dto.url
    )

}