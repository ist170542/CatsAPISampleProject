package com.example.catsapisampleproject.ui.components.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.catsapisampleproject.domain.model.BreedWithImage
import com.example.catsapisampleproject.domain.useCases.GetCatImagesPaginatedUseCase
import com.example.catsapisampleproject.util.Resource
import com.example.catsapisampleproject.util.StringMapper
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.catsapisampleproject.dataLayer.local.entities.isEffectiveFavourite
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatImagesUIState(
    val imagesFlow: Flow<PagingData<BreedWithImage>> = emptyFlow(),
    val isLoading: Boolean = false,
    val error: String = ""
)

@HiltViewModel
class ImagesPaginatedSubScreenViewModel @Inject constructor(
    getCatImagesPaginatedUseCase: GetCatImagesPaginatedUseCase,
    val repository: CatBreedsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CatImagesUIState(
            isLoading = true
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        val favouritesFlow = repository.observeFavouriteCatBreeds()
            .map { list -> list.filter { it.isEffectiveFavourite() }.map { it.imageId }.toSet() }

        val combinedPagingFlow = combine(
            getCatImagesPaginatedUseCase(viewModelScope),
            favouritesFlow
        ) { pagingImages, favourites ->
            pagingImages.map { image ->
                image.copy(isFavourite = favourites.contains(image.image?.imageId))
            }
        }.cachedIn(viewModelScope)

        _uiState.value = _uiState.value.copy(
            imagesFlow = combinedPagingFlow,
            isLoading = false
        )
    }

    fun toggleFavourite(imageId: String?, isCurrentlyFavourite: Boolean) {
        if (imageId == null) return

        val flow = if (isCurrentlyFavourite) {
            repository.deleteCatBreedAsFavourite(imageId)
        } else {
            repository.setCatBreedAsFavourite(imageId)
        }

        viewModelScope.launch {
            flow
                .onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Unexpected error.")
                    }
                }
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.update { it.copy(isLoading = false, error = "") }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = StringMapper(context).getErrorString(result.error)
                                )
                            }
                        }

                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
        }
    }
}

