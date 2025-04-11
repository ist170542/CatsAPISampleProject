package com.example.catsapisampleproject.ui.components.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.dataLayer.repositories.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FavouriteListUIState(
    val isLoading: Boolean = false,
    val favouriteList: List<BreedWithImage> = emptyList(),
    val error: String = "",
    val averageMinLifeSpan: Double? = 0.0
)

@HiltViewModel
class BreedFavouriteListViewModel @Inject constructor(
    private val getCatBreedsUseCase: GetCatBreedsUseCase,
    private val deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavouriteListUIState())
    val uiState = _uiState.asStateFlow()

    init {
        getFavouriteBreeds()
    }

    private fun getFavouriteBreeds() {
        getCatBreedsUseCase.invoke().onEach { result ->
            when (result) {
                // filter favourite items
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.uiText ?: "An unexpected error occurred",
                        isLoading = false)
                }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    val favouriteList = result.data?.filter { it.isFavourite } ?: emptyList()
                    val averageMinLifeSpan: Double = favouriteList.mapNotNull { it.breed.minLifeSpan }.average()

                    _uiState.update {
                        it.copy(
                            favouriteList = favouriteList,
                            averageMinLifeSpan = averageMinLifeSpan,
                            isLoading = false
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)

    }

    // Handle the favorite button click (remove from favourites in this case)
    fun clickedFavouriteButton(
        imageReferenceId: String?,
        isFavourite: Boolean
    ) {
        imageReferenceId?.let { imageID ->
            handleDeleteFavourite(imageID)
        }

    }

    private fun handleDeleteFavourite(imageReferenceId: String) {
        deleteCatFavouriteUseCase.invoke(imageReferenceId)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        removeFromFavourites(imageReferenceId)
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
    private fun removeFromFavourites(imageId: String) {
        _uiState.update { state ->
            val updatedList = state.favouriteList.filterNot { breedWithImage ->
                breedWithImage.image?.image_id == imageId
            }

            state.copy(
                favouriteList = updatedList,
            )
        }
    }

}
