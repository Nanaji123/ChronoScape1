package com.example.testapplication.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.testapplication.SessionManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val handler = Handler(Looper.getMainLooper())

    var growthLevel by mutableFloatStateOf(sessionManager.getGrowth())
        private set

    /** Target growth duration in minutes */
    var growthDurationMinutes by mutableFloatStateOf(sessionManager.getGrowthDuration())
        private set

    val currentLifeDay: Int
        get() {
            // Calculate days since start date (midnight to midnight)
            val startMillis = sessionManager.getLifeStartTime()
            val startDate = LocalDate.ofEpochDay(startMillis / (1000 * 60 * 60 * 24))
            val today = LocalDate.now()
            return ChronoUnit.DAYS.between(startDate, today).toInt() + 1
        }

    val totalLifeDays = 360

    val welcomeMessage = "World Tree"

    private val refreshRunnable =
            object : Runnable {
                override fun run() {
                    growthLevel = sessionManager.getGrowth()
                    handler.postDelayed(this, 100)
                }
            }

    init {
        handler.post(refreshRunnable)
    }

    fun waterPlant() {
        val inc = 100f
        val new = (growthLevel + inc).coerceAtMost(2000f)
        growthLevel = new
        sessionManager.saveGrowth(new)
    }

    /** Decrease growth by a chunk (simulate pruning) */
    fun shrinkGrowth() {
        val new = (growthLevel - 500f).coerceAtLeast(10f)
        growthLevel = new
        sessionManager.saveGrowth(new)
    }

    fun resetGrowth() {
        growthLevel = 10f
        sessionManager.saveGrowth(10f)
    }

    fun setGrowthDuration(minutes: Float) {
        growthDurationMinutes = minutes
        sessionManager.saveGrowthDuration(minutes)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(refreshRunnable)
    }
}
