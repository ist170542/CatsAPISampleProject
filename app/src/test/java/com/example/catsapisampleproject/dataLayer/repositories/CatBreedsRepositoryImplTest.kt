package com.example.catsapisampleproject.dataLayer.repositories

import app.cash.turbine.test
import com.example.catsapisampleproject.dataLayer.dto.responses.BreedDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.FavouriteDTO
import com.example.catsapisampleproject.dataLayer.dto.responses.ImageDTO
import com.example.catsapisampleproject.dataLayer.local.LocalDataSource
import com.example.catsapisampleproject.dataLayer.local.entities.*
import com.example.catsapisampleproject.dataLayer.network.NetworkManager
import com.example.catsapisampleproject.dataLayer.remote.RemoteDataSource
import com.example.catsapisampleproject.domain.model.InitializationResult
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
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
    fun `fetchAndCacheCatBreeds returns Success when network is available`() = runTest {
        coEvery { networkManager.isConnected() } returns true

        val sampleBreedDTO = BreedDTO("1", "Abyssinian", "Desc", "Active", "Egypt", "img1", "10 - 15")
        val sampleFavouriteDTO = FavouriteDTO("fav1", "img1")
        val sampleImageDTO = ImageDTO("http://test.com/cat.jpg", "img1")

        coEvery { remoteDataSource.getCatBreeds() } returns listOf(sampleBreedDTO)
        coEvery { remoteDataSource.getFavourites() } returns listOf(sampleFavouriteDTO)
        coEvery { remoteDataSource.getCatBreedImageByReferenceImageId("img1") } returns sampleImageDTO

        coEvery { localDataSource.getFavouriteCatBreeds() } returns emptyList()
        coEvery { localDataSource.insertCatBreeds(any()) } just Runs
        coEvery { localDataSource.insertCatBreedsDetails(any()) } just Runs
        coEvery { localDataSource.insertCatBreedImages(any()) } just Runs
        coEvery { localDataSource.deleteAllFavourites() } just Runs
        coEvery { localDataSource.insertFavourites(any()) } just Runs

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(InitializationResult.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds returns Error when no offline data available`() = runTest {
        coEvery { networkManager.isConnected() } returns false
        coEvery { localDataSource.getCatBreeds() } returns emptyList()
        coEvery { localDataSource.getCatBreedImages() } returns emptyList()
        coEvery { localDataSource.getFavouriteCatBreeds() } returns emptyList()

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(InitializationResult.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds OfflineDataAvailable when offline data is available`() = runTest {
        coEvery { networkManager.isConnected() } returns false

        val localBreed = CatBreedEntity("1", "Abyssinian", "img1", 10, 15)
        val localImage = CatBreedImageEntity("img1", "1", "http://example.com/cat.jpg")
        val localFavourite = FavouriteEntity("img1", "fav1", PendingOperation.None)

        coEvery { localDataSource.getCatBreeds() } returns listOf(localBreed)
        coEvery { localDataSource.getCatBreedImages() } returns listOf(localImage)
        coEvery { localDataSource.getFavouriteCatBreeds() } returns listOf(localFavourite)

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isEqualTo(InitializationResult.OfflineDataAvailable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchAndCacheCatBreeds handles exceptions and falls back to offline`() = runTest {
        coEvery { networkManager.isConnected() } returns true
        coEvery { remoteDataSource.getCatBreeds() } throws Exception("Network error")
        coEvery { localDataSource.getCatBreeds() } returns emptyList()
        coEvery { localDataSource.getCatBreedImages() } returns emptyList()
        coEvery { localDataSource.getFavouriteCatBreeds() } returns emptyList()

        repository.fetchAndCacheCatBreeds().test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(InitializationResult.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

}
