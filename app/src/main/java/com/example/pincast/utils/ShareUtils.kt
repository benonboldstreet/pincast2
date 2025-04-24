package com.example.pincast.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Share an image URL with external apps
 */
fun shareImage(context: Context, url: String) {
    try {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
    }
} 