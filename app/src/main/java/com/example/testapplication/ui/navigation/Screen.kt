package com.example.testapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Login : Screen("login", "Login", null)
    object OTP : Screen("otp", "OTP", null)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
}
