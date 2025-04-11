package com.example.catsapisampleproject.domain.useCases

/*
    Container/wrapper to group the use cases and simplify DI and organization
 */
data class CatBreedsUseCases(
    val getCatBreedUseCase: GetCatBreedUseCase,
    val getBreedsUseCase: GetCatBreedsUseCase,
    val setCatFavouriteUseCase: SetCatFavouriteUseCase,
    val deleteCatFavouriteUseCase: DeleteCatFavouriteUseCase
)
