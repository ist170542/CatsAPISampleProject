package com.example.catsapisampleproject.domain.useCases

import app.cash.turbine.test
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedDetailsEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.local.entities.PendingOperation
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
        val breed = CatBreedEntity("1", "Abyssinian", "img123", 10, 15)
        val image = CatBreedImageEntity("img123", "1", "http://example.com/cat.jpg")
        val details = CatBreedDetailsEntity("1", "Friendly", "Asia", origin = "Asia")

        val favourite = FavouriteEntity("img123", "fav1", PendingOperation.None)

        coEvery { repository.getCatBreedDetailsById("1") } returns flowOf(Triple(breed, image, details))
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
        val breed = CatBreedEntity("1", "Abyssinian", null, 10, 15)
        val details = CatBreedDetailsEntity("1", "Friendly", "Asia", origin = "Asia")

        coEvery { repository.getCatBreedDetailsById("1") } returns flowOf(Triple(breed, null, details))

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
        coEvery { repository.getCatBreedDetailsById("1") } throws RuntimeException("DB failure")

        useCase("1").test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(ErrorType.DatabaseError, (result as Resource.Error).error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}