package com.example.catsapisampleproject.data.domain.usecases

import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import app.cash.turbine.test
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.data.local.entities.PendingOperation
import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetCatFavouriteUseCaseTest {

    private lateinit var repository: CatBreedsRepository
    private lateinit var useCase: SetCatFavouriteUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SetCatFavouriteUseCase(repository)
    }

    @Test
    fun `emits loading and success on happy path`() = runTest {
        val imageId = "img123"
        val entity = FavouriteEntity(
            imageId = imageId,
            favouriteId = "fav1",
            pendingOperation = PendingOperation.None
        )

        // Only Success — use case handles the loading
        coEvery { repository.setCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Success(true)
        )

        useCase(imageId).test {
            val first = awaitItem()
            assertEquals(Resource.Loading, first)

            val result = awaitItem()
            assertTrue(result is Resource.Success)

            val fav = (result as Resource.Success).data
            assertTrue(fav)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits success when operation is queued`() = runTest {
        val imageId = "img123"

        coEvery { repository.setCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Error(ErrorType.OperationQueued)
        )

        useCase(imageId).test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue("Expected Resource.Success", result is Resource.Success)

            val fav = (result as Resource.Success).data
            assertTrue(fav)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error when delete fails with unknown error`() = runTest {
        val imageId = "img789"

        coEvery { repository.setCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Error(ErrorType.UnknownError)
        )

        useCase(imageId).test {
            // First emission: Loading
            assertTrue(awaitItem() is Resource.Loading)

            // Second emission: Error
            val result = awaitItem()
            assertTrue("Expected Resource.Error", result is Resource.Error)
            assertEquals(ErrorType.UnknownError, (result as Resource.Error).error)

            // flow complete
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}