package com.example.catsapisampleproject.dataLayer.remote

import com.example.catsapisampleproject.BuildConfig
import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteRequestDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CatAPIService {

    @GET("breeds")
    suspend fun getCatBreeds(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY
    ) : Response<List<BreedDTO>>

    @GET("images/{imageid}")
    suspend fun getCatBreedImageByReferenceImageId(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY,
        @Path("imageid") imageID: String
    ) : Response<ImageDTO>

    @GET("favourites")
    suspend fun getFavourites(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY
    ) : Response<List<FavouriteDTO>>

    @POST("favourites")
    suspend fun postCatBreedAsFavourite(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY,
        @Body favouriteDTO: FavouriteRequestDTO
    ): Response<FavouriteDTO>

    @DELETE("favourites/{favouriteid}")
    suspend fun deleteCatBreedAsFavourite(
        @Header("x-api-key") apiKey: String = BuildConfig.CAT_API_KEY,
        @Path("favouriteid") favouriteID: String
    ): Response<ResponseBody>

}