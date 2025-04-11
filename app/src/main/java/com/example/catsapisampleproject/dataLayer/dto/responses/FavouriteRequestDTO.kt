package com.example.catsapisampleproject.dataLayer.dto.responses

import com.google.gson.annotations.SerializedName

/**
 * Misspelling to keep the consistency with the API
 */
data class FavouriteRequestDTO(

    @SerializedName("image_id")
    val imageID: String

)
