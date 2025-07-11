package com.example.catsapisampleproject.data.remote.dto.responses

import com.google.gson.annotations.SerializedName

data class BreedDTO(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("temperament")
    val temperament: String,

    @SerializedName("origin")
    val origin: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("reference_image_id")
    val referenceImageId: String?,

    @SerializedName("life_span")
    val lifeSpan: String
)
