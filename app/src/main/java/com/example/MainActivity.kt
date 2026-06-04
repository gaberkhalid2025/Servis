package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.FirestoreSim
import com.example.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // Initialize persistent offline-first simulation db
        FirestoreSim.init(this)

        setContent {
            // Listen to real-time custom configuration streams
            val configsState = FirestoreSim.configs.collectAsState()
            val configs = configsState.value

            // Safe parsing of custom configurations saved in database
            val primaryColor = parseHexColor(configs.primaryHex, Color(0xFF0284C7))
            val secondaryColor = parseHexColor(configs.secondaryHex, Color(0xFFEF4444))

            // Build dynamic high-contrast material color schemes
            val customColorScheme = darkColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                background = Color(0xFF111827), // Cosmic Dark Background
                surface = Color(0xFF1F2937),    // Slate card background
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFFF3F4F6),
                onSurface = Color(0xFFF3F4F6)
            )

            MaterialTheme(
                colorScheme = customColorScheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun parseHexColor(hex: String, defaultColor: Color): Color {
        return try {
            val cleanHex = hex.trim().replace("#", "")
            if (cleanHex.length == 6) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else if (cleanHex.length == 8) {
                Color(android.graphics.Color.parseColor("#$cleanHex"))
            } else {
                defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }
}
