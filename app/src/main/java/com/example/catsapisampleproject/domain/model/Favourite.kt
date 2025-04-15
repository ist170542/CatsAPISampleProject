package com.example.catsapisampleproject.domain.model

data class Favourite(
    val imageId: String,
    val favouriteId: String? = null,
    val status: FavouriteStatus = FavouriteStatus.Synced
)

enum class FavouriteStatus {
    Synced,
    PendingAdd,
    PendingDelete
}
