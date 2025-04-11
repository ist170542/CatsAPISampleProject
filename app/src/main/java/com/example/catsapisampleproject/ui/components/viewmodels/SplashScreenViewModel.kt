package com.example.catsapisampleproject.ui.components.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepositoryImpl
import com.example.catsapisampleproject.domain.useCases.InitializeApplicationDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed class SplashScreenUIState {
    object Loading : SplashScreenUIState()
    data class NavigateToMain(val online : Boolean): SplashScreenUIState()
    data class Error(val message: String) : SplashScreenUIState()
}

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val initializeApplicationDataUseCase: InitializeApplicationDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashScreenUIState>(SplashScreenUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        initializeApplicationData()
    }

    private fun initializeApplicationData() {
        initializeApplicationDataUseCase.invoke().onEach { result ->
            when (result) {
                is CatBreedsRepositoryImpl.InitializationResult.Loading -> {
                    _uiState.value = SplashScreenUIState.Loading
                }

                is CatBreedsRepositoryImpl.InitializationResult.Success -> {
                    _uiState.value = SplashScreenUIState.NavigateToMain(online = true)
                }

                is CatBreedsRepositoryImpl.InitializationResult.OfflineDataAvailable -> {
                    _uiState.value = SplashScreenUIState.NavigateToMain(online = false)
                }

                is CatBreedsRepositoryImpl.InitializationResult.Error -> {
                    _uiState.value = SplashScreenUIState.Error(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }
}

