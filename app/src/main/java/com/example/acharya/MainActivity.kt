package com.example.acharya// Keep YOUR original package name here!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Using the standard Material 3 Theme.
            // Note: Android Studio might have generated a custom theme for you
            // (like "MyAppTheme"). You can use either!
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This launches the chat interface we built in ChatScreen.kt
                    ChatScreen()
                }
            }
        }
    }
}