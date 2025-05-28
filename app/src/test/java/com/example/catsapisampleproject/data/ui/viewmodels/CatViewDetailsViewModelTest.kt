package com.example.catsapisampleproject.data.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.catsapisampleproject.domain.model.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.model.CatBreed
import com.example.catsapisampleproject.domain.model.CatBreedImage
import com.example.catsapisampleproject.domain.model.Favourite
import com.example.catsapisampleproject.domain.model.FavouriteStatus
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedWithDetailsUseCase
import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import com.example.catsapisampleproject.presentation.components.viewmodels.CatViewDetailsViewModel
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringUtils
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CatViewDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @get:Rule
    val mockkRule = MockKRule(this)

    private val mockGetCatBreedDetailsUseCase = mockk<GetCatBreedWithDetailsUseCase>()
    private val mockSetCatFavouriteUseCase = mockk<SetCatFavouriteUseCase>()
    private val mockDeleteCatFavouriteUseCase = mockk<DeleteCatFavouriteUseCase>()
    private val mockContext = mockk<android.content.Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clearMocks(mockGetCatBreedDetailsUseCase, mockSetCatFavouriteUseCase, mockDeleteCatFavouriteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockBreedWithDetails(
        isFavourite: Boolean,
        imageId: String? = "img123"
    ): BreedWithImageAndDetails {
        val mockBreed = mockk<CatBreed>(relaxed = true)
        val mockImage = imageId?.let {
            CatBreedImage(imageId = it, url = "http://test.com/$it.jpg", breedId = "breed1")
        }
        return BreedWithImageAndDetails(
            breed = mockBreed,
            image = mockImage,
            details = null,
            isFavourite = isFavourite
        )
    }

    @Test
    fun `if breedId is missing, ui state should show error`() = testScope.runTest {
        every { mockContext.getString(any()) } returns "Breed ID missing"

        val viewModel = CatViewDetailsViewModel(
            SavedStateHandle(),
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Breed ID missing", state.error)
            assertTrue(!state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load breed details successfully`() = testScope.runTest {
        val breedDetails = createMockBreedWithDetails(isFavourite = false)
        val detailsFlow: Flow<Resource<BreedWithImageAndDetails>> = flow {
            emit(Resource.Loading)
            emit(Resource.Success(breedDetails))
        }

        val savedStateHandle = SavedStateHandle(mapOf("breedId" to "123"))
        every { mockGetCatBreedDetailsUseCase.invoke("123") } returns detailsFlow

        val viewModel = CatViewDetailsViewModel(
            savedStateHandle,
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        viewModel.uiState.test {
            awaitItem() // Initial empty state
            awaitItem() // Loading state
            val finalState = awaitItem() // Success state
            assertEquals(breedDetails, finalState.breed)
            assertTrue(!finalState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load breed details with error`() = testScope.runTest {
        val errorMsg = "Failed to load details"
        every { mockContext.getString(any()) } returns errorMsg

        val detailsFlow = flow {
            emit(Resource.Loading)
            emit(Resource.Error(ErrorType.DatabaseError))
        }

        val savedStateHandle = SavedStateHandle(mapOf("breedId" to "123"))
        every { mockGetCatBreedDetailsUseCase.invoke("123") } returns detailsFlow

        val viewModel = CatViewDetailsViewModel(
            savedStateHandle,
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        viewModel.uiState.test {
            awaitItem() // Initial state
            awaitItem() // Loading state
            val errorState = awaitItem() // Error state
            assertEquals(errorMsg, errorState.error)
            assertTrue(!errorState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favourite when already favourite triggers deletion and updates state`() = testScope.runTest {
        val breedDetails = createMockBreedWithDetails(isFavourite = true)
        val detailsFlow = flowOf(Resource.Success(breedDetails))
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to "123"))
        every { mockGetCatBreedDetailsUseCase.invoke("123") } returns detailsFlow
        every { mockDeleteCatFavouriteUseCase.invoke("img123") } returns flowOf(Resource.Success(true))

        val viewModel = CatViewDetailsViewModel(
            savedStateHandle,
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        advanceUntilIdle()
        viewModel.toggleFavourite()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.breed?.isFavourite == false)
    }

    @Test
    fun `toggle favourite when not favourite triggers setting and updates state`() = testScope.runTest {
        val breedDetails = createMockBreedWithDetails(isFavourite = false)
        val detailsFlow = flowOf(Resource.Success(breedDetails))
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to "123"))
        every { mockGetCatBreedDetailsUseCase.invoke("123") } returns detailsFlow

        val dummyFavourite = Favourite("img123", "fav123", FavouriteStatus.Synced)
        every { mockSetCatFavouriteUseCase.invoke("img123") } returns flowOf(Resource.Success(true))

        val viewModel = CatViewDetailsViewModel(
            savedStateHandle,
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        advanceUntilIdle()
        viewModel.toggleFavourite()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.breed?.isFavourite == true)
    }

    @Test
    fun `toggle favourite does nothing if image reference is missing`() = testScope.runTest {
        val breedDetails = createMockBreedWithDetails(isFavourite = false, imageId = null)
        val detailsFlow = flowOf(Resource.Success(breedDetails))
        val savedStateHandle = SavedStateHandle(mapOf("breedId" to "123"))
        every { mockGetCatBreedDetailsUseCase.invoke("123") } returns detailsFlow

        val viewModel = CatViewDetailsViewModel(
            savedStateHandle,
            mockGetCatBreedDetailsUseCase,
            mockSetCatFavouriteUseCase,
            mockDeleteCatFavouriteUseCase,
            mockContext
        )

        advanceUntilIdle()
        viewModel.toggleFavourite()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StringUtils.EMPTY_STRING, state.favouriteOperationError)
    }
}
