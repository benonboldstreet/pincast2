package com.example.pincast.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.pincast.data.PincastRepository
import com.example.pincast.data.remote.PincastApi
import com.example.pincast.ui.viewmodels.AuthViewModel
import com.example.pincast.ui.viewmodels.UploadViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Create manual instances without Hilt
    private val api = Retrofit.Builder()
        .baseUrl("https://api.example.com/") // Replace with your actual base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PincastApi::class.java)
    
    private val repository = PincastRepository(api)
    
    // TODO: In a real app, these would be injected with Hilt
    val authViewModel = AuthViewModel(application)
    val uploadViewModel = UploadViewModel(application)
    
    // We're not creating galleryViewModel here as it depends on repository
    // val galleryViewModel = GalleryViewModel(repository)
} 