package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.domain.model.CatBreedDetails

object CatBreedDetailsMapper {
    fun fromEntity(entity: CatBreedDetailsEntity): CatBreedDetails {
        return CatBreedDetails(
            breedId = entity.breedID,
            description = entity.description,
            temperament = entity.temperament,
            origin = entity.origin
        )
    }
}