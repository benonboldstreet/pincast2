package com.example.pincast.data.remote.models

import com.google.gson.annotations.SerializedName
import java.io.File

data class ImageRequest(
    @SerializedName("file")
    val file: File
)

data class ImageResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("cid")
    val cid: String,
    @SerializedName("size")
    val size: Long = 0,
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
) 