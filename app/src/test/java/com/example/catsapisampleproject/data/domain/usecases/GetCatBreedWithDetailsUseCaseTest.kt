package com.example.catsapisampleproject.domain.useCases

import app.cash.turbine.test
import com.example.catsapisampleproject.data.local.entities.FavouriteEntity
import com.example.catsapisampleproject.data.local.entities.PendingOperation
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedDetails
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.repositories.CatBreedsRepository
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCatBreedWithDetailsUseCaseTest {

    private lateinit var repository: CatBreedsRepository
    private lateinit var useCase: GetCatBreedWithDetailsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCatBreedWithDetailsUseCase(repository)
    }

    @Test
    fun `returns success with favourite when imageId is not null`() = runTest {
        val breed = CatBreed("1", "Abyssinian", "img123", 10, 15)
        val image = CatBreedImage("img123", "1", "http://example.com/cat.jpg")
        val details = CatBreedDetails("1", "Friendly", "Asia", origin = "Asia")

        val favourite = FavouriteEntity("img123", "fav1", PendingOperation.None)

        coEvery { repository.getCatBreedDetailsByIdWithFavourite("1") } returns flowOf(
            BreedWithImageAndDetails(breed, image, details))
        every { repository.observeFavouriteByImageId("img123") } returns flowOf(favourite)

        useCase("1").test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertTrue((result as Resource.Success).data.isFavourite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns success without favourite when imageId is null`() = runTest {
        val breed = CatBreed("1", "Abyssinian", null, 10, 15)
        val details = CatBreedDetails("1", "Friendly", "Asia", origin = "Asia")

        coEvery { repository.getCatBreedDetailsByIdWithFavourite("1") } returns flowOf(BreedWithImageAndDetails(breed, null, details))

        useCase("1").test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertFalse((result as Resource.Success).data.isFavourite)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns error on exception`() = runTest {
        coEvery { repository.getCatBreedDetailsByIdWithFavourite("1") } throws RuntimeException("DB failure")

        useCase("1").test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(ErrorType.DatabaseError, (result as Resource.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}