package com.example.pincast.ui.viewmodels

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class GatewayStatus {
    ONLINE,
    OFFLINE,
    SLOW,
    UNKNOWN
}

data class GatewayInfo(
    val name: String,
    val url: String,
    val status: GatewayStatus = GatewayStatus.UNKNOWN,
    val responseTimeMs: Long = 0,
    val lastTestedAt: String? = null
)

data class GatewayHealthUiState(
    val gatewayStats: List<GatewayInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GatewayHealthViewModel(application: Application) : AndroidViewModel(application) {
    private val cacheRepository = (application as PincastApplication).cacheRepository
    private val TAG = "GatewayHealthViewModel"
    
    private val _uiState = MutableStateFlow(GatewayHealthUiState(isLoading = true))
    val uiState: StateFlow<GatewayHealthUiState> = _uiState.asStateFlow()
    
    init {
        initializeGateways()
        refreshAllGateways()
    }
    
    private fun initializeGateways() {
        val gateways = CacheManager.IPFS_GATEWAYS.map { gateway ->
            GatewayInfo(
                name = gateway,
                url = "${gateway}QmYgtfMBZW5B5bWgwHXvDyDwHXxG8yf6xgULYANpBnz7Kf" // Test CID
            )
        }
        
        _uiState.update { it.copy(gatewayStats = gateways) }
    }
    
    fun refreshAllGateways() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val updatedGateways = mutableListOf<GatewayInfo>()
                
                withContext(Dispatchers.IO) {
                    for (gateway in _uiState.value.gatewayStats) {
                        updatedGateways.add(testGateway(gateway))
                    }
                }
                
                _uiState.update { it.copy(
                    gatewayStats = updatedGateways,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                Log.e(TAG, "Error testing gateways", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error testing gateways: ${e.message}"
                ) }
            }
        }
    }
    
    fun refreshGateway(gatewayUrl: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val currentGateways = state.gatewayStats.toMutableList()
                val gatewayIndex = currentGateways.indexOfFirst { it.url == gatewayUrl }
                
                if (gatewayIndex >= 0) {
                    // Mark this gateway as loading
                    val updatedGateway = currentGateways[gatewayIndex].copy(status = GatewayStatus.UNKNOWN)
                    currentGateways[gatewayIndex] = updatedGateway
                }
                
                state.copy(gatewayStats = currentGateways, isLoading = true)
            }
            
            try {
                withContext(Dispatchers.IO) {
                    val gatewayIndex = _uiState.value.gatewayStats.indexOfFirst { it.url == gatewayUrl }
                    if (gatewayIndex >= 0) {
                        val gateway = _uiState.value.gatewayStats[gatewayIndex]
                        val updatedGateway = testGateway(gateway)
                        
                        _uiState.update { state ->
                            val currentGateways = state.gatewayStats.toMutableList()
                            currentGateways[gatewayIndex] = updatedGateway
                            state.copy(gatewayStats = currentGateways, isLoading = false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error testing gateway: $gatewayUrl", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private suspend fun testGateway(gateway: GatewayInfo): GatewayInfo {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val url = URL(gateway.url)
                val connection = url.openConnection()
                connection.connectTimeout = 5000 // 5 seconds
                connection.readTimeout = 10000 // 10 seconds
                connection.connect()
                
                // Read a small amount of data to fully test the connection
                val inputStream = connection.getInputStream()
                val buffer = ByteArray(1024)
                inputStream.read(buffer)
                inputStream.close()
                
                val endTime = System.currentTimeMillis()
                val responseTime = endTime - startTime
                
                // Determine status based on response time
                val status = when {
                    responseTime < 1000 -> GatewayStatus.ONLINE
                    responseTime < 3000 -> GatewayStatus.SLOW
                    else -> GatewayStatus.SLOW
                }
                
                gateway.copy(
                    status = status,
                    responseTimeMs = responseTime,
                    lastTestedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
            } catch (e: Exception) {
                Log.e(TAG, "Gateway test failed for ${gateway.url}: ${e.message}")
                gateway.copy(
                    status = GatewayStatus.OFFLINE,
                    responseTimeMs = 0,
                    lastTestedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
            }
        }
    }
} 