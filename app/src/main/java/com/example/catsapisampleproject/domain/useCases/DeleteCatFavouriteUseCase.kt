package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
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
        try {
            emit(Resource.Loading())
            val catBreeds = catRepository.deleteCatBreedAsFavourite(imageReferenceId = imageReferenceId)

            catBreeds.collect {
                    result -> emit(result)
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage?: "Unexpected Error"))
        } catch (e: IOException){
            emit(Resource.Error(e.localizedMessage?: "Couldn't reach server. Check connection"))
        }
    }
}