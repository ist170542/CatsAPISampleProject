package com.example.catsapisampleproject.data.remote.dto.requests

import com.google.gson.annotations.SerializedName

/**
 * Misspelling to keep the consistency with the API
 */
data class FavouriteRequestDTO(

    @SerializedName("image_id")
    val imageID: String

)
