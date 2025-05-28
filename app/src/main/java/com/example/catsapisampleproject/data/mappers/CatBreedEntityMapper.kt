package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.remote.dto.responses.BreedDTO
import com.example.catsapisampleproject.data.local.entities.CatBreedEntity

object CatBreedEntityMapper {

    fun fromDto(dto: BreedDTO): CatBreedEntity {
        val parts = dto.lifeSpan.split(" - ")
        val minLifeSpan = if (parts.size == 2) {
            parts[0].trim().toIntOrNull()
        } else {
            null
        }
        val maxLifeSpan = if (parts.size == 2) {
            parts[1].trim().toIntOrNull()
        } else {
            null
        }

        return CatBreedEntity(
            id = dto.id,
            name = dto.name,
            referenceImageId = dto.referenceImageId,
            minLifeSpan = minLifeSpan,
            maxLifeSpan = maxLifeSpan
        )
    }


}