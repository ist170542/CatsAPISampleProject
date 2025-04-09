package com.example.catsapisampleproject.domain.useCases

import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * UseCase to get the list of breeds
 */
class GetCatBreedsUseCase @Inject constructor(
    private val catRepository: CatBreedsRepository
) {
    operator fun invoke(): Flow<Resource<List<CatBreed>>> = flow {
        try {
            emit(Resource.Loading())
            val catBreeds = catRepository.getCatBreeds()

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