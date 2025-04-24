package com.example.pincast.data.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "JackalAPI"
    private const val BASE_URL = "https://pinapi.jackalprotocol.com"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRob3JpemVkIjp0cnVlLCJleHAiOjE3NzU2MzcwNzIsIm5hbWUiOiJQaW5DYXN0IiwidXNlciI6ImF1dGgwfDY3YTM4NjhhMGMzY2IxNDhmZjJiYjU3OCJ9.zGPxHtIXiOzX_6dz0VmKOfXyaZ_zZxh8mzyY8Fy42io"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val jackalApiService: JackalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JackalApiService::class.java)
    }

    fun getAuthHeader(): String {
        return "Bearer $API_KEY"
    }
} 