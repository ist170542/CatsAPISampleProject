package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.remote.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity

object FavouriteEntityMapper {
    fun fromDto(dto: FavouriteDTO): FavouriteEntity {
        return FavouriteEntity(
            favouriteId = dto.favouriteID,
            imageId = dto.imageID
        )

    }
}