package com.example.catsapisampleproject.ui.components.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
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
import javax.inject.Inject

data class BreedListUIState(
    val isLoading: Boolean = false,
    val fullBreedList: List<BreedWithImage> = emptyList(),
    val filteredBreedList: List<BreedWithImage> = emptyList(),
    val error: String = "",
    val searchText: String = ""
)

/**
 * //todo rewrite comment
 *  Still not very reactive behaviour. Will try to fetch data initially, then observe DB (since when
 * a post favourite call occurs, its stored locally), so that those changes to spread the user made
 * changes to the multiple screens.
 *
 */
@HiltViewModel
class BreedListViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val setCatFavouriteUseCase: SetCatFavouriteUseCase,
    private val deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreedListUIState())
    val uiState = _uiState.asStateFlow()

    init {
        getCatBreeds()
    }

    // Fetch cat breeds and update the UI
    private fun getCatBreeds() {
        getCatBreedsUseCase.invoke().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            fullBreedList = result.data ?: emptyList(),
                            filteredBreedList = filterBreeds(
                                result.data ?: emptyList(),
                                it.searchText
                            ),
                            isLoading = false,
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
    fun clickedFavouriteButton(
        imageReferenceId: String?,
        isCurrentlyFavourite: Boolean
    ) {
        imageReferenceId?.let { imageID ->

            if (isCurrentlyFavourite) {
                handleDeleteFavourite(imageID)
            } else {
                handleSetFavourite(imageID)
            }

        }

    }

    private fun handleSetFavourite(imageReferenceId: String) {
        setCatFavouriteUseCase.invoke(imageReferenceId)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = StringUtils.EMPTY_STRING
                            )
                        }
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = StringMapper(context).getErrorString(result.error)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun handleDeleteFavourite(imageReferenceId: String) {
        deleteCatFavouriteUseCase.invoke(imageReferenceId)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
//                        updateBreedFavouriteStatus(imageReferenceId, false)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = StringUtils.EMPTY_STRING
                            )
                        }
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = StringMapper(context).getErrorString(result.error)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // This function updates the search text and the filtered breed list based on the search text
    fun updateSearchText(newText: String) {
        _uiState.update { state ->
            state.copy(
                searchText = newText,
                filteredBreedList = filterBreeds(state.fullBreedList, newText)
            )
        }
    }

    private fun filterBreeds(breeds: List<BreedWithImage>, query: String): List<BreedWithImage> {
        return breeds.filter { it.breed.name.startsWith(query, ignoreCase = true) }
    }

}