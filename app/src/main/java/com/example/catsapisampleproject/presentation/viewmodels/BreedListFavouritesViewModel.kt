package com.example.catsapisampleproject.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catsapisampleproject.data.mappers.CatBreedsStatsUtilMapper.computeAverageMinLifeSpan
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.DeleteCatFavouriteUseCase
import com.example.catsapisampleproject.domain.useCases.GetCatBreedsUseCase
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
    @ApplicationContext private val context: Context,
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
                    it.copy(
                        error = StringMapper(context = context).getErrorString(result.error),
                        isLoading = false
                    )
                }

                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    // filter favourite items
                    val favouriteList = result.data.filter { it.isFavourite } ?: emptyList()
                    val averageMinLifeSpan: Double? = computeAverageMinLifeSpan(favouriteList)

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
                            error = StringMapper(context = context)
                                .getErrorString(result.error)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}
