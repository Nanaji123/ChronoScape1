package com.example.testapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.ui.theme.TestApplicationTheme
import com.example.testapplication.viewmodel.SettingsViewModel

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    SettingsPageContent(
        modifier = modifier,
        title = viewModel.title
    )
}

@Composable
fun SettingsPageContent(
    modifier: Modifier = Modifier,
    title: String
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPagePreview() {
    TestApplicationTheme {
        SettingsPageContent(title = "Settings (Preview)")
    }
}
