package com.example.catsapisampleproject.dataLayer.repositories

import android.util.Log
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.mappers.CatBreedMapper
import com.example.catsapisampleproject.dataLayer.mappers.FavouriteEntityMapper
import com.example.catsapisampleproject.dataLayer.network.NetworkManager
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
    private val localDataSource: LocalDataSource,
    private val networkManager: NetworkManager
) : CatBreedsRepository {

    private val TAG = "CatBreedsRepositoryImpl"

    /**
     *  Checks connectivity and tries to retrieve Cat Breed data from server and store it locally
     * or if not successful, tries to retrieve previously stored data
     */
    override fun fetchAndCacheCatBreeds(): Flow<InitializationResult> = flow {

        try {
            if (networkManager.isConnected()) {
                // try to retrieve from network

                Log.d(TAG, "Network available. Trying to fetch cat breeds.")

                // Fetch breedDTOs and FavouritesDto in parallel
                val (breedDTOs, favouritesDTO) = coroutineScope {
                    val breedDTOsDeferred = async { remoteDataSource.getCatBreeds() }
                    val favouritesDeferred = async { remoteDataSource.getFavourites() }
                    breedDTOsDeferred.await() to favouritesDeferred.await()
                }

                val breeds = breedDTOs.map { dto ->
                    CatBreedMapper().fromDto(dto)
                }

                localDataSource.insertCatBreeds(breeds)

                val favouriteEntities = favouritesDTO.map { favDTO ->
                   FavouriteEntityMapper().fromDto(favDTO)
                }

                // Update the local favourites table with fresh data.
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

                // save freshly retrieved data to local database
                withContext(Dispatchers.IO) {
                    localDataSource.insertCatBreeds(breeds)
                    localDataSource.insertCatBreedImages(catBreedImageEntities)
                }

                Log.d(TAG, "Successfully retrieved and stored Cat Breeds data")
                emit(InitializationResult.Success)

            } else {
                Log.d(TAG, "No connectivity -> Checking local data")
                // no connectivity -> try to retrieve from local database
                emit(tryToFetchOfflineData())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception fetching cat breeds: ${e.message}. Trying offline")
            emit(tryToFetchOfflineData())
        }

    }

    private suspend fun tryToFetchOfflineData() : InitializationResult {

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

        return if (breedWithImageList.isEmpty()) {
            Log.d(TAG, "No stored data available for offline functionality")
            InitializationResult.Error("Unable to provide offline data")
        } else {
            Log.d(TAG, "Stored data available! Proceeding with offline functionality")
            InitializationResult.OfflineDataAvailable
        }

    }

    sealed class InitializationResult {
        data object Loading : InitializationResult()
        data object Success : InitializationResult()
        data object OfflineDataAvailable : InitializationResult()
        data class Error(val message: String) : InitializationResult()
    }

    /**
     *  Fetches data from the local storage and emits updates for the "subscribing" components to act
     * accordingly
     */
    override fun observeCatBreeds(): Flow<Resource<List<BreedWithImage>>> = flow {

        try {
            val combinedFlow = combine(
                localDataSource.observeCatBreeds(),         // Flow<List<CatBreed>>
                localDataSource.observeCatBreedImages(),     // Flow<List<CatBreedImage>>
                localDataSource.observeFavouriteCatBreeds()  // Flow<List<FavouriteEntity>>
            ) { breeds, images, favourites ->
                // Combine the three lists into one list of BreedWithImage.
                createBreedWithImageList(breeds, images, favourites)
            }.flowOn(Dispatchers.IO)

            combinedFlow.collect { breedWithImageList ->
                emit(Resource.Success(breedWithImageList))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error reading from the Database"))
        }
    }

    private fun createBreedWithImageList(breeds: List<CatBreed>,
                                         images: List<CatBreedImage>?,
                                         favourites: List<FavouriteEntity>): List<BreedWithImage> {

        val favouriteSet = favourites.map { it.imageId }.toSet()


        return breeds.map { breed ->
            val isFavourite = breed.referenceImageId?.let { favouriteSet.contains(it) } ?: false
            val image = images?.find { it.breed_id == breed.id }
            BreedWithImage(breed, image, isFavourite)
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