package com.example.catsapisampleproject.data.network

import kotlinx.coroutines.flow.Flow

interface NetworkManager {
    val isOnline: Flow<Boolean>
    suspend fun isConnected(): Boolean
}
