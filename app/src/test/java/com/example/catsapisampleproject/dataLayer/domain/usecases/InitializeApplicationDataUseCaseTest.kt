package com.example.catsapisampleproject.dataLayer.domain.usecases

import app.cash.turbine.test
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepositoryImpl
import com.example.catsapisampleproject.domain.model.AppInitResult
import com.example.catsapisampleproject.domain.useCases.InitializeApplicationDataUseCase
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class InitializeApplicationDataUseCaseTest {

    private lateinit var repository: CatBreedsRepository
    private lateinit var useCase: InitializeApplicationDataUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = InitializeApplicationDataUseCase(repository)
    }

    @Test
    fun `emits loading then success when initialization succeeds`() = runTest {
        coEvery { repository.fetchAndCacheCatBreeds() } returns flowOf(
            CatBreedsRepositoryImpl.InitializationResult.Success
        )

        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(AppInitResult.Success, (result as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits loading then offline mode when offline data is available`() = runTest {
        coEvery { repository.fetchAndCacheCatBreeds() } returns flowOf(
            CatBreedsRepositoryImpl.InitializationResult.OfflineDataAvailable
        )

        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(AppInitResult.OfflineMode, (result as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits loading then failure when initialization returns error`() = runTest {
        coEvery { repository.fetchAndCacheCatBreeds() } returns flowOf(
            CatBreedsRepositoryImpl.InitializationResult.Error("Failed to load data")
        )

        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(
                AppInitResult.Failure("Failed to load data"),
                (result as Resource.Success).data
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error on exception`() = runTest {
        coEvery { repository.fetchAndCacheCatBreeds() } returns flow {
            throw RuntimeException("unexpected")
        }

        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(ErrorType.UnknownError, (result as Resource.Error).error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}