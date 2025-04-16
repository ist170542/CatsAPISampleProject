package com.example.catsapisampleproject.domain.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.ImageSearchDTO
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.CatBreed

object BreedWithImageMapper {

    fun fromDto(dto: ImageSearchDTO) : BreedWithImage {
        return BreedWithImage(
            breed = CatBreed(
                id = dto.breeds.firstOrNull()?.id.orEmpty(),
                name = dto.breeds.firstOrNull()?.name.orEmpty(),
                referenceImageId = dto.breeds.firstOrNull()?.referenceImageId.orEmpty(),
                minLifeSpan = dto.breeds.firstOrNull()?.lifeSpan?.split("-")?.firstOrNull()?.trim()?.toIntOrNull() ?: 0,
                maxLifeSpan = dto.breeds.firstOrNull()?.lifeSpan?.split("-")?.lastOrNull()?.trim()?.toIntOrNull() ?: 0
            ),
            image = CatBreedImageMapper.fromDto(dto)
        )
    }
}