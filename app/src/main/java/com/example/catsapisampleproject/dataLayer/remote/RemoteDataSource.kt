package com.example.catsapisampleproject.dataLayer.remote

import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageSearchDTO

interface RemoteDataSource{
    suspend fun getCatBreeds() : List<BreedDTO>
    suspend fun getCatBreedImageByReferenceImageId(referenceImageId: String) : ImageDTO
    suspend fun getFavourites() : List<FavouriteDTO>
    suspend fun postCatBreedAsFavourite(imageReferenceId: String): FavouriteDTO
    suspend fun deleteCatBreedAsFavourite(favouriteId: String): Boolean
    suspend fun getCatImagesPaginated(limit: Int, page: Int): List<ImageSearchDTO>
}