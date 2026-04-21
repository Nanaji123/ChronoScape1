package com.example.testapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var phoneNumber by mutableStateOf("")
        private set

    fun onPhoneNumberChange(newValue: String) {
        if (newValue.length <= 10) {
            phoneNumber = newValue
        }
    }

    val isContinueEnabled: Boolean
        get() = phoneNumber.length == 10
}
