package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase to get a specific breed
 */
class GetCatBreedWithDetailsUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(breedId: String): Flow<Resource<BreedWithImageAndDetails>> = flow {
        emit(Resource.Loading)

        try {
            catRepository.getCatBreedDetailsByIdWithFavourite(breedId).collect{
                emit(Resource.Success(it))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorType.DatabaseError))
        }
    }
}