package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase to set a cat breed as favourite
 */
class SetCatFavouriteUseCase @Inject constructor(
    private val repository: CatBreedsRepository
) {
    operator fun invoke(imageReferenceId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)

        repository.setCatBreedAsFavourite(imageReferenceId).collect { result ->
            when (result) {
                is Resource.Success -> emit(result)
                is Resource.Error -> {
                    if (result.error is ErrorType.OperationQueued) {
                        emit(
                            Resource.Success(true)
                        )
                    } else {
                        emit(Resource.Error(result.error))
                    }
                }

                is Resource.Loading -> emit(Resource.Loading)
            }
        }
    }
}
