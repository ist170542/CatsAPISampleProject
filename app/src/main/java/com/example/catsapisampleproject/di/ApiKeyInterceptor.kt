package com.example.catsapisampleproject.di

import com.example.catsapisampleproject.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestWithAPIKey = original.newBuilder()
            .addHeader("x-api-key", BuildConfig.CAT_API_KEY)
            .build()

        return chain.proceed(requestWithAPIKey)
    }
}