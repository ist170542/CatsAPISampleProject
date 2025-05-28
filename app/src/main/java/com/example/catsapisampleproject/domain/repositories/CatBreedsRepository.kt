package com.example.catsapisampleproject.domain.repositories

import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.model.InitializationResult
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow

interface CatBreedsRepository {
    fun fetchAndCacheCatBreeds(): Flow<InitializationResult>

    fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>>
    fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>>

    fun observeCatBreeds(): Flow<List<BreedWithImage>>

    fun getCatBreedDetailsByIdWithFavourite(breedId: String): Flow<BreedWithImageAndDetails>
    fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?>
}