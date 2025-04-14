package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImageAndDetails
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase to get a specific breed
 */
class GetCatBreedUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(breedId: String): Flow<Resource<BreedWithImageAndDetails>> = flow {
            emit(Resource.Loading)
            val catBreeds = catRepository.observeCatBreedDetailsById(breedId)

            catBreeds.collect {
                    result -> emit(result)
            }
    }
}