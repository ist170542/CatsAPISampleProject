package com.example.catsapisampleproject.domain.useCases

import app.cash.turbine.test
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedEntity
import com.example.catsapisampleproject.dataLayer.local.entities.CatBreedImageEntity
import com.example.catsapisampleproject.dataLayer.local.entities.FavouriteEntity
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetCatBreedsUseCaseTest {

    private lateinit var repository: CatBreedsRepository
    private lateinit var useCase: GetCatBreedsUseCase

    private val fakeBreeds = listOf(
        CatBreedEntity("1", "Abyssinian", "img1", 10, 15)
    )

    private val fakeImages = listOf(
        CatBreedImageEntity("img1", "1", "https://example.com/image.jpg")
    )

    private val fakeFavourites = MutableStateFlow(
        listOf(FavouriteEntity("img1", "fav1"))
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCatBreedsUseCase(repository)
    }

    @Test
    fun `emits loading and then success with mapped list`() = runTest {
        // Arrange
        coEvery { repository.observeCatBreeds() } returns flowOf(
            Triple(fakeBreeds, fakeImages, fakeFavourites)
        )

        // Act & Assert
        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Success)
            val data = (result as Resource.Success).data
            assertNotNull(data)
            assertEquals("1", data.first().breed.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error on repository failure`() = runTest {
        coEvery { repository.observeCatBreeds() } throws RuntimeException("DB fail")

        useCase().test {
            assertTrue(awaitItem() is Resource.Loading)

            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(ErrorType.UnknownError, (result as Resource.Error).error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}