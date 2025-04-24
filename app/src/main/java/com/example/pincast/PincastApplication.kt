package com.example.pincast

import android.app.Application
import com.example.pincast.data.PincastRepository
import com.example.pincast.data.cache.CacheRepository
import com.example.pincast.data.remote.PincastApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PincastApplication : Application() {
    
    // Application scope for background tasks
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    
    // Create repository instances for use throughout the app
    val repository: PincastRepository by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Replace with your actual API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val api = retrofit.create(PincastApi::class.java)
        PincastRepository(api)
    }
    
    // Create cache repository
    val cacheRepository: CacheRepository by lazy {
        CacheRepository(applicationContext)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize app-wide components
        initializeCache()
    }
    
    private fun initializeCache() {
        applicationScope.launch {
            // Prune cache to a reasonable size on startup
            cacheRepository.pruneCache(100) // 100MB max cache size
        }
    }
} 