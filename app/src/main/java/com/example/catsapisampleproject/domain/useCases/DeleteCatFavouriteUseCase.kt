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
    operator fun invoke(imageReferenceId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)

        catRepository.deleteCatBreedAsFavourite(imageReferenceId).collect { result ->
            val mapped = when (result) {
                is Resource.Success -> Resource.Success(true)
                is Resource.Error ->
                    if (result.error is ErrorType.OperationQueued)
                        Resource.Success(true)
                    else Resource.Error(result.error)
                is Resource.Loading -> null // ignore duplicate loading
            }

            mapped?.let { emit(it) }
        }
    }
}