package com.example.pincast.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pincast.ui.components.SimpleCollection
import com.example.pincast.ui.components.SimpleImage
import com.example.pincast.web3.WalletConnect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Simplified UI state without repository dependencies
data class SimpleWeb3UiState(
    val isWalletConnected: Boolean = false,
    val walletAddress: String? = null,
    val images: List<SimpleImage> = emptyList(),
    val collections: List<SimpleCollection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Simplified ViewModel without Hilt
class Web3ViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SimpleWeb3UiState())
    val uiState: StateFlow<SimpleWeb3UiState> = _uiState.asStateFlow()

    init {
        updateWalletState()
        // Initialize with sample data for demo purposes
        loadSampleData()
    }

    fun updateWalletState() {
        _uiState.value = _uiState.value.copy(
            isWalletConnected = WalletConnect.isWalletConnected(),
            walletAddress = WalletConnect.getWalletAddress()
        )
    }

    private fun loadSampleData() {
        val sampleImages = listOf(
            SimpleImage(
                id = "1",
                name = "Mountain Image",
                cid = "QmXzY1bT3GDj2a2X4DuZbh7q9CVszJk6XzGPAmhBYxJaGs",
                url = "https://images.unsplash.com/photo-1519681393784-d120267933ba"
            ),
            SimpleImage(
                id = "2",
                name = "Beach Sunset",
                cid = "QmYxT2QzNjPFs8WMHryHyxXLnDgbwJeQFLC7geqjn2no8X",
                url = "https://images.unsplash.com/photo-1506953823976-52e1fdc0149a"
            )
        )
        
        val sampleCollection = SimpleCollection(
            id = "1",
            name = "Nature Collection",
            description = "Beautiful nature photos",
            cid = "QmZVyTCJHFrcwj6KYzfCdoCfcF3yxYA6mVJMYMvBNq9dSW",
            images = sampleImages
        )
        
        _uiState.value = _uiState.value.copy(
            images = sampleImages,
            collections = listOf(sampleCollection),
            isLoading = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 