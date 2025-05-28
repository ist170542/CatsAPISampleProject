package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.remote.dto.responses.ImageDTO
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity

object CatBreedImageEntityMapper {
    fun fromDto(breedId: String, imageDto: ImageDTO): CatBreedImageEntity {
        return CatBreedImageEntity(
            breed_id = breedId,
            image_id = imageDto.id,
            url = imageDto.url
        )
    }
}