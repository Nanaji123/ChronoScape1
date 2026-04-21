package com.example.testapplication.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.testapplication.SessionManager

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val handler = Handler(Looper.getMainLooper())

    var growthLevel by mutableFloatStateOf(sessionManager.getGrowth())
        private set

    /** Growth speed multiplier: 0.5x … 3x, default 1x */
    var growthSpeed by mutableFloatStateOf(1f)

    val welcomeMessage = "World Tree"

    private val refreshRunnable = object : Runnable {
        override fun run() {
            growthLevel = sessionManager.getGrowth()
            handler.postDelayed(this, 100)
        }
    }

    init {
        handler.post(refreshRunnable)
    }

    fun waterPlant() {
        val inc = 100f * growthSpeed
        val new = (growthLevel + inc).coerceAtMost(2000f)
        growthLevel = new
        sessionManager.saveGrowth(new)
    }

    /** Jump growth by a big chunk (simulate fast-forward) */
    fun fastGrow() {
        val new = (growthLevel + 500f).coerceAtMost(2000f)
        growthLevel = new
        sessionManager.saveGrowth(new)
    }

    fun resetGrowth() {
        growthLevel = 10f
        sessionManager.saveGrowth(10f)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(refreshRunnable)
    }
}
