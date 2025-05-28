package com.example.catsapisampleproject.data.repositories

import com.example.catsapisampleproject.data.remote.dto.responses.BreedDTO
import com.example.catsapisampleproject.data.remote.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.data.local.source.LocalDataSource
import com.example.catsapisampleproject.data.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.data.local.entities.PendingOperation
import com.example.catsapisampleproject.data.local.entities.isEffectiveFavourite
import com.example.catsapisampleproject.data.mappers.CatBreedDetailsEntityMapper
import com.example.catsapisampleproject.data.mappers.CatBreedEntityMapper
import com.example.catsapisampleproject.data.mappers.FavouriteEntityMapper
import com.example.catsapisampleproject.data.network.NetworkManager
import com.example.catsapisampleproject.data.remote.source.RemoteDataSource
import com.example.catsapisampleproject.data.mappers.CatBreedDetailsMapper
import com.example.catsapisampleproject.data.mappers.CatBreedImageMapper
import com.example.catsapisampleproject.data.mappers.CatBreedMapper
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.model.BreedWithImageListMapper
import com.example.catsapisampleproject.domain.model.InitializationResult
import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

                //Log.d(TAG, "Network available. Trying to fetch cat breeds.")

                // 1. Sync pending favourites
                withContext(Dispatchers.IO) {
                    val localFavs = localDataSource.getFavouriteCatBreeds()
                    syncPendingFavorites(localFavs)
                }

                // 2. Fetch breedDTOs and FavouritesDto in parallel
                val (breedDTOs, favouritesDTO) = coroutineScope {
                    val breedDTOsDeferred = async { remoteDataSource.getCatBreeds() }
                    val favouritesDeferred = async { remoteDataSource.getFavourites() }
                    breedDTOsDeferred.await() to favouritesDeferred.await()
                }

                val remoteFavourites = favouritesDTO.map { favDTO ->
                    FavouriteEntityMapper.fromDto(favDTO)
                }

                // Some might still be pending (not successful sync)
                val localPending = withContext(Dispatchers.IO) {
                    localDataSource.getFavouriteCatBreeds()
                        .filter { it.pendingOperation != PendingOperation.None }
                }

                val favouritesToBeStored = remoteFavourites + localPending

                val breeds = breedDTOs.map { dto ->
                    CatBreedEntityMapper.fromDto(dto)
                }

                val breedsDetails = breedDTOs.map { dto ->
                    CatBreedDetailsEntityMapper.fromDTO(dto)
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

                //Log.d(TAG, "Successfully retrieved and stored Cat Breeds data")
                emit(InitializationResult.Success)

            } else {
                //Log.d(TAG, "No connectivity -> Checking local data")
                // no connectivity -> try to retrieve from local database
                emit(tryToFetchOfflineData())
            }
        } catch (e: Exception) {
            //Log.d(TAG, "Exception fetching cat breeds: ${e.stackTraceToString()}. Trying offline")
            emit(tryToFetchOfflineData())
        }

    }

    private suspend fun processImagesConcurrently(breedDTOs: List<BreedDTO>) =
        coroutineScope {
            breedDTOs.mapNotNull { dto ->
                dto.referenceImageId?.let { imageId ->
                    async(Dispatchers.IO) {
                        try {
                            CatBreedImageEntity(
                                breed_id = dto.id,
                                url = remoteDataSource.getCatBreedImageByReferenceImageId(imageId).url,
                                image_id = imageId
                            )
                        } catch (e: Exception) {
                            //Log.w(TAG, "Failed to fetch image $imageId", e)
                            // failed for this image, silent fail
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
                            //Log.d(TAG, "Successfully synced add fav ${fav.imageId}. " +
                            //"Storing in local DB")
                            localDataSource.insertFavourite(
                                fav.copy(
                                    favouriteId = response.favouriteID,
                                    pendingOperation = PendingOperation.None
                                )
                            )
                        }

                        PendingOperation.Delete -> {
                            fav.favouriteId?.let { favId ->
                                //Log.d(TAG, "Successfully synced delete fav ${fav.imageId}. " +
                                //"Removing from local DB")
                                remoteDataSource.deleteCatBreedAsFavourite(favId)
                                localDataSource.deleteFavourite(fav.imageId)
                            }
                        }

                        else -> {
                            //do nothing
                        }
                    }
                } catch (e: Exception) {
                    //Log.w(TAG, "Failed to sync fav ${fav.imageId}, keeping pending state")
                }
            }
        }
    }

    private suspend fun tryToFetchOfflineData(): InitializationResult {

        // Offline fallback: retrieve data from the local database.
        val offlineBreeds = withContext(Dispatchers.IO) { localDataSource.getCatBreeds() }
        val offlineImages = withContext(Dispatchers.IO) { localDataSource.getCatBreedImages() }
        val offlineFavourites =
            withContext(Dispatchers.IO) { localDataSource.getFavouriteCatBreeds() }

        val breedWithImageList = BreedWithImageListMapper.createBreedWithImageList(
            offlineBreeds,
            offlineImages,
            offlineFavourites
        )

        return if (breedWithImageList.isEmpty()) {
            //Log.d(TAG, "No stored data available for offline functionality")
            InitializationResult.Error("Unable to provide offline data")
        } else {
            //Log.d(TAG, "Stored data available! Proceeding with offline functionality")
            InitializationResult.OfflineDataAvailable
        }

    }

    /**
     *  Fetches data from the local storage and emits updates for the "subscribing" components to act
     * accordingly
     */
    override fun observeCatBreeds():
            Flow<List<BreedWithImage>> =
        flow {

            try {

                //static fetch once
                val breeds = withContext(Dispatchers.IO) { localDataSource.getCatBreeds() }
                val images = withContext(Dispatchers.IO) { localDataSource.getCatBreedImages() }

                val favouriteFlow = localDataSource.observeFavouriteCatBreeds()

                favouriteFlow.map { favourite ->

                    BreedWithImageListMapper.createBreedWithImageList(
                        breeds,
                        images,
                        favourite
                    )

                }.collect{
                    emit(it)
                }

            } catch (e: Exception) {
                throw e
            }
        }

    override fun setCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>> =
        flow {

            if (!networkManager.isConnected()) {

                val queued = FavouriteEntity(
                    favouriteId = null,
                    imageId = imageReferenceId,
                    pendingOperation = PendingOperation.Add
                )

                localDataSource.insertFavourite(queued)
                //Log.d(TAG, "setCatBreedAsFavourite - No internet connection, insertion queued")
                emit(Resource.Error(ErrorType.OperationQueued))
                return@flow
            }

            try {
                val favouriteDTO: FavouriteDTO =
                    remoteDataSource.postCatBreedAsFavourite(imageReferenceId)

                // Check if a pending delete existed — cancel it
                val existing = localDataSource.getFavouriteByImageId(imageReferenceId)

                val resolved = if (existing?.pendingOperation == PendingOperation.Delete) {
                    //Log.d(TAG, "setCatBreedAsFavourite - Cancel the pending delete by updating the entry")
                    // Cancel the pending delete by updating the entry
                    existing.copy(
                        pendingOperation = PendingOperation.None,
                        favouriteId = favouriteDTO.favouriteID // From server if online, or keep old one if offline
                    )
                } else {
                    // Normal add
                    //Log.d(TAG, "setCatBreedAsFavourite - No pending operation found, inserting new entry")
                    FavouriteEntity(
                        imageId = imageReferenceId,
                        favouriteId = favouriteDTO.favouriteID,
                        pendingOperation = PendingOperation.None
                    )
                }

                localDataSource.insertFavourite(resolved)
                emit(Resource.Success(true))

            } catch (e: Exception) {
                //Log.d(TAG, "setCatBreedAsFavourite - Failed to favourite. Insertion queued")
                localDataSource.insertFavourite(
                    FavouriteEntity(
                        imageId = imageReferenceId,
                        favouriteId = null,
                        pendingOperation = PendingOperation.Add
                    )
                )
                emit(Resource.Error(ErrorType.OperationQueued))
            }
        }

    override fun deleteCatBreedAsFavourite(imageReferenceId: String): Flow<Resource<Boolean>> =
        flow {

            val existing = localDataSource.getFavouriteByImageId(imageReferenceId)

            if (existing == null) {
                //Log.d(TAG, "deleteCatBreedAsFavourite - Favourite not found")
                emit(Resource.Error(ErrorType.DataNotFound))
                return@flow
            }

            if (!networkManager.isConnected()) {
                //Log.d(TAG, "deleteCatBreedAsFavourite - No internet connection")

                when (existing.pendingOperation) {
                    PendingOperation.Add -> {
                        // The favourite was never committed to the server, delete it from db
                        //Log.d(TAG, "deleteCatBreedAsFavourite - Cancelling pending insertion")
                        localDataSource.deleteFavourite(imageReferenceId)
                    }

                    else -> {
                        // Mark for deletion
                        //Log.d(TAG, "deleteCatBreedAsFavourite - Marking existing as pending deletion")
                        if (existing.pendingOperation != PendingOperation.Delete) {
                            localDataSource.insertFavourite(
                                existing.copy(pendingOperation = PendingOperation.Delete)
                            )
                        }
                    }
                }

                emit(Resource.Error(ErrorType.OperationQueued))
                return@flow
            }

            try {
                if (!existing.favouriteId.isNullOrEmpty()) {
                    remoteDataSource.deleteCatBreedAsFavourite(existing.favouriteId)
                }

                //Log.d(TAG, "deleteCatBreedAsFavourite - Favourite deleted, removing straight from db")
                localDataSource.deleteFavourite(imageReferenceId)

                emit(Resource.Success(true))
            } catch (e: Exception) {
                // Queue deletion if server fails
                //Log.d(TAG, "deleteCatBreedAsFavourite - Exception: ${e.message}")
                localDataSource.insertFavourite(
                    existing.copy(pendingOperation = PendingOperation.Delete)
                )
                emit(Resource.Error(ErrorType.OperationQueued))
            }
        }.catch { e ->
            emit(Resource.Error(ErrorType.UnknownError))
        }

    /**
     *  Fetches data from the local storage and emits updates for the "subscribing" components to act
     * accordingly
     */
    override fun getCatBreedDetailsByIdWithFavourite(breedId: String)
            : Flow<BreedWithImageAndDetails> = flow {

        val breed = withContext(Dispatchers.IO) { localDataSource.getCatBreedById(breedId) }
        if (breed == null) {
            throw IllegalStateException("Breed not found")
        }

        val image = withContext(Dispatchers.IO) {
            localDataSource.getCatBreedImageByBreedId(breedId)
        }

        val details = withContext(Dispatchers.IO) {
            localDataSource.getCatBreedDetails(breedId)
        }

        val imageId = breed.referenceImageId

        if (imageId == null) {
            //this is due to the fact that a favourite being always associated by the imageId
            emit(
                BreedWithImageAndDetails(
                    breed = CatBreedMapper.fromEntity(breed),
                    image = null,
                    details = details?.let { CatBreedDetailsMapper.fromEntity(it) },
                    isFavourite = false
                )
            )
            return@flow
        }

        localDataSource.observeFavouriteByImageId(imageId).map { favourite ->
            val isFavourite = favourite?.isEffectiveFavourite() == true
                BreedWithImageAndDetails(
                    breed = CatBreedMapper.fromEntity(breed),
                    image = image?.let { CatBreedImageMapper.fromEntity(it) },
                    details = details?.let { CatBreedDetailsMapper.fromEntity(it) },
                    isFavourite = isFavourite
                )

        }.collect { result ->
            emit(result)
        }
    }

    override fun observeFavouriteByImageId(imageId: String): Flow<FavouriteEntity?> {
        return localDataSource.observeFavouriteByImageId(imageId)
    }
}