package com.example.pincast.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pincast.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cache Settings Section
            SectionTitle(title = "Cache Settings")
            
            // Cache Size Slider
            CacheSizePreference(
                currentValue = uiState.maxCacheSizeMb,
                onValueChange = { viewModel.updateMaxCacheSize(it) }
            )
            
            // Auto-cleaning toggle
            SwitchPreference(
                title = "Auto-clean cache",
                description = "Automatically prune cache when app starts",
                checked = uiState.autoCleanCache,
                onCheckedChange = { viewModel.updateAutoCleanCache(it) }
            )
            
            // Pre-caching toggle
            SwitchPreference(
                title = "Pre-cache on WiFi",
                description = "Automatically download content when on WiFi",
                checked = uiState.preCacheOnWifi,
                onCheckedChange = { viewModel.updatePreCacheOnWifi(it) }
            )
            
            // Keep favorites toggle
            SwitchPreference(
                title = "Keep favorites cached",
                description = "Don't auto-clean favorites",
                checked = uiState.keepFavoritesCached,
                onCheckedChange = { viewModel.updateKeepFavoritesCached(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // IPFS Gateway Settings
            SectionTitle(title = "IPFS Gateway Settings")
            
            // Primary Gateway
            DropdownPreference(
                title = "Primary Gateway",
                options = uiState.availableGateways,
                selectedOption = uiState.primaryGateway,
                onOptionSelected = { viewModel.updatePrimaryGateway(it) }
            )
            
            // Gateway Performance
            if (uiState.gatewayStats.isNotEmpty()) {
                Text(
                    text = "Gateway Performance",
                    style = MaterialTheme.typography.titleMedium
                )
                
                uiState.gatewayStats.forEach { (gateway, time) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = gateway.removePrefix("https://").removeSuffix("/ipfs/"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${time}ms",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Cache Management
            SectionTitle(title = "Cache Management")
            
            // Current cache usage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Current Cache Usage")
                Text("${uiState.currentCacheSizeMb} MB")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cached Items")
                Text("${uiState.cachedItemCount}")
            }
            
            // Clear cache button
            Button(
                onClick = { showClearCacheDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Cache")
            }
        }
    }
    
    // Confirmation dialog for clearing cache
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache?") },
            text = { 
                Text("This will remove all cached files except favorites. Are you sure you want to continue?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCache()
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SwitchPreference(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun CacheSizePreference(
    currentValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Maximum Cache Size",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$currentValue MB",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Slider(
            value = currentValue.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 50f..500f,
            steps = 9,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownPreference(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
} 