package com.example.pincast.data.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.time.LocalDateTime
import com.example.pincast.util.LocalDateTimeParceler

@Immutable
@Parcelize
@TypeParceler<LocalDateTime, LocalDateTimeParceler>
data class Image(
    val id: String,
    val name: String,
    val cid: String,
    val url: String,
    val uploadDate: LocalDateTime = LocalDateTime.now()
) : Parcelable

// Response models for API interactions
data class ImageUploadResponse(
    @SerializedName("name") val name: String = "",
    @SerializedName("cid") val cid: String = "",
    @SerializedName("merkle") val merkle: String = "",
    
    // Optional fields that might be in the response
    @SerializedName("url") val url: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("error") val error: String = ""
)

// Model for the list files response
data class FilesListResponse(
    @SerializedName("files") val files: List<FileItem> = emptyList(),
    @SerializedName("count") val count: Int = 0
)

data class FileItem(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("file_name") val fileName: String = "",
    @SerializedName("cid") val cid: String = "",
    @SerializedName("size") val size: Int = 0,
    @SerializedName("created_at") val createdAt: String = ""
) 