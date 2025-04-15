package com.example.catsapisampleproject.domain.model

data class CatBreed(
    val id: String,
    val name: String,
    val referenceImageId: String?,
    val minLifeSpan: Int?,
    val maxLifeSpan: Int?
)