package com.example.catsapisampleproject.domain.useCases

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.catsapisampleproject.dataLayer.repositories.CatBreedsRepository
import com.example.catsapisampleproject.domain.mappers.BreedWithImageMapper
import com.example.catsapisampleproject.domain.model.BreedWithImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCatImagesPaginatedUseCase @Inject constructor(
    private val repository: CatBreedsRepository
) {
    operator fun invoke(scope: CoroutineScope): Flow<PagingData<BreedWithImage>> {
        return repository.observeCatImagesPaginated()
            .map { pagingData ->
                pagingData.map { dto ->
                    BreedWithImageMapper.fromDto(dto)
                }
            }
            .cachedIn(scope)
    }
}