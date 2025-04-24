package com.example.pincast.data.api

import com.example.pincast.data.models.FilesListResponse
import com.example.pincast.data.models.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface JackalApiService {
    
    @Multipart
    @POST("api/files")
    suspend fun uploadImage(
        @Header("Authorization") authToken: String,
        @Part image: MultipartBody.Part
    ): ImageUploadResponse
    
    @GET("api/files")
    suspend fun getImages(
        @Header("Authorization") authToken: String
    ): FilesListResponse
    
    @DELETE("api/files/{id}")
    suspend fun deleteImage(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): Map<String, Boolean>
    
    @PUT("api/files/{id}")
    suspend fun renameImage(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Map<String, Boolean>
} 