package com.example.catsapisampleproject.ui.viewmodels

import android.content.Context
import app.cash.turbine.test
import com.example.catsapisampleproject.domain.model.AppInitResult
import com.example.catsapisampleproject.domain.useCases.InitializeApplicationDataUseCase
import com.example.catsapisampleproject.ui.components.viewmodels.SplashScreenUIState
import com.example.catsapisampleproject.ui.components.viewmodels.SplashScreenViewModel
import com.example.catsapisampleproject.util.ErrorType
import com.example.catsapisampleproject.util.Resource
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SplashScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @get:Rule
    val mockkRule = MockKRule(this)

    private val mockUseCase = mockk<InitializeApplicationDataUseCase>()
    private val mockContext = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        clearMocks(mockUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = testScope.runTest {
        every { mockUseCase.invoke() } returns emptyFlow()
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when use case returns AppInitResult Success, should navigate to main`() = testScope.runTest {
        every { mockUseCase.invoke() } returns flowOf(
            Resource.Loading,
            Resource.Success(AppInitResult.Success)
        )
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(SplashScreenUIState.NavigateToMain(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when use case returns OfflineMode, should navigate with message`() = testScope.runTest {
        every { mockUseCase.invoke() } returns flowOf(
            Resource.Loading,
            Resource.Success(AppInitResult.OfflineMode)
        )
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(
                SplashScreenUIState.NavigateToMain(message = "Using offline data"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when use case returns Failure, should show error message`() = testScope.runTest {
        val errorMessage = "Connection failed"
        every { mockUseCase.invoke() } returns flowOf(
            Resource.Loading,
            Resource.Success(AppInitResult.Failure(errorMessage))
        )
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(SplashScreenUIState.Error(errorMessage), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when retry clicked, should re-initialize`() = testScope.runTest {
        every { mockUseCase.invoke() } returnsMany listOf(
            flowOf(Resource.Loading, Resource.Success(AppInitResult.Failure("Initial error"))),
            flowOf(Resource.Loading, Resource.Success(AppInitResult.Success))
        )
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(SplashScreenUIState.Error("Initial error"), awaitItem())

            viewModel.onRetryClicked()

            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(SplashScreenUIState.NavigateToMain(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should handle Resource_Error fallback`() = testScope.runTest {
        every { mockUseCase.invoke() } returns flowOf(
            Resource.Loading,
            Resource.Error(ErrorType.UnknownError)
        )
        val viewModel = SplashScreenViewModel(mockUseCase, mockContext)

        viewModel.uiState.test {
            assertEquals(SplashScreenUIState.Loading, awaitItem())
            assertEquals(SplashScreenUIState.Error("Something went wrong"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
