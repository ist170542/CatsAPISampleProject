package com.example.catsapisampleproject.dataLayer.repositories

import androidx.paging.PagingData
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageSearchDTO
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow

interface CatBreedsRepository {
    fun fetchAndCacheCatBreeds(): Flow<CatBreedsRepositoryImpl.InitializationResult>

    fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<FavouriteEntity>>
    fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>>

    fun observeCatBreeds(): Flow<Triple<List<CatBreedEntity>, List<CatBreedImageEntity>?, Flow<List<FavouriteEntity>>>>

    fun getCatBreedDetailsById(breedId: String): Flow<Triple<CatBreedEntity, CatBreedImageEntity?, CatBreedDetailsEntity?>>
    fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?>
    fun observeCatImagesPaginated(): Flow<PagingData<ImageSearchDTO>>
    fun observeFavouriteCatBreeds(): Flow<List<FavouriteEntity>>
}