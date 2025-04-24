package com.example.pincast.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.pincast.data.models.Image
import com.example.pincast.utils.toast
import java.net.URLDecoder
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    cid: String?,
    name: String?,
    url: String?,
    navController: NavController
) {
    val context = LocalContext.current
    
    // Decode URL parameters
    val decodedName = try { URLDecoder.decode(name, "UTF-8") } catch (e: Exception) { name ?: "Image" }
    val decodedUrl = try { URLDecoder.decode(url, "UTF-8") } catch (e: Exception) { url ?: "" }
    
    // Create an image object from the parameters
    val image = Image(
        id = "",  // Empty string as this is just for display
        cid = cid ?: "", 
        name = decodedName, 
        url = decodedUrl,
        uploadDate = LocalDateTime.now()  // Current time as this is just for display
    )
    
    // State for zooming and panning
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Create a list of fallback URLs
    val fallbackUrls = buildList {
        // Original URL
        add(image.url)
        
        // IPFS gateways with various parameters
        if (image.cid.isNotEmpty()) {
            // Cloudflare gateway with filename
            add("https://cloudflare-ipfs.com/ipfs/${image.cid}?filename=${image.name}")
            // Cloudflare gateway direct
            add("https://cloudflare-ipfs.com/ipfs/${image.cid}")
            // IPFS.io gateway with filename
            add("https://ipfs.io/ipfs/${image.cid}?filename=${image.name}")
            // IPFS.io gateway direct
            add("https://ipfs.io/ipfs/${image.cid}")
            // Dweb.link gateway
            add("https://dweb.link/ipfs/${image.cid}")
            // NFT.Storage gateway
            add("https://nftstorage.link/ipfs/${image.cid}")
            // Gateway.pinata.cloud
            add("https://gateway.pinata.cloud/ipfs/${image.cid}")
        }
    }
    
    // State for tracking the current URL being used
    var currentUrlIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(decodedName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            // Display the image with zoom/pan capability
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fallbackUrls[currentUrlIndex])
                    .crossfade(true)
                    .build(),
                contentDescription = decodedName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            
                            // Adjust the pan amount based on the scale
                            val scaledPanX = pan.x * scale
                            val scaledPanY = pan.y * scale
                            
                            // Apply the pan with constraints based on the scale
                            offsetX += scaledPanX
                            offsetY += scaledPanY
                            
                            // Limit the panning based on the scale
                            val maxOffset = (scale - 1) * 500 // Approximate constraint
                            offsetX = offsetX.coerceIn(-maxOffset, maxOffset)
                            offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
                        }
                    },
                loading = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading image...\nTrying source ${currentUrlIndex + 1} of ${fallbackUrls.size}",
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                },
                error = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (loadError) {
                            Text(
                                "Failed to load image from all sources",
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                },
                onSuccess = { 
                    isLoading = false
                    loadError = false
                    // Reset zoom/pan when a new image is loaded
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                onError = {
                    Log.e("ImageDetailScreen", "Error loading URL: ${fallbackUrls[currentUrlIndex]}")
                    
                    if (currentUrlIndex < fallbackUrls.size - 1) {
                        // Try the next URL
                        currentUrlIndex++
                        isLoading = true
                    } else {
                        // All URLs failed
                        loadError = true
                        isLoading = false
                        context.toast("Failed to load image after trying ${fallbackUrls.size} sources")
                    }
                }
            )
            
            // Help text overlay
            if (!isLoading && !loadError) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        "Pinch to zoom • Double tap to reset",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
} 