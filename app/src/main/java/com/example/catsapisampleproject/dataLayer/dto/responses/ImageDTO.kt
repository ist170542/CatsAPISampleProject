package com.example.catsapisampleproject.dataLayer.dto.responses

import com.google.gson.annotations.SerializedName

data class ImageDTO (
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String
)