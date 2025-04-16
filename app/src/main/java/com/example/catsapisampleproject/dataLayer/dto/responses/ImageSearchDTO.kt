package com.example.catsapisampleproject.dataLayer.dto.responses

import com.google.gson.annotations.SerializedName

data class ImageSearchDTO (
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("breeds")
    val breeds: List<BreedDTO>
)