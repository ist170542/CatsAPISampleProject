package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase to set a cat breed as favourite
 */
class DeleteCatFavouriteUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(
        imageReferenceId: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        val catBreeds = catRepository.deleteCatBreedAsFavourite(imageReferenceId = imageReferenceId)

        catBreeds.collect { result ->
            emit(result)

            when (result) {
                is Resource.Error -> {
                    if (result.error is ErrorType.OperationQueued) {
                        // Treat queued operations as success (UI wise is the same)
                        emit(
                            Resource.Success(true)
                        )
                    } else {
                        emit(result)
                    }
                }

                else -> emit(result)
            }
        }
    }
}