package com.example.pincast.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Gallery : Screen("gallery")
    object Upload : Screen("upload")
    object ImageDetail : Screen("image_detail/{cid}/{name}/{url}") {
        fun createRoute(cid: String, name: String, url: String): String {
            return "image_detail/$cid/$name/$url"
        }
    }
} 