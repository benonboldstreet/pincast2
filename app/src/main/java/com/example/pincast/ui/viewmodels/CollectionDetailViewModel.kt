package com.example.pincast.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pincast.PincastApplication
import com.example.pincast.data.models.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: Collection? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CollectionDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as PincastApplication).repository
    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllCollections()
                .collect { result ->
                    when {
                        result.isSuccess -> {
                            val collections = result.getOrNull() ?: emptyList()
                            val collection = collections.find { it.id == collectionId }
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                collection = collection
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

    fun deleteCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val collectionId = _uiState.value.collection?.id ?: return@launch
            repository.deleteCollection(collectionId)
                .collect { result ->
                    when {
                        result.isSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                collection = null
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 