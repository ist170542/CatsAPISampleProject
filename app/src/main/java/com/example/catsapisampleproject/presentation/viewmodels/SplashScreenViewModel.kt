package com.example.catsapisampleproject.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.domain.model.AppInitResult
import com.example.catsapisampleproject.domain.useCases.InitializeApplicationDataUseCase
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed class SplashScreenUIState {
    object Loading : SplashScreenUIState()
    data class NavigateToMain(val message: String = "") : SplashScreenUIState()
    data class Error(val message: String) : SplashScreenUIState()
}

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val initializeApplicationDataUseCase: InitializeApplicationDataUseCase,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashScreenUIState>(SplashScreenUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        initializeApplicationData()
    }

    fun onRetryClicked() {
        initializeApplicationData()
    }

    private fun initializeApplicationData() {
        initializeApplicationDataUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> _uiState.value = SplashScreenUIState.Loading

                is Resource.Success -> {
                    when (val outcome = result.data) {
                        is AppInitResult.Success -> _uiState.value =
                            SplashScreenUIState.NavigateToMain()

                        is AppInitResult.OfflineMode -> _uiState.value =
                            SplashScreenUIState.NavigateToMain("Using offline data")

                        is AppInitResult.Failure -> _uiState.value =
                            SplashScreenUIState.Error(outcome.message)
                    }
                }

                is Resource.Error -> {
                    _uiState.value =
                        SplashScreenUIState.Error(StringMapper(context).getErrorString(result.error))
                }
            }
        }.launchIn(viewModelScope)
    }
}

