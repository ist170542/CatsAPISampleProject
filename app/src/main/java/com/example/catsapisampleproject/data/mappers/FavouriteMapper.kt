package com.example.catsapisampleproject.data.mappers

import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.data.local.entities.PendingOperation
import com.example.catsapisampleproject.domain.model.Favourite
import com.example.catsapisampleproject.domain.model.FavouriteStatus

object FavouriteMapper {

    fun fromEntity(entity: FavouriteEntity): Favourite {
        val status = when (entity.pendingOperation) {
            PendingOperation.Add -> FavouriteStatus.PendingAdd
            PendingOperation.Delete -> FavouriteStatus.PendingDelete
            else -> FavouriteStatus.Synced
        }

        return Favourite(
            imageId = entity.imageId,
            favouriteId = entity.favouriteId,
            status = status
        )
    }
}