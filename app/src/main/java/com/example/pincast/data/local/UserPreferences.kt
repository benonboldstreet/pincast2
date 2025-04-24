package com.example.pincast.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.pincast.data.model.User

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    fun saveUser(user: User) {
        sharedPreferences.edit()
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_PASSWORD, user.password)
            .apply()
    }
    
    fun getUser(): User? {
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val password = sharedPreferences.getString(KEY_PASSWORD, null)
        
        return if (email != null && password != null) {
            User(email, password)
        } else {
            null
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains(KEY_EMAIL)
    }
    
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        private const val PREFS_NAME = "pincast_preferences"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }
} 