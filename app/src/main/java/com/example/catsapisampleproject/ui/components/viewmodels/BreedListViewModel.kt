package com.example.catsapisampleproject.ui.components.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
import com.example.catsapisampleproject.domain.useCases.SetCatFavouriteUseCase
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * //todo
 *  Still not very reactive behaviour. Will try to fetch data initially, then observe DB (since when
 * a post favourite call occurs, its stored locally), so that those changes to spread the user made
 * changes to the multiple screens.
 *
 */
@HiltViewModel
class BreedListViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val setCatFavouriteUseCase: SetCatFavouriteUseCase,
    private val deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase
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
                            filteredBreedList = result.data ?: emptyList(),
                            isLoading = false,
                            error = StringUtils.EMPTY_STRING
                        )
                    }
                }

                //todo maybe add another Resource for Error BUT offline data provided
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.uiText ?: "An unexpected error occurred",
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
                        updateBreedFavouriteStatus(imageReferenceId, true)
                        _uiState.update { it.copy(isLoading = false, error = StringUtils.EMPTY_STRING) }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.uiText ?: "Failed to set favourite")
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
                        updateBreedFavouriteStatus(imageReferenceId, false)
                        _uiState.update { it.copy(isLoading = false, error = StringUtils.EMPTY_STRING) }
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.uiText ?: "Failed to delete favourite")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Updates the favourite status in both full and filtered lists.
    private fun updateBreedFavouriteStatus(imageId: String, isFavourite: Boolean) {
        _uiState.update { state ->
            val updateList: (List<BreedWithImage>) -> List<BreedWithImage> = { list ->
                list.map { breedWithImage ->
                    if (breedWithImage.image?.image_id == imageId) {
                        breedWithImage.copy(isFavourite = isFavourite)
                    } else breedWithImage
                }
            }

            state.copy(
                fullBreedList = updateList(state.fullBreedList),
                filteredBreedList = updateList(state.filteredBreedList)
            )
        }
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
        return breeds.filter { it.breed.name.contains(query, ignoreCase = true) }
    }

}