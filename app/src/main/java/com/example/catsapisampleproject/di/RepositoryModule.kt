package com.example.catsapisampleproject.di

import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide Repository related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    //todo dont forget to add local data sources
    fun provideCatBreedsRepository(remoteDataSource: RemoteDataSource): CatBreedsRepository {
        return CatBreedsRepositoryImpl(remoteDataSource)
    }
}