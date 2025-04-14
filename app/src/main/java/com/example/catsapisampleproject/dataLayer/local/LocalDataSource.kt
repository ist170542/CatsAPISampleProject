package com.example.catsapisampleproject.dataLayer.local

import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    //cat breeds
    suspend fun getCatBreeds(): List<CatBreed>
    suspend fun getCatBreedById(breedId: String): CatBreed?
    suspend fun updateCatBreed(catBreed: CatBreed)
    suspend fun insertCatBreeds(catBreeds: List<CatBreed>)
    suspend fun insertCatBreed(catBreed: CatBreed)
    fun observeCatBreeds(): Flow<List<CatBreed>>
    fun observeCatBreedById(breedId: String): Flow<CatBreed?>

    //cat breed images
    suspend fun getCatBreedImages() : List<CatBreedImage>?
    suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImage?
    suspend fun getCatBreedImageById(imageId: String): CatBreedImage?
    suspend fun updateCatBreedImage(catBreedImage: CatBreedImage)
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImage>)
    suspend fun insertCatBreedImage(catBreedImage: CatBreedImage)
    fun observeCatBreedImages(): Flow<List<CatBreedImage>>
    fun observeCatBreedImageByBreedId(breedId: String): Flow<CatBreedImage?>

    //favourites
    suspend fun getFavouriteCatBreeds(): List<FavouriteEntity>
    suspend fun insertFavourite(favouriteEntity: FavouriteEntity)
    suspend fun insertFavourites(favouriteEntities: List<FavouriteEntity>)
    suspend fun deleteFavourite(imageId: String)
    suspend fun getFavouriteByImageId(imageId: String): FavouriteEntity?
    suspend fun deleteAllFavourites()
    fun observeFavouriteCatBreeds(): Flow<List<FavouriteEntity>>
    fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?>

    //details
    suspend fun getCatBreedDetails(breedId: String): CatBreedDetailsEntity?
    suspend fun insertCatBreedDetails(catBreed: CatBreedDetailsEntity)
    suspend fun insertCatBreedsDetails(catBreeds: List<CatBreedDetailsEntity>)

}