package com.example.pincast.data

import android.net.Uri
import android.util.Log
import com.example.pincast.data.models.Collection
import com.example.pincast.data.models.Image
import com.example.pincast.data.remote.PincastApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime

/**
 * Repository for handling data operations
 */
class PincastRepository(private val api: PincastApi) {
    
    private val TAG = "PincastRepository"
    private val IPFS_GATEWAY = "https://ipfs.io/ipfs"

    // User authentication state
    private var authToken: String? = null
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return !authToken.isNullOrEmpty()
    }
    
    /**
     * Login user with email and password
     */
    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            val response = api.login(email, password)
            if (response.isSuccessful) {
                response.body()?.get("token")?.let {
                    authToken = it
                    true
                } ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            false
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
        } finally {
            authToken = null
        }
    }
    
    /**
     * Get all images
     */
    fun getAllImages(): Flow<Result<List<Image>>> = flow {
        emit(Result.loading<List<Image>>())
        
        try {
            val response = api.getAllImages()
            if (response.isSuccessful) {
                val images = response.body() ?: emptyList()
                emit(Result.success(images))
            } else {
                emit(Result.failure(Exception("Failed to fetch images: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching images", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get all collections
     */
    fun getAllCollections(): Flow<Result<List<Collection>>> = flow {
        emit(Result.loading<List<Collection>>())
        
        try {
            val response = api.getAllCollections()
            if (response.isSuccessful) {
                // This would normally parse the response into Collection objects
                // Simplified for now
                emit(Result.success(emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to fetch collections: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching collections", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete an image by ID
     */
    fun deleteImage(id: String): Flow<Result<Boolean>> = flow {
        emit(Result.loading<Boolean>())
        
        try {
            val response = api.deleteImage(id)
            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                emit(Result.failure(Exception("Failed to delete image: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete a collection by ID
     */
    fun deleteCollection(id: String): Flow<Result<Boolean>> = flow {
        emit(Result.loading<Boolean>())
        
        try {
            val response = api.deleteCollection(id)
            if (response.isSuccessful) {
                emit(Result.success(true))
            } else {
                emit(Result.failure(Exception("Failed to delete collection: ${response.code()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting collection", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Upload an image
     * In a real app, this would upload to the API
     */
    fun uploadImage(uri: Uri): Result<Image> {
        // This would normally process the file and make an API call
        // For now, we'll just create a dummy image
        val dummyImage = Image(
            id = "img_${System.currentTimeMillis()}",
            name = "Uploaded ${LocalDateTime.now()}",
            cid = "QmXzY1bT3GDj2a2X4DuZbh7q9CVszJk6XzGPAmhBYxJaGs",
            url = "https://images.unsplash.com/photo-1519681393784-d120267933ba",
            uploadDate = LocalDateTime.now()
        )
        
        return Result.success(dummyImage)
    }
} 