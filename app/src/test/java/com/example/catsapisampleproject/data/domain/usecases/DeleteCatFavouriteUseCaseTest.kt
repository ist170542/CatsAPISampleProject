package com.example.catsapisampleproject.data.domain.usecases

import app.cash.turbine.test
import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteCatFavouriteUseCaseTest {

    private lateinit var repository: CatBreedsRepository
    private lateinit var useCase: DeleteCatFavouriteUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DeleteCatFavouriteUseCase(repository)
    }

    @Test
    fun `emits loading and success when delete works`() = runTest {
        val imageId = "img123"

        // Only mock success — use case emits loading
        coEvery { repository.deleteCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Success(true)
        )

        useCase(imageId).test {
            // 1. The use case itself emits Loading
            assertEquals(Resource.Loading, awaitItem())

            // 2. Then it emits the result from the repo
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(true, (result as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits success when operation is queued`() = runTest {
        val imageId = "img123"

        coEvery { repository.deleteCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Error(ErrorType.OperationQueued)
        )

        useCase(imageId).test {
            assertEquals(Resource.Loading, awaitItem())

            val result = awaitItem()
            assertTrue("Expected Resource.Success", result is Resource.Success)
            assertEquals(true, (result as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error on failure`() = runTest {
        val imageId = "img123"

        coEvery { repository.deleteCatBreedAsFavourite(imageId) } returns flowOf(
            Resource.Error(ErrorType.UnknownError)
        )

        useCase(imageId).test {
            val first = awaitItem()
            assertEquals(Resource.Loading, first)

            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(ErrorType.UnknownError, (result as Resource.Error).error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
