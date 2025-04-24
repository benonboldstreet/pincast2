package com.example.pincast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pincast.ui.navigation.Screen
import com.example.pincast.ui.screens.GalleryScreen
import com.example.pincast.ui.screens.HomeScreen
import com.example.pincast.ui.screens.ImageDetailScreen
import com.example.pincast.ui.screens.LoginScreen
import com.example.pincast.ui.screens.UploadScreen
import com.example.pincast.ui.screens.Web3Screen
import com.example.pincast.ui.theme.PincastTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PincastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PincastApp()
                }
            }
        }
    }
}

@Composable
fun PincastApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGallery = { 
                    navController.navigate(Screen.Gallery.route)
                },
                onNavigateToUpload = { navController.navigate(Screen.Upload.route) },
                onLogout = { navController.navigate(Screen.Login.route) }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) }
            )
        }
        
        // Added Gallery screen
        composable(Screen.Gallery.route) {
            GalleryScreen(navController = navController)
        }
        
        composable(Screen.Upload.route) {
            UploadScreen(
                onNavigateBack = { navController.navigateUp() },
                onUploadSuccess = { image ->
                    // Navigate to image detail
                    val encodedName = URLEncoder.encode(image.name, StandardCharsets.UTF_8.toString())
                    val encodedUrl = URLEncoder.encode(image.url, StandardCharsets.UTF_8.toString())
                    navController.navigate(Screen.ImageDetail.createRoute(image.cid, encodedName, encodedUrl))
                }
            )
        }
        
        composable(
            route = Screen.ImageDetail.route,
            arguments = listOf(
                navArgument("cid") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cid = backStackEntry.arguments?.getString("cid")
            val name = backStackEntry.arguments?.getString("name")
            val url = backStackEntry.arguments?.getString("url")
            
            ImageDetailScreen(
                cid = cid,
                name = name,
                url = url,
                navController = navController
            )
        }
        
        // Additional screen for Web3 features
        composable("web3") {
            Web3Screen(navController = navController)
        }
    }
}