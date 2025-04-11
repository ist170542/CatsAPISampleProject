package com.example.catsapisampleproject.dataLayer.mappers

import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity

class FavouriteEntityMapper {
    fun fromDto(dto: FavouriteDTO): FavouriteEntity {
        return FavouriteEntity(
            favouriteId = dto.favouriteID,
            imageId = dto.imageID
        )

    }
}