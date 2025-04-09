package com.example.catsapisampleproject.util

/**
 * class to send data with the correct state (success, error, loading)
 */
sealed class Resource<T>(val data: T? = null, val uiText: String? = null) {
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(uiText: String, data: T? = null): Resource<T>(data, uiText)
    class Loading<T>(data: T? = null): Resource<T>(data)
}