package com.example.catsapisampleproject.data.local.source

import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    //cat breeds
    suspend fun getCatBreeds(): List<CatBreedEntity>
    suspend fun getCatBreedById(breedId: String): CatBreedEntity?
    suspend fun insertCatBreeds(catBreeds: List<CatBreedEntity>)

    //cat breed images
    suspend fun getCatBreedImages(): List<CatBreedImageEntity>?
    suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImageEntity?
    suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImageEntity>)

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