package com.example.pincast.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pincast.data.models.Image
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class UploadViewModel(application: Application) : AndroidViewModel(application) {
    
    // We won't use repository for this simplified version
    
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState
    
    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            
            try {
                // Simulate a successful upload after a delay
                kotlinx.coroutines.delay(1500)
                
                // Create a dummy image with a generated ID
                val dummyImage = Image(
                    id = "img_${System.currentTimeMillis()}",
                    name = "Uploaded image",
                    cid = "QmXzY1bT3GDj2a2X4DuZbh7q9CVszJk6XzGPAmhBYxJaGs",
                    url = "https://images.unsplash.com/photo-1519681393784-d120267933ba",
                    uploadDate = LocalDateTime.now()
                )
                
                _uploadState.value = UploadState.Success(dummyImage)
                
            } catch (e: Exception) {
                Log.e("UploadViewModel", "Exception during upload", e)
                e.printStackTrace()
                _uploadState.value = UploadState.Error("Exception: ${e.message}")
            }
        }
    }
    
    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val image: Image) : UploadState()
        data class Error(val message: String) : UploadState()
    }
} 