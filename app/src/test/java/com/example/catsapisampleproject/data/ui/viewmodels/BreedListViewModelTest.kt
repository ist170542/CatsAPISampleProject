package com.example.catsapisampleproject.data.ui.viewmodels

import android.content.Context
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import com.example.catsapisampleproject.presentation.viewmodels.BreedListViewModel
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class BreedListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // No @Before setup of viewModel – we'll create the viewModel in each test after stubbing.
    private val getCatBreedsUseCase = mockk<GetCatBreedsUseCase>()
    private val setCatFavouriteUseCase = mockk<SetCatFavouriteUseCase>()
    private val deleteCatFavouriteUseCase = mockk<DeleteCatFavouriteUseCase>()
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `search filters breeds correctly`() = runTest {
        // Create two BreedWithImage instances with different names
        val breed1 = mockk<BreedWithImage>(relaxed = true) {
            // Using startsWith filter inside ViewModel (prefix match)
            every { breed.name } returns "Siamese"
        }
        val breed2 = mockk<BreedWithImage>(relaxed = true) {
            every { breed.name } returns "Persian"
        }
        val breeds = listOf(breed1, breed2)

        // Stub getCatBreedsUseCase to return these breeds
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(breeds))

        // Then instantiate the viewModel so its init block calls getCatBreeds()
        val viewModel = BreedListViewModel(
            getCatBreedsUseCase,
            setCatFavouriteUseCase,
            deleteCatFavouriteUseCase,
            context
        )

        // Now update search text to "sia" (we use startsWith in the filter)
        viewModel.updateSearchText("sia")

        // Advance time so the UI state gets updated
        advanceUntilIdle()

        // Assert that filtering only keeps "Siamese"
        assertEquals(1, viewModel.uiState.value.filteredBreedList.size)
        assertEquals("Siamese", viewModel.uiState.value.filteredBreedList.first().breed.name)
    }

    @Test
    fun `toggle favourite updates state`() = testScope.runTest {
        // Given: use cases return dummy responses
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(emptyList()))
        coEvery { setCatFavouriteUseCase.invoke(any()) } returns flowOf(
            Resource.Loading,
            Resource.Success(true)
        )

        val viewModel = BreedListViewModel(
            getCatBreedsUseCase,
            setCatFavouriteUseCase,
            deleteCatFavouriteUseCase,
            context = context
        )

        viewModel.clickedFavouriteButton("img123", isCurrentlyFavourite = false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.error.isEmpty())
    }

    @Test
    fun `set favourite emits error updates UI state`() = runTest {
        // Stub getCatBreedsUseCase to return an empty list
        coEvery { getCatBreedsUseCase.invoke() } returns flowOf(Resource.Success(emptyList()))
        // Simulate an error from setCatFavouriteUseCase
        coEvery { setCatFavouriteUseCase.invoke(any()) } returns flowOf(
            Resource.Error(ErrorType.UnknownError)
        )

        // For error string mapping, stub the context's getString call.
        coEvery { context.getString(any()) } returns "Something went wrong"

        val viewModel = BreedListViewModel(
            getCatBreedsUseCase,
            setCatFavouriteUseCase,
            deleteCatFavouriteUseCase,
            context
        )

        viewModel.clickedFavouriteButton("img123", false)

        advanceUntilIdle()

        // Assert that isLoading is false after processing error and the error message is updated
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Something went wrong", viewModel.uiState.value.error)
    }
}