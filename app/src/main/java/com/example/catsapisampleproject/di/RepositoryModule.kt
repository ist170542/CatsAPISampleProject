package com.example.catsapisampleproject.di

import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.network.NetworkManager
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
    fun provideCatBreedsRepository(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        networkManager: NetworkManager
    ): CatBreedsRepository {
        return CatBreedsRepositoryImpl(remoteDataSource, localDataSource, networkManager)
    }
}