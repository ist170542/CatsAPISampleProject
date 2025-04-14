package com.example.catsapisampleproject.dataLayer.local

import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImpl(
    private val catBreedsDao: CatBreedsDao,
    private val catBreedImagesDao: CatBreedImagesDao,
    private val favouritesDao: FavouriteBreedsDao,
    private val catBreedDetailsDao: CatBreedDetailsDao
) : LocalDataSource {

    override suspend fun getCatBreeds(): List<CatBreed> {
        return catBreedsDao.getAllCatBreeds()
    }

    override suspend fun getCatBreedById(breedId: String): CatBreed? {
        return catBreedsDao.getCatBreedById(breedId)
    }

    override suspend fun updateCatBreed(catBreed: CatBreed) {
        catBreedsDao.updateCatBreed(catBreed)
    }

    override suspend fun insertCatBreeds(catBreeds: List<CatBreed>) {
        catBreedsDao.insertCatBreeds(catBreeds)
    }

    override suspend fun insertCatBreed(catBreed: CatBreed) {
        catBreedsDao.insertCatBreed(catBreed)
    }

    override fun observeCatBreeds(): Flow<List<CatBreed>> {
        return catBreedsDao.observeAllCatBreeds()
    }

    override fun observeCatBreedById(breedId: String): Flow<CatBreed?> {
        return catBreedsDao.observeCatBreedById(breedId)
    }

    override suspend fun getCatBreedImages(): List<CatBreedImage>? {
        return catBreedImagesDao.getAllCatBreedImages()
    }

    override suspend fun getCatBreedImageByBreedId(breedId: String): CatBreedImage? {
        return catBreedImagesDao.getCatBreedImageByBreedId(breedId)
    }

    override suspend fun getCatBreedImageById(imageId: String): CatBreedImage? {
        return catBreedImagesDao.getCatBreedImageById(imageId)
    }

    override suspend fun updateCatBreedImage(catBreedImage: CatBreedImage) {
        catBreedImagesDao.updateCatBreedImage(catBreedImage)
    }

    override suspend fun insertCatBreedImages(catBreedImages: List<CatBreedImage>) {
        catBreedImagesDao.insertCatBreedImages(catBreedImages)
    }

    override suspend fun insertCatBreedImage(catBreedImage: CatBreedImage) {
        catBreedImagesDao.insertCatBreedImage(catBreedImage)
    }

    override fun observeCatBreedImages(): Flow<List<CatBreedImage>> {
        return catBreedImagesDao.observeAllCatBreedImages()
    }

    override fun observeCatBreedImageByBreedId(breedId: String): Flow<CatBreedImage?> {
        return catBreedImagesDao.observeCatBreedImageByBreedId(breedId)
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