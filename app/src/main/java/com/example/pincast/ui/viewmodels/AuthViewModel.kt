package com.example.pincast.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    // We'll simulate login status instead of using repository
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        // For demo purposes, always start as logged out
        _isLoggedIn.value = false
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Simulate successful login if credentials are provided
            _isLoggedIn.value = email.isNotEmpty() && password.isNotEmpty()
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }
} 