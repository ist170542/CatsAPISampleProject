package com.example.catsapisampleproject.data.local.source

import com.example.catsapisampleproject.data.local.database.CatBreedDetailsDao
import com.example.catsapisampleproject.data.local.database.CatBreedImagesDao
import com.example.catsapisampleproject.data.local.database.CatBreedsDao
import com.example.catsapisampleproject.data.local.database.FavouriteBreedsDao
import com.example.catsapisampleproject.data.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedEntity
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val catBreedsDao: CatBreedsDao,
    private val catBreedImagesDao: CatBreedImagesDao,
    private val favouritesDao: FavouriteBreedsDao,
    private val catBreedDetailsDao: CatBreedDetailsDao
) : LocalDataSource {

    override suspend fun getCatBreeds(): List<CatBreedEntity> {
        return catBreedsDao.getAllCatBreeds()
    }

    override suspend fun getCatBreedById(breedId: String): CatBreedEntity? {
        return catBreedsDao.getCatBreedById(breedId)
    }

    override suspend fun insertCatBreeds(catBreeds: List<CatBreedEntity>) {
        catBreedsDao.insertCatBreeds(catBreeds)
    }

    override suspend fun getCatBreedImages(): List<CatBreedImageEntity>? {
        return catBreedImagesDao.getAllCatBreedImages()
    }

    override suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImageEntity? {
        return catBreedImagesDao.getCatBreedImageByBreedId(breedId)
    }

    override suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImageEntity>) {
        catBreedImagesDao.insertCatBreedImages(catBreedImages)
    }

    override suspend fun getFavouriteCatBreeds(): List<FavouriteEntity> {
        return favouritesDao.getAllFavourites()
    }

    override suspend fun insertFavourite(favouriteEntity: FavouriteEntity) {
        favouritesDao.insertFavourite(favouriteEntity)
    }

    override suspend fun insertFavourites(favouriteEntities: List<FavouriteEntity>) {
        favouritesDao.insertFavourites(favouriteEntities)
    }

    override suspend fun deleteFavourite(imageId: String) {
        favouritesDao.deleteFavourite(FavouriteEntity(imageId, ""))
    }

    override suspend fun getFavouriteByImageId(imageId: String): FavouriteEntity? {
        return favouritesDao.getFavouriteByImageId(imageId)
    }

    override suspend fun deleteAllFavourites() {
        favouritesDao.deleteAllFavourites()
    }

    override fun observeFavouriteCatBreeds(): Flow<List<FavouriteEntity>> {
        return favouritesDao.observeAllFavourites()
    }

    override fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?> {
        return favouritesDao.observeFavouriteByImageId(imageId)
    }

    override suspend fun getCatBreedDetails(breedId: String): CatBreedDetailsEntity? {
        return catBreedDetailsDao.getCatBreedDetails(breedId)
    }

    override suspend fun insertCatBreedDetails(catBreed: CatBreedDetailsEntity) {
        catBreedDetailsDao.insertCatBreedDetails(catBreed)
    }

    override suspend fun insertCatBreedsDetails(catBreeds: List<CatBreedDetailsEntity>) {
        catBreedDetailsDao.insertCatBreedsDetails(catBreeds)
    }

}