package com.example.catsapisampleproject.dataLayer.repositories

import android.util.Log
import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.local.entities.PendingOperation
import com.example.catsapisampleproject.dataLayer.local.entities.isEffectiveFavourite
import com.example.catsapisampleproject.dataLayer.mappers.CatBreedDetailsMapper
import com.example.catsapisampleproject.dataLayer.mappers.CatBreedMapper
import com.example.catsapisampleproject.dataLayer.mappers.FavouriteEntityMapper
import com.example.catsapisampleproject.dataLayer.mappers.createBreedWithImageList
import com.example.catsapisampleproject.dataLayer.network.NetworkManager
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BreedWithImage(
    val breed: CatBreed,
    val image: CatBreedImage?,
    val isFavourite: Boolean = false
)

data class BreedWithImageAndDetails(
    val breed: CatBreed,
    val image: CatBreedImage?,
    val details: CatBreedDetailsEntity?,
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

                // Sync pending favourites
                withContext(Dispatchers.IO) {
                    val localFavs = localDataSource.getFavouriteCatBreeds()
                    syncPendingFavorites(localFavs)
                }

                // Fetch breedDTOs and FavouritesDto in parallel
                val (breedDTOs, favouritesDTO) = coroutineScope {
                    val breedDTOsDeferred = async { remoteDataSource.getCatBreeds() }
                    val favouritesDeferred = async { remoteDataSource.getFavourites() }
                    breedDTOsDeferred.await() to favouritesDeferred.await()
                }

                val remoteFavourites = favouritesDTO.map { favDTO ->
                    FavouriteEntityMapper().fromDto(favDTO)
                }

                // Some might still be pending (not successful sync)
                val localPending = withContext(Dispatchers.IO){
                    localDataSource.getFavouriteCatBreeds()
                        .filter { it.pendingOperation != PendingOperation.None }
                }

                val favouritesToBeStored = remoteFavourites + localPending

                val breeds = breedDTOs.map { dto ->
                    CatBreedMapper().fromDto(dto)
                }

                val breedsDetails = breedDTOs.map { dto ->
                    CatBreedDetailsMapper().fromDTO(dto)
                }

                val catBreedImageEntities = processImagesConcurrently(breedDTOs)

                // save freshly retrieved data to local database
                withContext(Dispatchers.IO) {
                    localDataSource.insertCatBreeds(breeds)
                    localDataSource.insertCatBreedsDetails(breedsDetails)
                    localDataSource.insertCatBreedImages(catBreedImageEntities)
                    localDataSource.deleteAllFavourites()
                    localDataSource.insertFavourites(favouritesToBeStored)
                }

                Log.d(TAG, "Successfully retrieved and stored Cat Breeds data")
                emit(InitializationResult.Success)

            } else {
                Log.d(TAG, "No connectivity -> Checking local data")
                // no connectivity -> try to retrieve from local database
                emit(tryToFetchOfflineData())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Exception fetching cat breeds: ${e.stackTraceToString()}. Trying offline")
            emit(tryToFetchOfflineData())
        }

    }

    private suspend fun processImagesConcurrently(breedDTOs: List<BreedDTO>) =
        coroutineScope {
            breedDTOs.mapNotNull { dto ->
                dto.referenceImageId?.let { imageId ->
                    async(Dispatchers.IO) {
                        try {
                            CatBreedImage(
                                breed_id = dto.id,
                                url = remoteDataSource.getCatBreedImageByReferenceImageId(imageId).url,
                                image_id = imageId
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch image $imageId", e)
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }

    private suspend fun syncPendingFavorites(pending: List<FavouriteEntity>) {
        pending.forEach { fav ->
            withContext(Dispatchers.IO) {
                try {
                    when (fav.pendingOperation) {
                        PendingOperation.Add -> {
                            val response = remoteDataSource.postCatBreedAsFavourite(fav.imageId)
                            Log.d(TAG, "Successfully synced add fav ${fav.imageId}. " +
                                    "Storing in local DB")
                            localDataSource.insertFavourite(
                                fav.copy(
                                    favouriteId = response.favouriteID,
                                    pendingOperation = PendingOperation.None
                                )
                            )
                        }

                        PendingOperation.Delete -> {
                            fav.favouriteId?.let { favId ->
                                Log.d(TAG, "Successfully synced delete fav ${fav.imageId}. " +
                                        "Removing from local DB")
                                remoteDataSource.deleteCatBreedAsFavourite(favId)
                                localDataSource.deleteFavourite(fav.imageId)
                            }
                        }

                        else -> {
                            //do nothing
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync fav ${fav.imageId}, keeping pending state")
                }
            }
        }
    }

    private suspend fun tryToFetchOfflineData() : InitializationResult {

        // Offline fallback: retrieve data from the local database.
        val offlineBreeds = withContext(Dispatchers.IO) { localDataSource.getCatBreeds() }
        val offlineImages = withContext(Dispatchers.IO) { localDataSource.getCatBreedImages() }
        val offlineFavourites = withContext(Dispatchers.IO) { localDataSource.getFavouriteCatBreeds() }

        val breedWithImageList = createBreedWithImageList(
            offlineBreeds,
            offlineImages,
            offlineFavourites
        )

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

    override fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<FavouriteEntity>> = flow {

        if (!networkManager.isConnected()) {

            localDataSource.insertFavourite(
                FavouriteEntity(
                    favouriteId = null,
                    imageId = imageReferenceId,
                    pendingOperation = PendingOperation.Add
                )
            )
            Log.d(TAG, "setCatBreedAsFavourite - No internet connection, insertion queued")
            emit(Resource.Error("No internet connection, operation queued"))
            return@flow
        }

        try {
            val favouriteDTO : FavouriteDTO
                = remoteDataSource.postCatBreedAsFavourite(imageReferenceId)

            // Check if a pending delete existed — cancel it
            val existing = localDataSource.getFavouriteByImageId(imageReferenceId)

            if (existing?.pendingOperation == PendingOperation.Delete) {
                Log.d(TAG, "setCatBreedAsFavourite - Cancel the pending delete by updating the entry")
                // Cancel the pending delete by updating the entry
                val updated = existing.copy(
                    pendingOperation = PendingOperation.None,
                    favouriteId = favouriteDTO.favouriteID // From server if online, or keep old one if offline
                )
                localDataSource.insertFavourite(updated)
            } else {
                // Normal add
                Log.d(TAG, "setCatBreedAsFavourite - No pending operation found, inserting new entry")
                val newEntry = FavouriteEntity(
                    imageId = imageReferenceId,
                    favouriteId = favouriteDTO.favouriteID,
                    pendingOperation = PendingOperation.None
                )
                localDataSource.insertFavourite(newEntry)
            }

            emit(Resource.Success(
                FavouriteEntity(
                favouriteId = favouriteDTO.favouriteID,
                imageId = imageReferenceId
            )
            ))

        } catch (e: Exception) {
            Log.d(TAG, "setCatBreedAsFavourite - Failed to favourite. Insertion queued")
            localDataSource.insertFavourite(
                FavouriteEntity(imageId = imageReferenceId,
                    favouriteId = null,
                    pendingOperation = PendingOperation.Add
                )
            )
            emit(Resource.Error("Failed to favourite. Operation queued"))
        }
    }

    override fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>> = flow {

        val existing = localDataSource.getFavouriteByImageId(imageReferenceId)

        if (existing == null) {
            Log.d(TAG, "deleteCatBreedAsFavourite - Favourite not found")
            emit(Resource.Error("Favourite not found"))
            return@flow
        }

        if (!networkManager.isConnected()) {
            Log.d(TAG, "deleteCatBreedAsFavourite - No internet connection")

            when (existing.pendingOperation) {
                PendingOperation.Add -> {
                    // The favourite was never committed to the server, delete it from db
                    Log.d(TAG, "deleteCatBreedAsFavourite - Cancelling pending insertion")
                    localDataSource.deleteFavourite(imageReferenceId)
                }
                else -> {
                    // Mark for deletion
                    Log.d(TAG, "deleteCatBreedAsFavourite - Marking existing as pending deletion")
                    if (existing.pendingOperation != PendingOperation.Delete) {
                        localDataSource.insertFavourite(
                            existing.copy(pendingOperation = PendingOperation.Delete)
                        )
                    }
                }
            }

            emit(Resource.Error("No internet connection, deletion handled offline"))
            return@flow
        }

        try {
            if (!existing.favouriteId.isNullOrEmpty()) {
                remoteDataSource.deleteCatBreedAsFavourite(existing.favouriteId)
            }

            Log.d(TAG, "deleteCatBreedAsFavourite - Favourite deleted, removing straight from db")
            localDataSource.deleteFavourite(imageReferenceId)

            emit(Resource.Success(true))
        } catch (e: Exception) {
            // Queue deletion if server fails
            Log.d(TAG, "deleteCatBreedAsFavourite - Exception: ${e.message}")
            localDataSource.insertFavourite(
                existing.copy(pendingOperation = PendingOperation.Delete)
            )
            emit(Resource.Error("Failed to delete favourite. Operation queued"))
        }
    }

    /**
     *  Fetches data from the local storage and emits updates for the "subscribing" components to act
     * accordingly
     */
    override fun observeCatBreedDetailsById(breedId: String): Flow<Resource<BreedWithImageAndDetails>> = flow {
        val breed = withContext(Dispatchers.IO) { localDataSource.getCatBreedById(breedId) }

        if (breed == null) {
            emit(Resource.Error("Cat breed not found"))
            return@flow
        }


        val image = withContext(Dispatchers.IO) { localDataSource.getCatBreedImageByBreedId(breedId) }
        val details = withContext(Dispatchers.IO) { localDataSource.getCatBreedDetails(breedId) }
        val imageId = breed.referenceImageId

        if (imageId == null) {
            emit(Resource.Success(BreedWithImageAndDetails(breed, image, isFavourite = false, details = details)))
            return@flow
        }

        emitAll(
            localDataSource.observeFavouriteByImageId(imageId).map { favourite ->
                val isFavourite = favourite?.isEffectiveFavourite() == true
                Resource.Success(BreedWithImageAndDetails(breed, image, isFavourite = isFavourite, details = details))
            }
        )
    }.catch {
        emit(Resource.Error(it.message ?: "Unexpected error"))
    }


//    override fun getCatBreed(breedId: String): Flow<Resource<BreedWithImage>> = flow {
//        try {
//            val breed = withContext(Dispatchers.IO) { localDataSource.getCatBreedById(breedId) }
//            val image = withContext(Dispatchers.IO) { localDataSource.getCatBreedImageByBreedId(breedId) }
//            val isFavourite = withContext(Dispatchers.IO) {
//                image?.let {
//                    localDataSource.getFavouriteByImageId(imageId = it.image_id)
//                }
//            }
//
//            breed?.let {
//                emit(Resource.Success(BreedWithImage(it, image, isFavourite != null)))
//            }
//
//            } catch (e: Exception) {
//                emit(Resource.Error(e.message ?: "Unknown error"))
//            }
//    }

}