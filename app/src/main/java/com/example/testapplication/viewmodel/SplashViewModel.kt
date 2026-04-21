package com.example.testapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    val quotes = listOf(
        "Believe in yourself!",
        "Keep pushing forward.",
        "Your potential is limitless."
    )
    
    var currentQuoteIndex by mutableIntStateOf(0)
        private set

    fun startTimer(onTimeout: () -> Unit) {
        viewModelScope.launch {
            for (i in 0 until 3) {
                currentQuoteIndex = i
                delay(1000)
            }
            onTimeout()
        }
    }
}
