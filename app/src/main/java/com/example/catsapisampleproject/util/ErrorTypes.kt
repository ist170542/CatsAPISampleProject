package com.example.catsapisampleproject.util

sealed class ErrorType(val code: String) {
    // General errors
    object NetworkError : ErrorType("error_network")
    object ServerError : ErrorType("error_server")
    object DatabaseError : ErrorType("error_database")
    object UnknownError : ErrorType("error_unknown")
    object OperationQueued : ErrorType("error_operation_queued")

    // Specific business errors
    object NoOfflineData : ErrorType("error_no_offline_data")
    object DataNotFound : ErrorType("data_not_found")
    object MissingBreedId : ErrorType("error_missing_breed_id")
    data class FavouriteOperationFailed(val operation: String) :
        ErrorType("error_favourite_operation_failed")

}