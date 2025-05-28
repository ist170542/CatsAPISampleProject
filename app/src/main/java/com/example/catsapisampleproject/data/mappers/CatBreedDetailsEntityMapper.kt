package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.remote.dto.responses.BreedDTO
import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity

object CatBreedDetailsEntityMapper {
    fun fromDTO(dto: BreedDTO): CatBreedDetailsEntity {
        return CatBreedDetailsEntity(
            breedID = dto.id,
            description = dto.description,
            temperament = dto.temperament,
            origin = dto.origin
        )
    }

}