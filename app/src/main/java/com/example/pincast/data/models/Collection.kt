package com.example.pincast.data.models

import androidx.compose.runtime.Immutable

@Immutable
data class Collection(
    val id: String,
    val name: String,
    val description: String,
    val cid: String,
    val images: List<Image> = emptyList()
) 