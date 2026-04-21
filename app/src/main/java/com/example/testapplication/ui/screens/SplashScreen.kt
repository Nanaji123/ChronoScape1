package com.example.testapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.ui.theme.TestApplicationTheme
import com.example.testapplication.viewmodel.SplashViewModel

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startTimer(onTimeout)
    }

    SplashScreenContent(
        quote = viewModel.quotes[viewModel.currentQuoteIndex],
        onSkipClick = onTimeout
    )
}

@Composable
fun SplashScreenContent(
    quote: String,
    onSkipClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = quote,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onSkipClick) {
                Text("Skip")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TestApplicationTheme {
        SplashScreenContent(
            quote = "Success is not final, failure is not fatal.",
            onSkipClick = {}
        )
    }
}
