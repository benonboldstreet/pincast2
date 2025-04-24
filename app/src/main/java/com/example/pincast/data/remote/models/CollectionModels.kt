package com.example.pincast.data.remote.models

import com.google.gson.annotations.SerializedName

data class CollectionRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageCids")
    val imageCids: List<String>
)

data class CollectionResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("cid")
    val cid: String,
    @SerializedName("images")
    val images: List<ImageResponse>
) 