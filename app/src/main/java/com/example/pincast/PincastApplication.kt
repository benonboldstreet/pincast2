package com.example.pincast

import android.app.Application
import com.example.pincast.data.PincastRepository
import com.example.pincast.data.remote.PincastApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PincastApplication : Application() {
    
    // Create repository instance for use throughout the app
    val repository: PincastRepository by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Replace with your actual API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val api = retrofit.create(PincastApi::class.java)
        PincastRepository(api)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components here
    }
} 