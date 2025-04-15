package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity

object CatBreedImageEntityMapper {
    fun fromDto(breedId: String, imageDto: ImageDTO): CatBreedImageEntity {
        return CatBreedImageEntity(
            breed_id = breedId,
            image_id = imageDto.id,
            url = imageDto.url
        )
    }
}