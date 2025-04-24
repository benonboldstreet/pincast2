package com.example.pincast.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pincast.data.api.ApiClient
import com.example.pincast.data.local.UserPreferences
import com.example.pincast.data.models.Image
import com.example.pincast.data.models.ImageUploadResponse
import com.example.pincast.data.models.FilesListResponse
import com.example.pincast.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class PincastRepository(private val context: Context) {
    
    private val TAG = "PincastRepository"
    private val userPreferences = UserPreferences(context)
    private val apiService = ApiClient.jackalApiService
    
    suspend fun loginUser(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        // Store user credentials locally (in a real app, we'd authenticate first)
        val user = User(email, password)
        userPreferences.saveUser(user)
        return@withContext true
    }
    
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        userPreferences.isUserLoggedIn()
    }
    
    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        userPreferences.logout()
        return@withContext true
    }
    
    suspend fun uploadImage(imageUri: Uri): Result<Image> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload for Uri: $imageUri")
            
            val file = getFileFromUri(imageUri)
            if (file == null) {
                Log.e(TAG, "Failed to load file from Uri")
                return@withContext Result.failure(Exception("Failed to load file"))
            }
            
            Log.d(TAG, "File prepared: ${file.absolutePath}, size: ${file.length()} bytes")
            
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            Log.d(TAG, "Sending request to Jackal API with Authorization: ${ApiClient.getAuthHeader().take(20)}...")
            
            val response = try {
                apiService.uploadImage(ApiClient.getAuthHeader(), part)
            } catch (e: Exception) {
                Log.e(TAG, "API call failed", e)
                return@withContext Result.failure(e)
            }
            
            // Handle the response according to the documented format
            if (response.cid.isBlank()) {
                Log.e(TAG, "Upload failed: Empty CID in response")
                return@withContext Result.failure(Exception("Upload failed: No valid CID returned"))
            }
            
            Log.d(TAG, "Upload successful, CID: ${response.cid}, Name: ${response.name}")
            
            // Create URL as per documentation - using IPFS gateway for viewing
            // Try Cloudflare's IPFS gateway which is more reliable than ipfs.io
            val ipfsUrl = "https://cloudflare-ipfs.com/ipfs/${response.cid}"
            
            val image = Image(
                id = response.id.ifBlank { UUID.randomUUID().toString() },
                cid = response.cid,
                name = response.name.ifBlank { file.name },
                uploadDate = LocalDateTime.now(),
                url = response.url.ifBlank { ipfsUrl }
            )
            
            return@withContext Result.success(image)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during upload", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun getAllImages(): Result<List<Image>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all images")
            val response = apiService.getImages(ApiClient.getAuthHeader())
            
            val images = response.files.map { item ->
                // Map API response to our model
                val cid = item.cid
                // Create multiple direct IPFS URLs without query parameters for better compatibility
                val ipfsUrl = "https://cloudflare-ipfs.com/ipfs/$cid"
                
                Image(
                    id = item.id.toString(),
                    cid = cid,
                    name = item.fileName,
                    uploadDate = try {
                        val createdAt = item.createdAt
                        if (createdAt.isNotBlank()) {
                            LocalDateTime.parse(createdAt.split(".")[0])
                        } else {
                            LocalDateTime.now()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date: ${item.createdAt}", e)
                        LocalDateTime.now()
                    },
                    url = ipfsUrl
                )
            }
            
            Log.d(TAG, "Fetched ${images.size} images out of ${response.count} total")
            return@withContext Result.success(images)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch images", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun deleteImage(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting image with ID: $id")
            try {
                val response = apiService.deleteImage(ApiClient.getAuthHeader(), id)
                val success = response["success"] == true
                Log.d(TAG, "Delete result: $success")
                return@withContext Result.success(success)
            } catch (e: Exception) {
                // Handle specific empty response case (line 1, column 1 error)
                if (e.message?.contains("end of input at line 1 column 1") == true) {
                    Log.d(TAG, "Got empty response, but considering deletion successful")
                    return@withContext Result.success(true)
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete image", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun renameImage(id: String, newName: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Renaming image with ID: $id to '$newName'")
            val request = mapOf("file_name" to newName)
            val response = apiService.renameImage(ApiClient.getAuthHeader(), id, request)
            val success = response["success"] == true
            Log.d(TAG, "Rename result: $success")
            return@withContext Result.success(success)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rename image", e)
            return@withContext Result.failure(e)
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.cacheDir, fileName)
        
        try {
            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
} 