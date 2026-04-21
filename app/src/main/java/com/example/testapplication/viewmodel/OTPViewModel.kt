package com.example.testapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class OTPViewModel : ViewModel() {
    var otp by mutableStateOf("")
        private set

    fun onOtpChange(newValue: String) {
        if (newValue.length <= 4) {
            otp = newValue
        }
    }

    val isVerifyEnabled: Boolean
        get() = otp.length == 4
}
