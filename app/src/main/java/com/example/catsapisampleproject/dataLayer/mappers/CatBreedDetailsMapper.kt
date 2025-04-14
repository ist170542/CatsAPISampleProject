package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity

class CatBreedDetailsMapper {
    fun fromDTO(dto: BreedDTO): CatBreedDetailsEntity {
        return CatBreedDetailsEntity(
            breedID = dto.id,
            description = dto.description,
            temperament = dto.temperament,
            origin = dto.origin
        )
    }

}