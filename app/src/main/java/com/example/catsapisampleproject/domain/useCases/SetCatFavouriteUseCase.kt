package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
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
class SetCatFavouriteUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(
        imageReferenceId: String
    ): Flow<Resource<FavouriteEntity>> = flow {
        try {
            emit(Resource.Loading())
            val catBreeds = catRepository.setCatBreedAsFavourite(imageReferenceId)

            catBreeds.collect {
                    result -> emit(result)
            }
        } catch (e: HttpException) {
            //todo handling
            emit(Resource.Error(e.localizedMessage?: "Unexpected Error"))
        } catch (e: IOException){
            emit(Resource.Error(e.localizedMessage?: "Couldn't reach server. Check connection"))
        }
    }
}