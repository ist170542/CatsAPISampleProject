package com.example.catsapisampleproject.dataLayer.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val imageId: String,
    val favouriteId: String
)