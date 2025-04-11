package com.example.catsapisampleproject.dataLayer.network

import kotlinx.coroutines.flow.Flow

interface NetworkManager {
    val isOnline: Flow<Boolean>
    suspend fun isConnected(): Boolean
}
