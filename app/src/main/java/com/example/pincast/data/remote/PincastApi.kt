package com.example.pincast.data.remote

import com.example.pincast.data.models.Image
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for Pincast backend
 */
interface PincastApi {
    @GET("images")
    suspend fun getAllImages(): Response<List<Image>>
    
    @GET("collections")
    suspend fun getAllCollections(): Response<List<Map<String, Any>>>
    
    @POST("upload")
    @Multipart
    suspend fun uploadImage(
        @Part("file") file: okhttp3.MultipartBody.Part
    ): Response<Map<String, String>>
    
    @DELETE("images/{id}")
    suspend fun deleteImage(
        @Path("id") id: String
    ): Response<Map<String, String>>
    
    @DELETE("collections/{id}")
    suspend fun deleteCollection(
        @Path("id") id: String
    ): Response<Map<String, String>>
    
    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<Map<String, String>>
    
    @POST("logout")
    suspend fun logout(): Response<Map<String, String>>
} 