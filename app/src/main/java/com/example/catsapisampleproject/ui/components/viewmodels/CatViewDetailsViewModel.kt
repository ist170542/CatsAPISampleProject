package com.example.catsapisampleproject.ui.components.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImageAndDetails
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedUseCase
import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringMapper
import com.example.catsapisampleproject.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatDetailsUIState(
    val breed: BreedWithImageAndDetails? = null,
    val isLoading: Boolean = false,
    val error: String = "",
    val favouriteOperationError: String = ""
)

@HiltViewModel
class CatViewDetailsViewModel @Inject constructor(
    savedStateHandle : SavedStateHandle,
    private val getCatBreedDetailsUseCase: GetCatBreedUseCase,
    private val setCatFavouriteUseCase: SetCatFavouriteUseCase,
    private val deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // retrieve breedId from navigation arguments
    private val breedId = savedStateHandle.get<String>("breedId")

    private val _uiState = MutableStateFlow(CatDetailsUIState())
    val uiState = _uiState.asStateFlow()

    init {
        if (breedId != null) {
            loadBreedDetails(breedId)
        } else {
            _uiState.value = CatDetailsUIState(isLoading = false, error = "Breed ID missing")
        }
    }

    private fun loadBreedDetails(breedId: String) {
        getCatBreedDetailsUseCase.invoke(breedId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            breed = result.data,
                            error = StringUtils.EMPTY_STRING
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = StringMapper(context).getErrorString(result.error),
                            isLoading = false
                        )
                    }
                }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }.launchIn(viewModelScope)
    }

    // Handle the favorite button click -> delegate to the appropriate use case
    fun toggleFavourite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val refId = currentState.breed?.image?.image_id ?: return@launch

            if (currentState.breed.isFavourite) {
                // Remove favourite.
                deleteCatFavouriteUseCase.invoke(refId)
                    .onEach { result ->
                        when (result) {
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        favouriteOperationError = StringMapper(context).getErrorString(result.error),
                                        isLoading = false
                                    )
                                }
                            }

                            is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = StringUtils.EMPTY_STRING,
                                        breed = it.breed?.copy(isFavourite = false)
                                    )
                                }
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            } else {
                // Set favourite.
                setCatFavouriteUseCase.invoke(refId)
                    .onEach { result ->
                        when (result) {
                            is Resource.Error -> {
                                _uiState.update {
                                    it.copy(
                                        favouriteOperationError = StringMapper(context).getErrorString(result.error),
                                        isLoading = false
                                    )
                                }
                            }

                            is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                            is Resource.Success -> {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        favouriteOperationError = StringUtils.EMPTY_STRING,
                                        breed = it.breed?.copy(isFavourite = true)
                                    )
                                }
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
        }
    }
}