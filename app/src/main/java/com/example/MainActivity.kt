package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.data.FirestoreSim
import com.example.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize our Firestore synchronization listeners and load falling simulated databases.
        FirestoreSim.initialize(applicationContext)

        setContent {
            // Load the main experience dashboard
            MainScreen()
        }
    }
}
