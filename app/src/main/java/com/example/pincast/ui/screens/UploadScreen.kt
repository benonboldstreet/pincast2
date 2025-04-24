package com.example.pincast.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pincast.data.models.Image
import com.example.pincast.ui.viewmodels.UploadViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit,
    onUploadSuccess: (Image) -> Unit,
    uploadViewModel: UploadViewModel = viewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Request permission
    val readExternalStoragePermission = when {
        android.os.Build.VERSION.SDK_INT >= 33 -> rememberPermissionState(
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
        else -> rememberPermissionState(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    
    // Image picker launcher
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }
    
    LaunchedEffect(uploadViewModel) {
        uploadViewModel.uploadState.collect { state ->
            when(state) {
                is UploadViewModel.UploadState.Loading -> {
                    uploading = true
                    errorMessage = null
                    uploadSuccess = false
                }
                is UploadViewModel.UploadState.Success -> {
                    uploading = false
                    uploadSuccess = true
                    errorMessage = null
                    // Call onUploadSuccess callback with the uploaded image
                    onUploadSuccess(state.image)
                }
                is UploadViewModel.UploadState.Error -> {
                    uploading = false
                    uploadSuccess = false
                    errorMessage = state.message
                }
                is UploadViewModel.UploadState.Idle -> {
                    uploading = false
                    errorMessage = null
                    uploadSuccess = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Image") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selected image preview
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("No image selected")
                }
            }
            
            // Select image button
            Button(
                onClick = {
                    if (!readExternalStoragePermission.status.isGranted) {
                        readExternalStoragePermission.launchPermissionRequest()
                    } else {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Image")
            }
            
            // Upload button
            Button(
                onClick = {
                    selectedImageUri?.let {
                        uploadViewModel.uploadImage(it)
                    } ?: run {
                        errorMessage = "Please select an image first"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageUri != null && !uploading
            ) {
                if (uploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Upload Image")
                }
            }
            
            // Status messages
            when {
                errorMessage != null -> {
                    Text(
                        errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Add suggestion for 404 error
                    if (errorMessage!!.contains("404") || errorMessage!!.contains("host")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "API connection issue detected. Please check the API endpoint configuration.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
} 