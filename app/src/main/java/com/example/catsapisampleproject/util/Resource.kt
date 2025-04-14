package com.example.catsapisampleproject.util

/**
 * class to send data with the correct state (success (offline flag to inform the user), error
 * , loading)
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: ErrorType) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}