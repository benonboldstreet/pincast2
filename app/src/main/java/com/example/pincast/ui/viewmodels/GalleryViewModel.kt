package com.example.pincast.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pincast.PincastApplication
import com.example.pincast.data.models.Image
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GalleryUiState(
    val images: List<Image> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PincastApplication).repository
    
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun loadImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllImages()
                .collect { result ->
                    when {
                        result.isLoading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        result.isSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                images = result.getOrNull() ?: emptyList()
                            )
                        }
                        result.isFailure -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        }
                    }
                }
        }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.deleteImage(image.id)
                .collect { result ->
                    when {
                        result.isLoading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        result.isSuccess -> {
                            loadImages()
                        }
                        result.isFailure -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        }
                    }
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 