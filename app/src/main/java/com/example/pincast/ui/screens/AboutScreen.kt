package com.example.pincast.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pincast.R

// Fallback for BuildConfig if it's not generated
private object MockBuildConfig {
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Pincast") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.ic_pincast_logo),
                contentDescription = "Pincast Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )
            
            // App name and version
            Text(
                text = "Pincast2",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Version ${MockBuildConfig.VERSION_NAME} (${MockBuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // App description
            AboutSectionTitle(title = "Overview")
            DescriptionText(
                text = "Pincast2 is a modern Android application designed to revolutionize media management through decentralized storage with IPFS integration via the Jackal Protocol."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AboutSectionTitle(title = "Core Features")
            
            FeatureItem(
                title = "Decentralized Storage",
                description = "Store your media securely on IPFS through the Jackal Protocol, ensuring your files remain accessible and resilient."
            )
            
            FeatureItem(
                title = "Intelligent Caching",
                description = "Our layered approach combines local caching with decentralized storage for optimal performance."
            )
            
            FeatureItem(
                title = "Advanced Media Management",
                description = "Upload, organize, browse, and view your media with a responsive gallery interface and detailed view capabilities."
            )
            
            FeatureItem(
                title = "Robust Sharing",
                description = "Share IPFS links to your content, copy media links to clipboard, or share directly to other apps."
            )
            
            FeatureItem(
                title = "Gateway Resilience",
                description = "Automatically switch between multiple IPFS gateways to ensure your content is always accessible."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Credits section
            AboutSectionTitle(title = "Credits")
            DescriptionText(
                text = "Developed with ❤️ using Kotlin, Jetpack Compose, and modern Android architecture components."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add link to GitHub repo if applicable
            Button(
                onClick = { /* Open GitHub link */ },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("View on GitHub")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AboutSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DescriptionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
fun FeatureItem(title: String, description: String) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 