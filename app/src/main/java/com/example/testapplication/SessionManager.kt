package com.example.testapplication

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val GROWTH_LEVEL = "growth_level"
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun saveGrowth(growth: Float) {
        prefs.edit().putFloat(GROWTH_LEVEL, growth).apply()
    }

    fun getGrowth(): Float {
        return prefs.getFloat(GROWTH_LEVEL, 10f) // Default growth 10f
    }
}
