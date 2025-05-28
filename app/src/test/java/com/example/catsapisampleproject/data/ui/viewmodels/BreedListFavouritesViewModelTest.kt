package com.example.catsapisampleproject.data.ui.viewmodels

import android.content.Context
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
import com.example.catsapisampleproject.presentation.viewmodels.BreedFavouriteListViewModel
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BreedFavouriteListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var getCatBreedsUseCase: GetCatBreedsUseCase

    @MockK
    private lateinit var deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase

    @MockK
    private lateinit var context: Context

    private lateinit var viewModel: BreedFavouriteListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(testDispatcher)

        // Initialize the viewModel with mocks
//        viewModel = BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)
    }

    @Test
    fun `initial state has loading true`() = runTest {
        // Given: The flow of breeds is loading
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Loading)

        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: Advance until idle to trigger any emissions
        advanceUntilIdle()

        // Then: The UI state should indicate loading
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successfully loads favourite breeds`() = runTest {
        // Given: A list of breeds with some favourites
        val mockBreeds = listOf(
            createMockBreedWithImage(isFavourite = true, minLifeSpan = 10),
            createMockBreedWithImage(isFavourite = false, minLifeSpan = 15),
            createMockBreedWithImage(isFavourite = true, minLifeSpan = 20)
        )

        // Mock the use case to return the list of breeds
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(mockBreeds))

        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: Advance until idle to trigger the emission
        advanceUntilIdle()

        // Then: The UI state should contain the favourites and correct lifespan average
        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.favouriteList.size) // Expecting 2 favourites
        assertEquals(15.0, uiState.averageMinLifeSpan!!, 0.1) // Expecting average lifespan of 15
    }

    @Test
    fun `handles delete favourite successfully`() = runTest {
        // Given: A breed that is marked as favourite
        val mockBreed = createMockBreedWithImage(isFavourite = true, imageId = "img123")

        // Mocking the calls for the use cases
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(listOf(mockBreed)))
        coEvery { deleteCatFavouriteUseCase.invoke(any()) } returns flowOf(Resource.Success(true))

        // Initialize ViewModel
        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: A favourite button is clicked to delete
        advanceUntilIdle() // First, load the breeds
        viewModel.clickedFavouriteButton("img123", true) // Delete the favourite
        advanceUntilIdle() // Wait for the action to complete

        // Then: Check that the UI is not loading and no errors occurred
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.error.isEmpty())
    }

    @Test
    fun `handles delete favourite error`() = runTest {
        // Given: A breed is marked as favourite and there's an error deleting it
        val mockBreed = createMockBreedWithImage(isFavourite = true, imageId = "img123")
        val error = ErrorType.FavouriteOperationFailed("Delete")
        val errorMessage = "Deletion failed"

        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(listOf(mockBreed)))
        coEvery { deleteCatFavouriteUseCase.invoke(any()) } returns flowOf(Resource.Error(error))
        coEvery { context.getString(any()) } returns errorMessage
        coEvery { context.getString(any(), any()) } returns "Deletion failed"

        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: A favourite button is clicked to delete
        advanceUntilIdle() // First, load the breeds
        viewModel.clickedFavouriteButton("img123", true) // Try to delete the favourite
        advanceUntilIdle() // Wait for the action to complete

        // Then: Check the error message and loading state
        assertEquals(errorMessage, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `ignores click when imageReferenceId is null`() = runTest {
        // Given: The use case returns an empty list of breeds
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(emptyList()))

        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: A null imageReferenceId is passed to the favourite button click
        advanceUntilIdle() // First, load the breeds (empty list)
        viewModel.clickedFavouriteButton(null, true) // Ignore click
        advanceUntilIdle() // Wait for the action to complete

        // Then: The delete operation should not be invoked
        coVerify(exactly = 0) { deleteCatFavouriteUseCase.invoke(any()) }
        assertTrue(viewModel.uiState.value.error.isEmpty())
    }

    @Test
    fun `handles error when loading breeds`() = runTest {
        // Given: A network error occurs when loading breeds
        val error = ErrorType.NetworkError
        val errorMessage = "Network error"
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Error(error))
        coEvery { context.getString(any()) } returns errorMessage

        viewModel =
            BreedFavouriteListViewModel(getCatBreedsUseCase, deleteCatFavouriteUseCase, context)

        // When: The breeds are loaded
        advanceUntilIdle()

        // Then: The error message should be displayed, and the list should be empty
        assertEquals(errorMessage, viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.favouriteList.isEmpty())
    }

    private fun createMockBreedWithImage(
        isFavourite: Boolean,
        minLifeSpan: Int? = null,
        imageId: String = "img1"
    ): BreedWithImage {
        val breed = CatBreed(
            id = "breed1",
            name = "Test Breed",
            referenceImageId = imageId,
            minLifeSpan = minLifeSpan,
            maxLifeSpan = null
        )

        val image = CatBreedImage(
            imageId = imageId,
            url = "http://example.com/$imageId.jpg",
            breedId = "breed1"
        )

        return BreedWithImage(
            breed = breed,
            image = image,
            isFavourite = isFavourite
        )
    }
}