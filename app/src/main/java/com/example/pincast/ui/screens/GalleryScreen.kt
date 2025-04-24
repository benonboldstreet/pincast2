package com.example.pincast.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pincast.data.models.Image
import com.example.pincast.ui.components.SimpleImageCard
import com.example.pincast.ui.navigation.Screen
import com.example.pincast.ui.viewmodels.GalleryViewModel
import com.example.pincast.utils.toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<Image?>(null) }
    
    // Load images when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }
    
    // Error handling
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            context.toast(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh action
                    IconButton(onClick = { viewModel.loadImages() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.images.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No images found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Upload an image to get started",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Image grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.images) { image ->
                        SimpleImageCard(
                            image = image.toSimpleImage(),
                            onImageClick = {
                                // Navigate to image detail
                                val encodedName = URLEncoder.encode(image.name, StandardCharsets.UTF_8.toString())
                                val encodedUrl = URLEncoder.encode(image.url, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.ImageDetail.createRoute(image.cid, encodedName, encodedUrl))
                            },
                            onShareClick = {
                                selectedImage = image
                                showShareDialog = true
                            },
                            onDeleteClick = {
                                viewModel.deleteImage(image)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Share dialog
    if (showShareDialog && selectedImage != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Image") },
            text = { 
                Column {
                    Text("Share this image via:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Copy URL button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(selectedImage!!.url))
                            context.toast("URL copied to clipboard")
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy URL to clipboard")
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Extension function to convert Image to SimpleImage
 */
private fun Image.toSimpleImage(): com.example.pincast.ui.components.SimpleImage {
    return com.example.pincast.ui.components.SimpleImage(
        id = this.id,
        name = this.name,
        cid = this.cid,
        url = this.url
    )
} 