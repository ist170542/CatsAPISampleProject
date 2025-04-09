package com.example.catsapisampleproject.domain.model

data class CatBreed (
    val id: String,
    val name: String,
    val description: String,
    val temperament: String,
    val origin: String,
    val referenceImageId: String?,
    val isFavorite: Boolean
)