package com.example.pincast.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pincast.PincastApplication
import com.example.pincast.data.cache.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val maxCacheSizeMb: Int = 100,
    val currentCacheSizeMb: Int = 0,
    val cachedItemCount: Int = 0,
    val autoCleanCache: Boolean = true,
    val preCacheOnWifi: Boolean = true,
    val keepFavoritesCached: Boolean = true,
    val primaryGateway: String = "https://cloudflare-ipfs.com/ipfs/",
    val availableGateways: List<String> = CacheManager.IPFS_GATEWAYS,
    val gatewayStats: Map<String, Long> = emptyMap()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val cacheRepository = (application as PincastApplication).cacheRepository
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        updateCacheStats()
    }
    
    private fun loadSettings() {
        // In a real app, these would be loaded from a preferences store
        // For now, we'll use default values
        _uiState.update { it.copy(
            maxCacheSizeMb = 100,
            autoCleanCache = true,
            preCacheOnWifi = true,
            keepFavoritesCached = true,
            primaryGateway = CacheManager.IPFS_GATEWAYS.first()
        ) }
    }
    
    private fun updateCacheStats() {
        viewModelScope.launch(Dispatchers.IO) {
            // Get cache directory size
            val cacheDir = File(getApplication<PincastApplication>().cacheDir, "ipfs_cache")
            if (cacheDir.exists()) {
                val size = calculateDirSize(cacheDir) / (1024 * 1024) // Convert to MB
                
                // Get cached item count
                val itemCount = cacheDir.listFiles()?.size ?: 0
                
                _uiState.update { it.copy(
                    currentCacheSizeMb = size,
                    cachedItemCount = itemCount
                ) }
            }
            
            // In a real app, we would also load gateway stats
            // For now, let's simulate some performance data
            val mockGatewayStats = mapOf(
                "https://cloudflare-ipfs.com/ipfs/" to 145L,
                "https://ipfs.io/ipfs/" to 320L,
                "https://gateway.pinata.cloud/ipfs/" to 210L
            )
            
            _uiState.update { it.copy(gatewayStats = mockGatewayStats) }
        }
    }
    
    fun updateMaxCacheSize(size: Int) {
        _uiState.update { it.copy(maxCacheSizeMb = size) }
        viewModelScope.launch {
            // In a real app, save this to preferences
            // For now, just update the cache
            cacheRepository.pruneCache(size)
            updateCacheStats()
        }
    }
    
    fun updateAutoCleanCache(enabled: Boolean) {
        _uiState.update { it.copy(autoCleanCache = enabled) }
        // In a real app, save this to preferences
    }
    
    fun updatePreCacheOnWifi(enabled: Boolean) {
        _uiState.update { it.copy(preCacheOnWifi = enabled) }
        // In a real app, save this to preferences
    }
    
    fun updateKeepFavoritesCached(enabled: Boolean) {
        _uiState.update { it.copy(keepFavoritesCached = enabled) }
        // In a real app, save this to preferences
    }
    
    fun updatePrimaryGateway(gateway: String) {
        _uiState.update { it.copy(primaryGateway = gateway) }
        // In a real app, save this to preferences
    }
    
    fun clearCache() {
        viewModelScope.launch {
            if (uiState.value.keepFavoritesCached) {
                cacheRepository.clearNonFavoriteCache()
            } else {
                // Clear the entire cache directory
                val cacheDir = File(getApplication<PincastApplication>().cacheDir, "ipfs_cache")
                cacheDir.listFiles()?.forEach { it.delete() }
            }
            updateCacheStats()
        }
    }
    
    private fun calculateDirSize(dir: File): Long {
        if (!dir.exists()) return 0
        var size: Long = 0
        
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirSize(file)
            } else {
                file.length()
            }
        }
        
        return size
    }
} 