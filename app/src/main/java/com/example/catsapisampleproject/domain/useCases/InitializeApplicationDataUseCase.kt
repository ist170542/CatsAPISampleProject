package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepositoryImpl
import com.example.catsapisampleproject.domain.model.AppInitResult
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case to initialize the application data. In this case is just a wrapper to facilitate
 */
class InitializeApplicationDataUseCase @Inject constructor(
    private val catBreedsRepository: CatBreedsRepository
) {
    operator fun invoke(): Flow<Resource<AppInitResult>> = flow {
        emit(Resource.Loading)

        catBreedsRepository.fetchAndCacheCatBreeds().collect { result ->
            val mapped = when (result) {
                is CatBreedsRepositoryImpl.InitializationResult.Success -> {
                    AppInitResult.Success
                }

                is CatBreedsRepositoryImpl.InitializationResult.Error -> {
                    AppInitResult.Failure(result.message)
                }

                CatBreedsRepositoryImpl.InitializationResult.OfflineDataAvailable -> {
                    AppInitResult.OfflineMode
                }

                CatBreedsRepositoryImpl.InitializationResult.Loading -> return@collect
            }

            emit(Resource.Success(mapped))
        }
    }.catch {
        emit(Resource.Error(ErrorType.UnknownError))
    }
}