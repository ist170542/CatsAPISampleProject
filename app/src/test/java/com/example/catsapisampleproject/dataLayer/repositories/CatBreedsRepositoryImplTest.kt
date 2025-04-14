package com.example.catsapisampleproject.dataLayer.repositories

import app.cash.turbine.test
import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.local.entities.PendingOperation
import com.example.catsapisampleproject.dataLayer.network.NetworkManager
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatBreedsRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var networkManager: NetworkManager
    private lateinit var repository: CatBreedsRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        remoteDataSource = mockk(relaxed = true)
        localDataSource = mockk(relaxed = true)
        networkManager = mockk(relaxed = true)
        repository = CatBreedsRepositoryImpl(remoteDataSource, localDataSource, networkManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeCatBreeds emits combined data correctly`() = runTest {
        // Arrange
        val breed = CatBreed(
            id = "1",
            name = "Abyssinian",
            referenceImageId = "img1",
            minLifeSpan = 10,
            maxLifeSpan = 15
        )
        val image = CatBreedImage(
            breed_id = "1",
            image_id = "img1",
            url = "http://test.com/cat.jpg"
        )
        val favourite = FavouriteEntity(
            imageId = "img1",
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )

        // Create a MutableSharedFlow with replay so that the latest emission is re-emitted.
        val favouriteFlow = MutableSharedFlow<List<FavouriteEntity>>(replay = 1)

        // Stubbing the local data source static functions.
        coEvery { localDataSource.getCatBreeds() } returns listOf(breed)
        coEvery { localDataSource.getCatBreedImages() } returns listOf(image)
        // For observeFavouriteCatBreeds, we use our shared flow.
        coEvery { localDataSource.observeFavouriteCatBreeds() } returns favouriteFlow

        // Act & Assert using Turbine.
        repository.observeCatBreeds().test {
            // First, emit an initial favourite list via the shared flow.
            favouriteFlow.emit(listOf(favourite))
            advanceUntilIdle()  // Ensure the emission is processed

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)

            // Check the combined data
            if (result is Resource.Success) {
                val data = result.data
                assertThat(data).isNotNull()
                assertThat(data).hasSize(1)
                val combined = data!!.first()
                assertThat(combined.breed).isEqualTo(breed)
                assertThat(combined.image?.url).isEqualTo("http://test.com/cat.jpg")
                assertThat(combined.isFavourite).isTrue()
            }

            // Now, simulate the favourite being removed by emitting an empty list.
            favouriteFlow.emit(emptyList())
            advanceUntilIdle()

            val result2 = awaitItem()
            assertThat(result2).isInstanceOf(Resource.Success::class.java)

            if (result2 is Resource.Success) {
                val data2 = result2.data
                assertThat(data2).isNotNull()
                assertThat(data2).hasSize(1)
                val combined2 = data2!!.first()
                assertThat(combined2.breed).isEqualTo(breed)
                assertThat(combined2.image?.url).isEqualTo("http://test.com/cat.jpg")
                assertThat(combined2.isFavourite).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCatBreeds handles database errors`() = runTest {
        // Arrange: simulate an exception in one of the flows.
        coEvery { localDataSource.observeFavouriteCatBreeds() } returns flow { throw Exception("Database error") }

        repository.observeCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)

            // Safely access the error within the Error state
            if (result is Resource.Error) {
                assertThat(result.error).isEqualTo(ErrorType.DatabaseError)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- Tests for fetchAndCacheCatBreeds() -----
    @Test
    fun `fetchAndCacheCatBreeds returns Success when network is available`() = runTest {
        // Arrange: simulate network connectivity.
        coEvery { networkManager.isConnected() } returns true

        val sampleBreedDTO = BreedDTO(
            id = "1",
            name = "Abyssinian",
            description = "Desc",
            temperament = "Active",
            origin = "Egypt",
            referenceImageId = "img1",
            lifeSpan = "10 - 15"
        )
        val sampleFavouriteDTO = FavouriteDTO(
            favouriteID = "fav1",
            imageID = "img1"
        )
        val sampleImageDTO = ImageDTO(
            url = "http://test.com/cat.jpg",
            id = "img1"
        )
        coEvery { remoteDataSource.getCatBreeds() } returns listOf(sampleBreedDTO)
        coEvery { remoteDataSource.getFavourites() } returns listOf(sampleFavouriteDTO)
        coEvery { remoteDataSource.getCatBreedImageByReferenceImageId("img1") } returns sampleImageDTO

        // Simulate empty local data initially.
        coEvery { localDataSource.getCatBreeds() } returns emptyList()
        coEvery { localDataSource.getCatBreedImages() } returns emptyList()
        coEvery { localDataSource.getFavouriteCatBreeds() } returns emptyList()

        // Prepare local data insertion mocks.
        coEvery { localDataSource.insertCatBreeds(any()) } just Runs
        coEvery { localDataSource.insertCatBreedsDetails(any()) } just Runs
        coEvery { localDataSource.insertCatBreedImages(any()) } just Runs
        coEvery { localDataSource.deleteAllFavourites() } just Runs
        coEvery { localDataSource.insertFavourites(any()) } just Runs

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds returns OfflineDataAvailable when offline data exists`() = runTest {
        // Arrange: simulate no network connectivity.
        coEvery { networkManager.isConnected() } returns false

        val localBreed = CatBreed(
            id = "1",
            name = "Abyssinian",
            referenceImageId = "img1",
            minLifeSpan = 10,
            maxLifeSpan = 15
        )
        val localImage = CatBreedImage(
            breed_id = "1",
            image_id = "img1",
            url = "http://example.com/cat.jpg"
        )
        val localFavourite = FavouriteEntity(
            imageId = "img1",
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )
        coEvery { localDataSource.getCatBreeds() } returns listOf(localBreed)
        coEvery { localDataSource.getCatBreedImages() } returns listOf(localImage)
        coEvery { localDataSource.getFavouriteCatBreeds() } returns listOf(localFavourite)

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.OfflineDataAvailable::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds returns Error when no offline data available`() = runTest {
        // Arrange: simulate no network and no offline data.
        coEvery { networkManager.isConnected() } returns false
        coEvery { localDataSource.getCatBreeds() } returns emptyList()
        coEvery { localDataSource.getCatBreedImages() } returns emptyList()
        coEvery { localDataSource.getFavouriteCatBreeds() } returns emptyList()

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds handles breeds without referenceImageId`() = runTest {
        // Arrange: a breed without a referenceImageId should not trigger an image fetch.
        coEvery { networkManager.isConnected() } returns true
        val breedWithoutImage = BreedDTO(
            id = "1",
            name = "Abyssinian",
            description = "Desc",
            temperament = "Active",
            origin = "Egypt",
            referenceImageId = null,
            lifeSpan = "10 - 15"
        )
        coEvery { remoteDataSource.getCatBreeds() } returns listOf(breedWithoutImage)

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { remoteDataSource.getCatBreedImageByReferenceImageId(any()) }
    }

    @Test
    fun `fetchAndCacheCatBreeds handles local insert failure`() = runTest {
        // Arrange: Simulate a DB error during insert.
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.getCatBreeds() } returns emptyList()
        coEvery { localDataSource.insertCatBreeds(any()) } throws Exception("DB Error")

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds handles partial network failures`() = runTest {
        // Arrange: Simulate a failure during remote fetch.
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.getCatBreeds() } throws Exception("Network error")

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(CatBreedsRepositoryImpl.InitializationResult.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- Tests for setCatBreedAsFavourite() -----
    @Test
    fun `setCatBreedAsFavourite returns Success when remote call succeeds`() = runTest {
        val imageReferenceId = "img1"
        val favouriteDTO = FavouriteDTO(favouriteID = "fav1", imageID = imageReferenceId)
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.postCatBreedAsFavourite(imageReferenceId) } returns favouriteDTO

        // Simulate that no record exists initially.
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns null

        repository.setCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            val favEntity = (result as Resource.Success).data
            assertThat(favEntity?.favouriteId).isEqualTo("fav1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setCatBreedAsFavourite returns Error when remote call fails and queues operation`() = runTest {
        val imageReferenceId = "img1"
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.postCatBreedAsFavourite(imageReferenceId) } throws Exception("Remote error")
        // Simulate no record exists
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns null

        repository.setCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ----- Tests for deleteCatBreedAsFavourite() -----
    @Test
    fun `deleteCatBreedAsFavourite returns Success when remote deletion succeeds`() = runTest {
        val imageReferenceId = "img1"
        val favEntity = FavouriteEntity(imageId = imageReferenceId, favouriteId = "fav1", pendingOperation = PendingOperation.None)
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns favEntity
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.deleteCatBreedAsFavourite("fav1") } returns true
        coEvery { localDataSource.deleteFavourite(imageReferenceId) } just Runs

        repository.deleteCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { localDataSource.deleteFavourite(imageReferenceId) }
    }

    @Test
    fun `deleteCatBreedAsFavourite returns Error when no favourite is found`() = runTest {
        val imageReferenceId = "img1"
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns null

        repository.deleteCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteCatBreedAsFavourite returns Error and queues deletion when offline and pending operation is not ADD`() = runTest {
        val imageReferenceId = "img1"
        val favEntity = FavouriteEntity(
            imageId = imageReferenceId,
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns favEntity
        coEvery { networkManager.isConnected() } returns false

        repository.deleteCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        // Verify that if there was no pending operation, it gets marked for deletion.
        coVerify { localDataSource.insertFavourite(match { it.pendingOperation == PendingOperation.Delete }) }
    }

    @Test
    fun `deleteCatBreedAsFavourite cancels pending addition when offline`() = runTest {
        val imageReferenceId = "img1"
        // Simulate a favourite pending an ADD.
        val favEntity = FavouriteEntity(
            imageId = imageReferenceId,
            favouriteId = null,
            pendingOperation = PendingOperation.Add
        )
        coEvery { localDataSource.getFavouriteByImageId(imageReferenceId) } returns favEntity
        coEvery { networkManager.isConnected() } returns false

        repository.deleteCatBreedAsFavourite(imageReferenceId).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
        // Verify that it cancels the pending addition (i.e. deletes the record).
        coVerify { localDataSource.deleteFavourite(imageReferenceId) }
    }
    
}