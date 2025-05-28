package com.example.catsapisampleproject.data.remote.source

import com.example.catsapisampleproject.data.remote.dto.responses.BreedDTO
import com.example.catsapisampleproject.data.remote.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.data.remote.dto.responses.ImageDTO

interface RemoteDataSource{
    suspend fun getCatBreeds() : List<BreedDTO>
    suspend fun getCatBreedImageByReferenceImageId(referenceImageId: String) : ImageDTO
    suspend fun getFavourites() : List<FavouriteDTO>
    suspend fun postCatBreedAsFavourite(imageReferenceId: String): FavouriteDTO
    suspend fun deleteCatBreedAsFavourite(favouriteId: String): Boolean
}