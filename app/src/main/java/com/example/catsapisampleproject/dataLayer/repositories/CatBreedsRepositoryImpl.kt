package com.example.catsapisampleproject.dataLayer.repositories

import android.util.Log
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BreedWithImage(
    val breed: CatBreed,
    val image: CatBreedImage?,
    val isFavourite: Boolean = false
)

class CatBreedsRepositoryImpl @Inject
constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : CatBreedsRepository {

    override fun getCatBreeds(): Flow<Resource<List<BreedWithImage>>> = flow {

        try {

            // Fetch breedDTOs and FavouritesDto in parallel
            val (breedDTOs, favouritesDTO) = coroutineScope {
                val breedDTOsDeferred = async { remoteDataSource.getCatBreeds() }
                val favouritesDeferred = async { remoteDataSource.getFavourites() }
                breedDTOsDeferred.await() to favouritesDeferred.await()
            }

//            //todo think better, maybe compute the favourites here and then just query the favourites db
//            //upon trying to update it
//            // Map breedDTOs to CatBreed objects and check favourites in a single pass
//            val breeds = breedDTOs.map { dto ->
//                val isFavourite = favouritesDTO.any { it.imageID == dto.referenceImageId }
//                CatBreed(
//                    id = dto.id,
//                    name = dto.name,
//                    description = dto.description,
//                    temperament = dto.temperament,
//                    origin = dto.origin,
//                    referenceImageId = dto.referenceImageId
//                )
//            }

            //todo move to a mapper
            val breeds = breedDTOs.map { dto ->
                val parts = dto.lifeSpan.split(" - ")
                val minLifeSpan = if (parts.size == 2) {
                    parts[0].trim().toIntOrNull()
                } else {
                    null
                }
                val maxLifeSpan = if (parts.size == 2) {
                    parts[1].trim().toIntOrNull()
                } else {
                    null
                }

                CatBreed(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    temperament = dto.temperament,
                    origin = dto.origin,
                    referenceImageId = dto.referenceImageId,
                    minLifeSpan = minLifeSpan,
                    maxLifeSpan = maxLifeSpan
                )
            }

            // Update the local favourites table with fresh data.
            val favouriteEntities = favouritesDTO.map { favDTO ->
                FavouriteEntity(
                    imageId = favDTO.imageID,
                    favouriteId = favDTO.favouriteID
                )
            }

            withContext(Dispatchers.IO) { localDataSource.deleteAllFavourites() }
            withContext(Dispatchers.IO) { localDataSource.insertFavourites(favouriteEntities) }

            val catBreedImageEntities = coroutineScope {
                // Iterate over the breedDTOs, mapping only those that have a non-empty referenceImageId.
                breedDTOs.mapNotNull { dto ->
                    // Check if the referenceImageId is null or empty.
                    if (dto.referenceImageId.isNullOrEmpty()) {
                        null // Skip this DTO if there is no valid referenceImageId.
                    } else {
                        // Launch a concurrent request to fetch the image.
                        async {
                            CatBreedImage(
                                breed_id = dto.id,
                                url = remoteDataSource.getCatBreedImageByReferenceImageId(dto.referenceImageId).url,
                                image_id = dto.referenceImageId
                            )
                        }
                    }
                }.awaitAll()
            }

            // Compute favourite flag using the list of remote favourites.
            // Using a set for faster look-up.
            val favouriteSet = favouritesDTO.map { it.imageID }.toSet()

            // Merge breeds and corresponding images
            val breedWithImageList = breeds.map { breed ->
                val isFavourite = breed.referenceImageId?.let { favouriteSet.contains(it) } ?: false
                val image = catBreedImageEntities.find { it.breed_id == breed.id }
                BreedWithImage(breed, image, isFavourite)
            }

            // save freshly retrieved data to local database
            withContext(Dispatchers.IO) {
                localDataSource.insertCatBreeds(breeds)
                localDataSource.insertCatBreedImages(catBreedImageEntities)
            }

            //retrieve data from the api error handling
            emit(Resource.Success(breedWithImageList))

        } catch (e: Exception) {

            Log.d("CatBreedsRepositoryImpl", "Error fetching cat breeds: ${e.message}")

            // Offline fallback: retrieve data from the local database.
            val offlineBreeds = withContext(Dispatchers.IO) { localDataSource.getCatBreeds() }
            val offlineImages = withContext(Dispatchers.IO) { localDataSource.getCatBreedImages() }
            val offlineFavourites = withContext(Dispatchers.IO) { localDataSource.getFavouriteCatBreeds() }

            // Compute favourite flag from local favourites.
            val localFavSet = offlineFavourites.map { it.imageId }.toSet()
            val breedsWithLocalFav = offlineBreeds.map { breed ->
                breed.copy()  // No isFavourite here in CatBreed
            }

            val breedWithImageList = breedsWithLocalFav.map { breed ->
                val isFavourite = breed.referenceImageId?.let { localFavSet.contains(it) } ?: false
                val image = offlineImages?.find { it.breed_id == breed.id }
                BreedWithImage(breed, image, isFavourite)
            }

            if (breedWithImageList.isEmpty()) {
                emit(Resource.Error("Unable to provide offline data"))
            } else {
                emit(Resource.Success(breedWithImageList, offline = true))
            }
        }

    }

    override fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<FavouriteEntity>> = flow {
        try {
            val favouriteDTO : FavouriteDTO
                = remoteDataSource.postCatBreedAsFavourite(imageReferenceId)

            localDataSource.insertFavourite(
                FavouriteEntity(
                favouriteId = favouriteDTO.favouriteID,
                imageId = imageReferenceId
                )
            )

            emit(Resource.Success(
                FavouriteEntity(
                favouriteId = favouriteDTO.favouriteID,
                imageId = imageReferenceId
            )
            ))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>> = flow {
        try {
            // Look up the stored favourite record by imageReferenceId.
            val favourite = localDataSource.getFavouriteByImageId(imageReferenceId)
            if (favourite != null) {
                val success = remoteDataSource.deleteCatBreedAsFavourite(favourite.favouriteId)
                if (success) {
                    localDataSource.deleteFavourite(imageReferenceId)
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error("Failed to remove favourite"))
                }
            } else {
                emit(Resource.Error("No favourite found for this breed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getCatBreed(breedId: String): Flow<Resource<BreedWithImage>> = flow {
        try {
            val breed = withContext(Dispatchers.IO) { localDataSource.getCatBreedById(breedId) }
            val image = withContext(Dispatchers.IO) { localDataSource.getCatBreedImageByBreedId(breedId) }
            val isFavourite = withContext(Dispatchers.IO) {
                image?.let {
                    localDataSource.getFavouriteByImageId(imageId = it.image_id)
                }
            }

            breed?.let {
                emit(Resource.Success(BreedWithImage(it, image, isFavourite != null)))
            }

            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
    }

}