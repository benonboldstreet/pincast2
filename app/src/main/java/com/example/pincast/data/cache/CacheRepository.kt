package com.example.pincast.data.cache

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.pincast.data.models.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

/**
 * Repository that manages the cache system
 */
class CacheRepository(private val context: Context) {
    
    private val TAG = "CacheRepository"
    
    // Expose the CacheManager and DAO for advanced use cases
    val cacheManager = CacheManager(context)
    private val database = CidDatabase.getDatabase(context)
    val cidDao = database.cidMetadataDao()
    
    /**
     * Get all cached items
     */
    fun getAllCachedItems(): Flow<List<CidMetadata>> {
        return cidDao.getAllMetadata()
    }
    
    /**
     * Get favorite items
     */
    fun getFavorites(): Flow<List<CidMetadata>> {
        return cidDao.getFavorites()
    }
    
    /**
     * Search for cached items
     */
    fun searchCache(query: String): Flow<List<CidMetadata>> {
        return cidDao.search(query)
    }
    
    /**
     * Convert CidMetadata to an Image object
     */
    private suspend fun cidMetadataToImage(metadata: CidMetadata): Image {
        val url = cacheManager.getBestUrlForCid(metadata.cid)
        return Image(
            id = metadata.cid, // Using CID as ID
            name = metadata.name,
            cid = metadata.cid,
            url = url,
            uploadDate = metadata.lastAccessed
        )
    }
    
    /**
     * Get all cached items as Images
     */
    fun getAllCachedImages(): Flow<List<Image>> {
        return cidDao.getAllMetadata().map { metadataList ->
            metadataList.map { metadata ->
                Image(
                    id = metadata.cid,
                    name = metadata.name,
                    cid = metadata.cid,
                    url = metadata.localPath ?: "https://ipfs.io/ipfs/${metadata.cid}",
                    uploadDate = metadata.lastAccessed
                )
            }
        }
    }
    
    /**
     * Store image metadata in cache
     */
    suspend fun cacheImageMetadata(image: Image) {
        val metadata = CidMetadata(
            cid = image.cid,
            name = image.name,
            lastAccessed = LocalDateTime.now(),
            accessCount = 1
        )
        
        cidDao.insert(metadata)
        cacheManager.cacheImage(image)
    }
    
    /**
     * Get metadata for a CID
     */
    suspend fun getMetadata(cid: String): CidMetadata? {
        return cidDao.getMetadata(cid)
    }
    
    /**
     * Get the best URL for a CID
     */
    suspend fun getBestUrlForCid(cid: String): String {
        // Track access
        val metadata = cidDao.getMetadata(cid)
        if (metadata != null) {
            cidDao.incrementAccessCount(cid)
        }
        
        // Check if we have a local file first
        if (metadata?.localPath != null) {
            val file = File(metadata.localPath)
            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "Using local file for CID $cid: ${metadata.localPath}")
                return "file://${metadata.localPath}"
            }
        }
        
        // Otherwise get best gateway URL
        return cacheManager.getBestUrlForCid(cid)
    }
    
    /**
     * Preload an image by CID
     * Returns true if successful, false otherwise
     */
    suspend fun preloadImage(cid: String): Boolean = withContext(Dispatchers.IO) {
        // Skip if we're not on WiFi
        if (!isOnWifi()) {
            Log.d(TAG, "Skipping preload for CID $cid - not on WiFi")
            return@withContext false
        }
        
        try {
            val file = cacheManager.downloadAndCacheFile(cid)
            if (file != null) {
                // Update metadata with local path
                val metadata = cidDao.getMetadata(cid)
                if (metadata != null) {
                    cidDao.update(metadata.copy(localPath = file.absolutePath))
                } else {
                    // Create new metadata if none exists
                    val newMetadata = CidMetadata(
                        cid = cid,
                        name = "Unknown",
                        localPath = file.absolutePath,
                        lastAccessed = LocalDateTime.now()
                    )
                    cidDao.insert(newMetadata)
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preload CID $cid: ${e.message}")
        }
        
        return@withContext false
    }
    
    /**
     * Delete a CID from cache
     */
    suspend fun deleteCid(cid: String) {
        // Delete from database
        cidDao.deleteByCid(cid)
        
        // Delete local file
        val file = cacheManager.getCachedFile(cid)
        file?.delete()
    }
    
    /**
     * Set a CID as favorite
     */
    suspend fun setFavorite(cid: String, isFavorite: Boolean) {
        // Check if metadata exists first
        val metadata = cidDao.getMetadata(cid)
        if (metadata != null) {
            cidDao.setFavorite(cid, isFavorite)
        } else {
            // Create metadata if it doesn't exist
            val newMetadata = CidMetadata(
                cid = cid,
                name = "Unknown",
                isFavorite = isFavorite,
                lastAccessed = LocalDateTime.now()
            )
            cidDao.insert(newMetadata)
        }
    }
    
    /**
     * Clear all non-favorite cached content
     */
    suspend fun clearNonFavoriteCache() = withContext(Dispatchers.IO) {
        val nonFavorites = cidDao.getAllMetadata().map { it.filter { metadata -> !metadata.isFavorite } }
        nonFavorites.collect { metadataList ->
            for (metadata in metadataList) {
                deleteCid(metadata.cid)
            }
        }
    }
    
    /**
     * Prune cache to stay under size limit
     */
    suspend fun pruneCache(maxSizeMb: Int) {
        cacheManager.pruneCache(maxSizeMb)
    }
    
    /**
     * Check if device is connected to WiFi
     */
    private fun isOnWifi(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
} 