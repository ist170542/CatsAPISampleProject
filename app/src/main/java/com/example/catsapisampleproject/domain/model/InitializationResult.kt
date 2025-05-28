package com.example.catsapisampleproject.domain.model

sealed class InitializationResult {
    data object Loading : InitializationResult()
    data object Success : InitializationResult()
    data object OfflineDataAvailable : InitializationResult()
    data class Error(val message: String) : InitializationResult()
}