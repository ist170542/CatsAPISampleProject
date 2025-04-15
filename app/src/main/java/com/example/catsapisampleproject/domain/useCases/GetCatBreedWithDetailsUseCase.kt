package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.local.entities.isEffectiveFavourite
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.mappers.CatBreedDetailsMapper
import com.example.catsapisampleproject.domain.mappers.CatBreedImageMapper
import com.example.catsapisampleproject.domain.mappers.CatBreedMapper
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
            val catDetailsFlow = catRepository.getCatBreedDetailsById(breedId)
            val breedTriple = catDetailsFlow.first()

            val breed = breedTriple.first
            val image = breedTriple.second
            val details = breedTriple.third

            val imageId = breed.referenceImageId

            if (imageId == null) {
                //this is due to the fact that a favourite being always associated by the imageId
                emit(
                    Resource.Success(
                        BreedWithImageAndDetails(
                            breed = CatBreedMapper.fromEntity(breed),
                            image = null,
                            details = details?.let { CatBreedDetailsMapper.fromEntity(it) },
                            isFavourite = false
                        )
                    )
                )
                return@flow
            }

            catRepository.observeFavouriteByImageId(imageId).map { favourite ->
                val isFavourite = favourite?.isEffectiveFavourite() == true

                Resource.Success(
                    BreedWithImageAndDetails(
                        breed = CatBreedMapper.fromEntity(breed),
                        image = image?.let { CatBreedImageMapper.fromEntity(it) },
                        details = details?.let { CatBreedDetailsMapper.fromEntity(it) },
                        isFavourite = isFavourite
                    )
                )
            }.collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            emit(Resource.Error(ErrorType.DatabaseError))
        }
    }
}