package com.example.catsapisampleproject.domain.mappers

import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedEntity
import com.example.catsapisampleproject.domain.model.CatBreed

object CatBreedMapper {
    fun fromEntity(entity: CatBreedEntity) = CatBreed(
        id = entity.id,
        name = entity.name,
        referenceImageId = entity.referenceImageId,
        minLifeSpan = entity.minLifeSpan,
        maxLifeSpan = entity.maxLifeSpan
    )

}