package com.example.testapplication

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val GROWTH_LEVEL = "growth_level"
        private const val GROWTH_DURATION = "growth_duration" // in minutes
        private const val LIFE_START_TIME = "life_start_time"
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

    fun saveGrowthDuration(minutes: Float) {
        prefs.edit().putFloat(GROWTH_DURATION, minutes).apply()
    }

    fun getGrowthDuration(): Float {
        // Default to a very long time (e.g., 2000 days = 2880000 minutes)
        return prefs.getFloat(GROWTH_DURATION, 2880000f)
    }

    fun getLifeStartTime(): Long {
        var startTime = prefs.getLong(LIFE_START_TIME, 0L)
        if (startTime == 0L) {
            // Set start date to Dec 14, 2025 (~130 days before April 22, 2026)
            val calendar = java.util.Calendar.getInstance()
            calendar.set(2025, 11, 14, 0, 0, 0) 
            startTime = calendar.timeInMillis
            prefs.edit().putLong(LIFE_START_TIME, startTime).apply()
        }
        return startTime
    }
}
