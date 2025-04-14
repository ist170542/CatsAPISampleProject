package com.example.catsapisampleproject.util

import android.content.Context
import com.example.catsapisampleproject.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StringMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getErrorString(error: ErrorType): String {
        return when (error) {
            is ErrorType.NetworkError -> context.getString(R.string.error_network)
            is ErrorType.ServerError -> context.getString(R.string.error_server)
            is ErrorType.DatabaseError -> context.getString(R.string.error_database)
            is ErrorType.NoOfflineData -> context.getString(R.string.error_no_offline_data)
            is ErrorType.MissingBreedId -> context.getString(R.string.error_missing_breed_id)
            is ErrorType.DataNotFound -> context.getString(R.string.error_data_not_found)
            is ErrorType.FavouriteOperationFailed ->
                context.getString(R.string.error_favourite_operation_failed, error.operation)
            else -> context.getString(R.string.error_unknown)
        }
    }
}