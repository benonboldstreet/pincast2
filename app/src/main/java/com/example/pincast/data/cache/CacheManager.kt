package com.example.pincast.data.cache

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.example.pincast.data.models.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * A layered caching system for IPFS content
 * Layer 1: Memory cache (LruCache)
 * Layer 2: Disk cache
 * Layer 3: IPFS gateways
 */
class CacheManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CacheManager"
        private const val MEMORY_CACHE_SIZE = 20 // Number of images to keep in memory
        private const val CACHE_DIR_NAME = "ipfs_cache"
        
        // List of IPFS gateways in order of preference
        val IPFS_GATEWAYS = listOf(
            "https://cloudflare-ipfs.com/ipfs/",
            "https://ipfs.io/ipfs/",
            "https://gateway.pinata.cloud/ipfs/",
            "https://gateway.ipfs.io/ipfs/",
            "https://dweb.link/ipfs/",
            "https://ipfs.fleek.co/ipfs/"
        )
        
        // Gateway performance tracking
        private val gatewayResponseTimes = ConcurrentHashMap<String, Long>()
    }
    
    // Memory cache for Image metadata
    private val memoryCache = LruCache<String, Image>(MEMORY_CACHE_SIZE)
    
    // Memory cache for image URLs that have been validated
    private val urlCache = LruCache<String, String>(50)
    
    // Cache directory for storing downloaded files
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Store an image in the memory cache
     */
    fun cacheImage(image: Image) {
        memoryCache.put(image.cid, image)
    }
    
    /**
     * Retrieve an image from the memory cache
     */
    fun getCachedImage(cid: String): Image? {
        return memoryCache.get(cid)
    }
    
    /**
     * Create a file path for a CID
     */
    private fun getFileForCid(cid: String): File {
        return File(cacheDir, cid)
    }
    
    /**
     * Check if a CID is cached on disk
     */
    fun isFileCached(cid: String): Boolean {
        val file = getFileForCid(cid)
        return file.exists() && file.length() > 0
    }
    
    /**
     * Get a cached file for a CID
     */
    fun getCachedFile(cid: String): File? {
        val file = getFileForCid(cid)
        return if (file.exists() && file.length() > 0) {
            file
        } else {
            null
        }
    }
    
    /**
     * Download and cache a file from IPFS
     * Returns the local file if successful, null otherwise
     */
    suspend fun downloadAndCacheFile(cid: String): File? = withContext(Dispatchers.IO) {
        if (isFileCached(cid)) {
            return@withContext getCachedFile(cid)
        }
        
        // Try each gateway until one works
        for (gateway in getSortedGateways()) {
            try {
                val startTime = System.currentTimeMillis()
                val url = "$gateway$cid"
                val urlObj = URL(url)
                val connection = urlObj.openConnection()
                connection.connectTimeout = 10000 // 10 seconds timeout
                connection.readTimeout = 30000 // 30 seconds read timeout
                
                val outputFile = getFileForCid(cid)
                FileOutputStream(outputFile).use { fileOut ->
                    val readableByteChannel: ReadableByteChannel = Channels.newChannel(connection.getInputStream())
                    val fileChannel = fileOut.channel
                    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
                }
                
                // Record the response time
                val responseTime = System.currentTimeMillis() - startTime
                gatewayResponseTimes[gateway] = responseTime
                
                Log.d(TAG, "Successfully downloaded CID $cid from $gateway in $responseTime ms")
                
                // Return the cached file
                return@withContext outputFile
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download from gateway $gateway: ${e.message}")
                // Continue to next gateway
            }
        }
        
        // If all gateways failed
        Log.e(TAG, "All gateways failed for CID: $cid")
        return@withContext null
    }
    
    /**
     * Get the best URL for a CID, checking the cache first
     */
    suspend fun getBestUrlForCid(cid: String): String = withContext(Dispatchers.IO) {
        // Check if we have a cached URL for this CID
        urlCache.get(cid)?.let { return@withContext it }
        
        // Try each gateway until one works
        for (gateway in getSortedGateways()) {
            try {
                val url = "$gateway$cid"
                val urlObj = URL(url)
                val connection = urlObj.openConnection()
                connection.connectTimeout = 5000 // 5 seconds timeout
                connection.connect()
                
                // If we reach here, the URL works
                urlCache.put(cid, url)
                return@withContext url
            } catch (e: Exception) {
                Log.w(TAG, "Gateway $gateway is not available: ${e.message}")
                // Continue to next gateway
            }
        }
        
        // If all gateways failed, return the first one as a fallback
        return@withContext "${IPFS_GATEWAYS[0]}$cid"
    }
    
    /**
     * Get gateways sorted by response time (fastest first)
     */
    private fun getSortedGateways(): List<String> {
        // If we have no performance data yet, return the default order
        if (gatewayResponseTimes.isEmpty()) {
            return IPFS_GATEWAYS
        }
        
        // Sort gateways by response time
        return IPFS_GATEWAYS.sortedBy { gateway ->
            gatewayResponseTimes[gateway] ?: Long.MAX_VALUE
        }
    }
    
    /**
     * Clear the entire cache
     */
    fun clearCache() {
        // Clear memory cache
        memoryCache.evictAll()
        urlCache.evictAll()
        
        // Clear file cache
        withContext(Dispatchers.IO) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }
    
    /**
     * Prune the cache to the specified size limit
     */
    suspend fun pruneCache(maxSizeMb: Int) = withContext(Dispatchers.IO) {
        val maxBytes = maxSizeMb * 1024 * 1024L
        val files = cacheDir.listFiles() ?: return@withContext
        
        // If we're under the limit, no need to prune
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= maxBytes) return@withContext
        
        // Sort files by last modified (oldest first)
        val sortedFiles = files.sortedBy { it.lastModified() }
        
        // Delete oldest files until we're under the limit
        for (file in sortedFiles) {
            if (totalSize <= maxBytes) break
            
            val fileSize = file.length()
            if (file.delete()) {
                totalSize -= fileSize
                Log.d(TAG, "Pruned cache file: ${file.name}, size: $fileSize bytes")
            }
        }
    }
} 