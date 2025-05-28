package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject

/**
 * UseCase to get the list of breeds
 */
class GetCatBreedsUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(): Flow<Resource<List<BreedWithImage>>> = flow {

        emit(Resource.Loading)

        try {

            catRepository.observeCatBreeds().collect {
                emit(Resource.Success(it))
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorType.UnknownError))
        }
    }
}