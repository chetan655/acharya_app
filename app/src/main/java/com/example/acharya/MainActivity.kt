package com.example.acharya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemTheme) }
            val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Creates ONE viewmodel to be shared across screens
                    val sharedViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                    NavHost(navController = navController, startDestination = "chat") {

                        composable("chat") {
                            ChatScreen(
                                navController = navController,
                                viewModel = sharedViewModel,
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = !isDarkTheme }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("scanner") {
                            ScannerScreen(
                                navController = navController,
                                viewModel = sharedViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}