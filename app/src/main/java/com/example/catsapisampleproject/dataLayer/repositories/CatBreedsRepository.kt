package com.example.catsapisampleproject.dataLayer.repositories

import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow

interface CatBreedsRepository {
    fun getCatBreed(breedId: String): Flow<Resource<BreedWithImage>>
    fun getCatBreeds(): Flow<Resource<List<BreedWithImage>>>
    fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<FavouriteEntity>>
    fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>>
}