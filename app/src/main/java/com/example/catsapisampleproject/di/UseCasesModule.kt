package com.example.catsapisampleproject.di

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.useCases.CatBreedsUseCases
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedWithDetailsUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide the UseCase dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    @Singleton
    fun provideCatBreedsUseCases(repository: CatBreedsRepository): CatBreedsUseCases {
        return CatBreedsUseCases(
            getCatBreedWithDetailsUseCase = GetCatBreedWithDetailsUseCase(repository),
            getBreedsUseCase = GetCatBreedsUseCase(repository),
            setCatFavouriteUseCase = SetCatFavouriteUseCase(repository),
            deleteCatFavouriteUseCase = DeleteCatFavouriteUseCase(repository),
        )
    }

}