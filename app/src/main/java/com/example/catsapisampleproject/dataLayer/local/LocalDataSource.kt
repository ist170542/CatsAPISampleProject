package com.example.catsapisampleproject.dataLayer.local

import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage

interface LocalDataSource {
    //cat breeds
    suspend fun getCatBreeds(): List<CatBreed>
    suspend fun getCatBreedById(breedId: String): CatBreed?
    suspend fun updateCatBreed(catBreed: CatBreed)
    suspend fun insertCatBreeds(catBreeds: List<CatBreed>)
    suspend fun insertCatBreed(catBreed: CatBreed)
    //cat breed images
    suspend fun getCatBreedImagesByBreedId(breedId: String): List<CatBreedImage>?
    suspend fun getCatBreedImageById(imageId: String): CatBreedImage?
    suspend fun updateCatBreedImage(catBreedImage: CatBreedImage)
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImage>)
    suspend fun insertCatBreedImage(catBreedImage: CatBreedImage)

}