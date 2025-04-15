package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.mappers.FavouriteMapper
import com.example.catsapisampleproject.domain.model.Favourite
import com.example.catsapisampleproject.domain.model.FavouriteStatus
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
    operator fun invoke(imageReferenceId: String): Flow<Resource<Favourite>> = flow {
        emit(Resource.Loading)

        repository.setCatBreedAsFavourite(imageReferenceId).collect { result ->
            when (result) {
                is Resource.Success -> emit(Resource.Success(FavouriteMapper.fromEntity(result.data)))
                is Resource.Error -> {
                    if (result.error is ErrorType.OperationQueued) {
                        emit(
                            Resource.Success(
                                Favourite(
                                    imageId = imageReferenceId,
                                    favouriteId = null,
                                    status = FavouriteStatus.PendingAdd
                                )
                            )
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
