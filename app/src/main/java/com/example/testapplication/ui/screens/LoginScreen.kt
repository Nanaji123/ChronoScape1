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
import com.example.testapplication.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onContinue: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    LoginScreenContent(
        phoneNumber = viewModel.phoneNumber,
        onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
        isContinueEnabled = viewModel.isContinueEnabled,
        onContinueClick = { onContinue(viewModel.phoneNumber) }
    )
}

@Composable
fun LoginScreenContent(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isContinueEnabled: Boolean,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enter your 10-digit phone number", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinueClick,
            enabled = isContinueEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TestApplicationTheme {
        LoginScreenContent(
            phoneNumber = "1234567890",
            onPhoneNumberChange = {},
            isContinueEnabled = true,
            onContinueClick = {}
        )
    }
}
