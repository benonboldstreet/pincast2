package com.example.pincast.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pincast.ui.components.SimpleImage
import com.example.pincast.ui.viewmodels.Web3ViewModel
import com.example.pincast.web3.WalletConnect
import com.example.pincast.utils.toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Web3Screen(
    navController: NavController,
    viewModel: Web3ViewModel = Web3ViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showMetadataDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<SimpleImage?>(null) }
    var generatedMetadata by remember { mutableStateOf("") }
    
    // We won't call loadImages() here as it's not defined in our simplified ViewModel
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Web3 Tools") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (WalletConnect.isWalletConnected()) {
                        IconButton(onClick = { 
                            WalletConnect.disconnectWallet()
                            viewModel.updateWalletState()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Disconnect Wallet")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Connection Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Wallet Connection",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (uiState.isWalletConnected) {
                            Text(
                                text = "Connected: ${uiState.walletAddress?.take(10)}...${uiState.walletAddress?.takeLast(6)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = { 
                                    WalletConnect.disconnectWallet()
                                    viewModel.updateWalletState()
                                }
                            ) {
                                Text("Disconnect")
                            }
                        } else {
                            Text(
                                text = "Connect your wallet to access Web3 features",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { 
                                    WalletConnect.connectKeplrWallet(context)
                                    // In a real implementation, we would wait for a callback
                                    // For demo purposes, we're simulating connection
                                    WalletConnect.setWalletConnection(
                                        "jkl1x8u42p9vz30zfzhtv33ap2h52gw2fgj44xpkh", 
                                        "jackal-1"
                                    )
                                    viewModel.updateWalletState()
                                }
                            ) {
                                Text("Connect Keplr Wallet")
                            }
                        }
                    }
                }
            }
            
            // NFT Creation Tools - only show if wallet is connected
            if (uiState.isWalletConnected) {
                item {
                    Text(
                        text = "NFT Creation Tools",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Generate NFT Metadata",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Create standard NFT metadata JSON for any of your images",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showMetadataDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Generate Metadata")
                            }
                        }
                    }
                }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Mint on Stargaze",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Mint your collections as NFTs on Stargaze marketplace",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    val mintUrl = WalletConnect.generateStargazeMintLink(
                                        uiState.collections.firstOrNull()?.cid ?: ""
                                    )
                                    
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(mintUrl)
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.collections.isNotEmpty()
                            ) {
                                Text("Open Stargaze")
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "Your Images",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                items(uiState.images) { image ->
                    ListItem(
                        headlineContent = { Text(image.name) },
                        supportingContent = { Text("CID: ${image.cid.take(15)}...") },
                        leadingContent = { 
                            Icon(Icons.Default.Check, contentDescription = null)
                        },
                        trailingContent = {
                            TextButton(onClick = {
                                selectedImage = image
                                generatedMetadata = WalletConnect.generateNftMetadata(
                                    name = image.name,
                                    description = "Created with PinCast using Jackal Protocol",
                                    imageCid = image.cid
                                )
                                showMetadataDialog = true 
                            }) {
                                Text("Generate")
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
    
    // Metadata dialog
    if (showMetadataDialog && selectedImage != null) {
        AlertDialog(
            onDismissRequest = { showMetadataDialog = false },
            title = { Text("NFT Metadata") },
            text = { 
                Column {
                    Text("Generated metadata for ${selectedImage?.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = generatedMetadata,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(generatedMetadata))
                        showMetadataDialog = false
                        // Show toast
                        context.toast("Metadata copied to clipboard")
                    }
                ) {
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMetadataDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
} 