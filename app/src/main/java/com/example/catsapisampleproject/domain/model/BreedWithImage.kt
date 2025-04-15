package com.example.catsapisampleproject.domain.model

data class BreedWithImage(
    val breed: CatBreed,
    val image: CatBreedImage?,
    val isFavourite: Boolean = false
)