package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
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
            val catBreeds = catRepository.observeCatBreeds()

            catBreeds.collect {
                result -> emit(result)
            }
    }
}