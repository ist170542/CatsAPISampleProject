package com.example.catsapisampleproject.dataLayer.local

import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage

class LocalDataSourceImpl(
    private val catBreedsDao: CatBreedsDao,
    private val catBreedImagesDao: CatBreedImagesDao
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

    override suspend fun getCatBreedImagesByBreedId(breedId: String): List<CatBreedImage>? {
        return catBreedImagesDao.getCatBreedImagesByBreedId(breedId)
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

}