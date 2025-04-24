package com.example.pincast.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Gallery : Screen("gallery")
    object Upload : Screen("upload")
    object Web3 : Screen("web3")
    object About : Screen("about")
    
    object ImageDetail : Screen("image_detail/{cid}/{name}/{url}") {
        fun createRoute(cid: String, name: String, url: String): String {
            return "image_detail/$cid/$name/$url"
        }
    }
    
    object CollectionDetail : Screen("collection_detail/{id}") {
        fun createRoute(id: String): String {
            return "collection_detail/$id"
        }
    }
} 