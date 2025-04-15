package com.example.catsapisampleproject.domain.model

data class BreedWithImageAndDetails(
    val breed: CatBreed,
    val image: CatBreedImage?,
    val details: CatBreedDetails?,
    val isFavourite: Boolean = false
)