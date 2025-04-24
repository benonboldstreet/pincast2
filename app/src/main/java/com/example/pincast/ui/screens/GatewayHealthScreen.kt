package com.example.pincast.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pincast.data.models.GatewayInfo
import com.example.pincast.data.models.GatewayStatus
import com.example.pincast.ui.viewmodels.GatewayHealthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayHealthScreen(
    navController: NavController,
    viewModel: GatewayHealthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gateway Health") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAllGateways() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh All")
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
            if (uiState.isLoading && uiState.gatewayStats.isEmpty()) {
                // Initial loading
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Testing IPFS Gateways...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Gateway Health Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val availableCount = uiState.gatewayStats.count { it.status == GatewayStatus.ONLINE }
                            val totalCount = uiState.gatewayStats.size
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Available Gateways")
                                Text(
                                    "$availableCount / $totalCount",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fastest Gateway")
                                val fastest = uiState.gatewayStats
                                    .filter { it.status == GatewayStatus.ONLINE }
                                    .minByOrNull { it.responseTimeMs }
                                
                                if (fastest != null) {
                                    Text(
                                        fastest.name.removePrefix("https://").removeSuffix("/ipfs/"),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text("None available")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            if (uiState.isLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    
                    // Fastest response time
                    Text(
                        "Response Time Colors",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fast (< 1s)")
                    }
                    
                    // Gateway list
                    Text(
                        "Gateway Status",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.gatewayStats.sortedBy { it.status }) { gateway ->
                            GatewayStatusCard(
                                gateway = gateway,
                                onRefresh = { viewModel.refreshGateway(gateway.url) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GatewayStatusCard(
    gateway: GatewayInfo,
    onRefresh: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (gateway.status) {
                                    GatewayStatus.ONLINE -> Color(0xFF4CAF50)  // Green
                                    GatewayStatus.OFFLINE -> Color(0xFFF44336) // Red
                                    GatewayStatus.SLOW -> Color(0xFFFF9800)    // Orange
                                    GatewayStatus.UNKNOWN -> Color(0xFF9E9E9E) // Gray
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        gateway.name.removePrefix("https://").removeSuffix("/ipfs/"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (gateway.status == GatewayStatus.ONLINE) {
                        Text(
                            "${gateway.responseTimeMs} ms",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Show less" else "Show more",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Gateway Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Full URL: ${gateway.url}")
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("Status: ${gateway.status}")
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (gateway.status == GatewayStatus.ONLINE) {
                        Text("Response Time: ${gateway.responseTimeMs} ms")
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Response time quality
                        val responseQuality = when {
                            gateway.responseTimeMs < 200 -> "Excellent"
                            gateway.responseTimeMs < 500 -> "Good"
                            gateway.responseTimeMs < 1000 -> "Fair"
                            else -> "Poor"
                        }
                        
                        Text("Response Quality: $responseQuality")
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (gateway.lastTestedAt != null) {
                        Text("Last Checked: ${gateway.lastTestedAt}")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Test button
                    Button(
                        onClick = onRefresh,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Gateway")
                    }
                }
            }
        }
    }
} 