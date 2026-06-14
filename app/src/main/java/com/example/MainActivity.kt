package com.example

import android.os.Bundle
import android.os.Build
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SystemMainApp
import com.example.ui.SystemViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure edge-to-edge styling is configured cleanly
        enableEdgeToEdge()

        // Ask for runtime notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Set high performance system refresh rate to Android preferences
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display
                if (display != null) {
                    val supportedModes = display.supportedModes
                    // Find a mode with the highest refresh rate matching system capabilities
                    val highestRefreshRateMode = supportedModes.maxByOrNull { it.refreshRate }
                    if (highestRefreshRateMode != null) {
                        val params = window.attributes
                        params.preferredDisplayModeId = highestRefreshRateMode.modeId
                        window.attributes = params
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                // Initialize main Solo Leveling system state provider
                val viewModel: SystemViewModel = viewModel()
                
                Box(modifier = Modifier.fillMaxSize()) {
                    SystemMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
