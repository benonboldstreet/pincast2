package com.example.pincast.data.model

data class User(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String
) 