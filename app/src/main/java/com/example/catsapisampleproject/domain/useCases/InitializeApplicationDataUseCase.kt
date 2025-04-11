package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case to initialize the application data. In this case is just a wrapper to facilitate
 */
class InitializeApplicationDataUseCase @Inject constructor(
    private val catBreedsRepository: CatBreedsRepository
){
    operator fun invoke() : Flow<CatBreedsRepositoryImpl.InitializationResult> = flow {
        emit(CatBreedsRepositoryImpl.InitializationResult.Loading)

        catBreedsRepository.fetchAndCacheCatBreeds().collect {
                result -> emit(result)
        }
    }
}
