package com.example.testapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.ui.theme.TestApplicationTheme
import com.example.testapplication.viewmodel.OTPViewModel

@Composable
fun OTPScreen(
    onVerify: () -> Unit,
    viewModel: OTPViewModel = viewModel()
) {
    OTPScreenContent(
        otp = viewModel.otp,
        onOtpChange = { viewModel.onOtpChange(it) },
        isVerifyEnabled = viewModel.isVerifyEnabled,
        onVerifyClick = { if (viewModel.isVerifyEnabled) onVerify() }
    )
}

@Composable
fun OTPScreenContent(
    otp: String,
    onOtpChange: (String) -> Unit,
    isVerifyEnabled: Boolean,
    onVerifyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter the 4-digit OTP", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onVerifyClick,
            enabled = isVerifyEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OTPScreenPreview() {
    TestApplicationTheme {
        OTPScreenContent(
            otp = "1234",
            onOtpChange = {},
            isVerifyEnabled = true,
            onVerifyClick = {}
        )
    }
}
