package com.example.catsapisampleproject.data.remote

import com.example.catsapisampleproject.data.remote.dto.responses.BreedDTO
import com.example.catsapisampleproject.data.remote.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.data.remote.dto.requests.FavouriteRequestDTO
import com.example.catsapisampleproject.data.remote.dto.responses.ImageDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CatAPIService {

    @GET("breeds")
    suspend fun getCatBreeds() : Response<List<BreedDTO>>

    @GET("images/{imageid}")
    suspend fun getCatBreedImageByReferenceImageId(
        @Path("imageid") imageID: String
    ) : Response<ImageDTO>

    @GET("favourites")
    suspend fun getFavourites() : Response<List<FavouriteDTO>>

    @POST("favourites")
    suspend fun postCatBreedAsFavourite(
        @Body favouriteDTO: FavouriteRequestDTO
    ): Response<FavouriteDTO>

    @DELETE("favourites/{favouriteid}")
    suspend fun deleteCatBreedAsFavourite(
        @Path("favouriteid") favouriteID: String
    ): Response<ResponseBody>

}