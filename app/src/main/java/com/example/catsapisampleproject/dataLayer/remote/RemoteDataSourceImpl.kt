package com.example.catsapisampleproject.dataLayer.remote

import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.dto.requests.FavouriteRequestDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import retrofit2.HttpException
import javax.inject.Inject

class RemoteDataSourceImpl @Inject constructor(
    private val catApiService: CatAPIService
) : RemoteDataSource {

    override suspend fun getCatBreeds(): List<BreedDTO> {

        val response = catApiService.getCatBreeds()

        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw HttpException(response)
        }

    }

    override suspend fun getCatBreedImageByReferenceImageId(referenceImageId: String): ImageDTO {

        val response = catApiService.getCatBreedImageByReferenceImageId(imageID = referenceImageId)

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body - TODO")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getFavourites(): List<FavouriteDTO> {
        val response = catApiService.getFavourites()

        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun postCatBreedAsFavourite(imageReferenceId: String): FavouriteDTO {
        val response = catApiService.postCatBreedAsFavourite(
            favouriteDTO = FavouriteRequestDTO(imageReferenceId)
        )

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Empty favourite response from service")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun deleteCatBreedAsFavourite(favouriteId: String): Boolean {
        val response = catApiService.deleteCatBreedAsFavourite(favouriteID = favouriteId)

        return response.isSuccessful
    }
}