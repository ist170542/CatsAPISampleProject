package com.example.catsapisampleproject.di

import com.example.catsapisampleproject.dataLayer.remote.CatAPIService
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSourceImpl
import com.example.catsapisampleproject.util.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Hilt Module to provide API and Remote data source
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCatAPI(): Retrofit =
        Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCatAPIService(retrofit: Retrofit): CatAPIService =
        retrofit.create(CatAPIService::class.java)

    @Provides
    @Singleton
    fun provideRemoteDataSource(api: CatAPIService): RemoteDataSource {
        return RemoteDataSourceImpl(api)
    }


}