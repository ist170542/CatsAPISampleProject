package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.BreedWithImageListMapper
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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

            val (breeds, images, favouritesFlow) = catRepository.observeCatBreeds().first()

            favouritesFlow.map { favourites ->

                val result = BreedWithImageListMapper.createBreedWithImageList(
                    breeds, images, favourites
                )
                Resource.Success(result)

            }.collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorType.UnknownError))
        }
    }
}