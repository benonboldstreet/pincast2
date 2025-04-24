package com.example.pincast.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.pincast.data.models.Image
import com.example.pincast.ui.viewmodels.ImageDetailViewModel
import com.example.pincast.utils.toast
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    cid: String?,
    name: String?,
    url: String?,
    navController: NavController,
    viewModel: ImageDetailViewModel = viewModel()
) {
    // Decode URL parameters
    val decodedName = remember(name) {
        name?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: "Unknown"
    }
    
    val decodedUrl = remember(url) {
        url?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
    }
    
    // Create Image object
    val image = remember(cid, decodedName, decodedUrl) {
        if (cid != null) {
            Image(
                id = cid,
                name = decodedName,
                cid = cid,
                url = decodedUrl,
                uploadDate = LocalDateTime.now()
            )
        } else null
    }
    
    // Initialize view model with the image
    LaunchedEffect(cid) {
        if (cid != null) {
            viewModel.initialize(cid)
        }
    }
    
    // Local UI state
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var showHelp by remember { mutableStateOf(true) }
    var showMetadata by remember { mutableStateOf(false) }
    
    // Get UI state from view model
    val uiState by viewModel.uiState.collectAsState()
    
    // Temporary URLs to try if the primary one fails
    val currentGateway = uiState.currentGateway
    val imageCid = cid ?: ""
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage
    val metadata = uiState.metadata
    val isFavorite = uiState.isFavorite
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        decodedName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite button
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFFFC107) else LocalContentColor.current.copy(alpha = LocalContentColor.current.alpha)
                        )
                    }
                    
                    // Info button to toggle metadata
                    IconButton(onClick = { showMetadata = !showMetadata }) {
                        Icon(Icons.Default.Info, contentDescription = "Show metadata")
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
            // Main image with zoom and pan
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentGateway)
                    .crossfade(true)
                    .build(),
                contentDescription = decodedName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RectangleShape)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            
                            // Calculate new offset with pan
                            val maxX = (size.width * (scale - 1)) / 2
                            val maxY = (size.height * (scale - 1)) / 2
                            
                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                            
                            // Hide help when user interacts
                            showHelp = false
                        }
                    },
                loading = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Failed to load image",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            
                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.tryNextGateway() }
                            ) {
                                Text("Try Another Gateway")
                            }
                        }
                    }
                }
            )
            
            // Help text overlay - shown briefly when screen is first displayed
            AnimatedVisibility(
                visible = showHelp,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            "Pinch to zoom • Drag to pan",
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Loading indicator for gateway switching
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Metadata panel
            AnimatedVisibility(
                visible = showMetadata,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Image Metadata",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Basic metadata
                            MetadataRow("Name", decodedName)
                            MetadataRow("CID", imageCid)
                            MetadataRow("Gateway", currentGateway.removePrefix("https://").substringBefore("/ipfs/"))
                            
                            // Advanced metadata
                            if (metadata != null) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                MetadataRow("Size", "${metadata.size / 1024} KB")
                                MetadataRow("MIME Type", metadata.mimeType)
                                MetadataRow("Access Count", "${metadata.accessCount}")
                                MetadataRow("Last Accessed", metadata.lastAccessed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                                if (metadata.localPath != null) {
                                    MetadataRow("Cached Locally", "Yes")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Gateway testing button
                            Button(
                                onClick = { viewModel.tryNextGateway() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Test Next Gateway")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f),
            overflow = TextOverflow.Ellipsis
        )
    }
} 