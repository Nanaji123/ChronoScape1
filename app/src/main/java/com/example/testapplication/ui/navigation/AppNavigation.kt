package com.example.testapplication.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testapplication.SessionManager
import com.example.testapplication.ui.components.BottomNavigationBar
import com.example.testapplication.ui.screens.*

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define which screens should show the bottom bar
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Dashboard.route,
        Screen.Settings.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onTimeout = {
                    if (sessionManager.isLoggedIn()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(onContinue = { phoneNumber ->
                    navController.navigate(Screen.OTP.route)
                })
            }
            composable(Screen.OTP.route) {
                OTPScreen(onVerify = {
                    sessionManager.setLoggedIn(true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) { HomePage() }
            composable(Screen.Dashboard.route) { DashboardPage() }
            composable(Screen.Settings.route) { SettingsPage() }
            composable(Screen.Profile.route) {
                ProfilePage(onLogout = {
                    sessionManager.setLoggedIn(false)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
