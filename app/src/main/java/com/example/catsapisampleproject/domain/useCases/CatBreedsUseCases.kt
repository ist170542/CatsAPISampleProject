package com.example.catsapisampleproject.domain.useCases

/*
    Container/wrapper to group the use cases and simplify DI and organization
 */
data class CatBreedsUseCases(
    val getBreedsUseCase: GetCatBreedsUseCase
)
