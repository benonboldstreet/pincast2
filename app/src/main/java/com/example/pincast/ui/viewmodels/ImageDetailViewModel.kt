package com.example.pincast.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pincast.PincastApplication
import com.example.pincast.data.cache.CidMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URL
import java.net.URLConnection

// Default IPFS gateways for the view model to use
private val DEFAULT_IPFS_GATEWAYS = listOf(
    "https://cloudflare-ipfs.com/ipfs/",
    "https://ipfs.io/ipfs/",
    "https://gateway.pinata.cloud/ipfs/",
    "https://gateway.ipfs.io/ipfs/",
    "https://dweb.link/ipfs/",
    "https://ipfs.fleek.co/ipfs/"
)

data class ImageDetailUiState(
    val cid: String = "",
    val currentGateway: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val metadata: CidMetadata? = null,
    val isFavorite: Boolean = false,
    val gatewayIndex: Int = 0,
    val availableGateways: List<String> = emptyList()
)

class ImageDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val cacheRepository = (application as PincastApplication).cacheRepository
    private val TAG = "ImageDetailViewModel"
    
    private val _uiState = MutableStateFlow(ImageDetailUiState())
    val uiState: StateFlow<ImageDetailUiState> = _uiState.asStateFlow()
    
    fun initialize(cid: String) {
        _uiState.update { it.copy(
            cid = cid,
            isLoading = true,
            availableGateways = DEFAULT_IPFS_GATEWAYS.map { gateway -> "$gateway$cid" }
        ) }
        
        viewModelScope.launch {
            // Load metadata
            loadMetadata(cid)
            
            // Get the best URL
            try {
                val bestUrl = cacheRepository.getBestUrlForCid(cid)
                _uiState.update { it.copy(
                    currentGateway = bestUrl,
                    isLoading = false
                ) }
                
                // Preload the image in the background if it's not already cached
                if (!cacheRepository.cacheManager.isFileCached(cid)) {
                    preloadImage(cid)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting best URL for CID $cid", e)
                val fallbackUrl = "${DEFAULT_IPFS_GATEWAYS.first()}$cid"
                _uiState.update { it.copy(
                    currentGateway = fallbackUrl,
                    isLoading = false
                ) }
            }
        }
    }
    
    private suspend fun loadMetadata(cid: String) {
        try {
            val metadata = cacheRepository.getMetadata(cid)
            if (metadata != null) {
                _uiState.update { it.copy(
                    metadata = metadata,
                    isFavorite = metadata.isFavorite
                ) }
            } else {
                // No metadata yet - create it
                val newMetadata = CidMetadata(
                    cid = cid,
                    name = "Unknown",
                    mimeType = detectMimeType(cid),
                    accessCount = 1
                )
                cacheRepository.cidDao.insert(newMetadata)
                _uiState.update { it.copy(metadata = newMetadata) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading metadata for CID $cid", e)
        }
    }
    
    private suspend fun detectMimeType(cid: String): String {
        return try {
            val gateway = DEFAULT_IPFS_GATEWAYS.first()
            val url = URL("$gateway$cid")
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.connect()
            connection.contentType ?: "application/octet-stream"
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting MIME type for CID $cid", e)
            "application/octet-stream"
        }
    }
    
    private suspend fun preloadImage(cid: String) {
        viewModelScope.launch {
            try {
                cacheRepository.preloadImage(cid)
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading image for CID $cid", e)
            }
        }
    }
    
    fun tryNextGateway() {
        _uiState.update { state ->
            // Move to next gateway
            val nextIndex = (state.gatewayIndex + 1) % state.availableGateways.size
            state.copy(
                gatewayIndex = nextIndex,
                currentGateway = state.availableGateways[nextIndex],
                isLoading = true,
                errorMessage = null
            )
        }
        
        // Test the connection and update loading state
        viewModelScope.launch {
            try {
                val currentGateway = _uiState.value.currentGateway
                val url = URL(currentGateway)
                val connection = url.openConnection() as URLConnection
                connection.connectTimeout = 5000
                connection.connect()
                
                // Connection successful
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to gateway: ${_uiState.value.currentGateway}", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to connect to gateway: ${e.localizedMessage}"
                ) }
            }
        }
    }
    
    fun toggleFavorite() {
        val cid = _uiState.value.cid
        val currentFavorite = _uiState.value.isFavorite
        
        viewModelScope.launch {
            try {
                cacheRepository.setFavorite(cid, !currentFavorite)
                _uiState.update { it.copy(isFavorite = !currentFavorite) }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite for CID $cid", e)
            }
        }
    }
} 