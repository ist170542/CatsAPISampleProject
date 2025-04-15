package com.example.catsapisampleproject.domain.model

sealed class AppInitResult {
    object Success : AppInitResult()
    object OfflineMode : AppInitResult()
    data class Failure(val message: String) : AppInitResult()
}