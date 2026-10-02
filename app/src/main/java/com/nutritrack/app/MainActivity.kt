package com.nutritrack.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.nutritrack.app.ui.NutriTrackApp
import com.nutritrack.app.ui.theme.NutriTrackTheme

// AppCompatActivity (rather than ComponentActivity) so per-app language changes apply on API 26-32.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriTrackTheme {
                NutriTrackApp()
            }
        }
    }
}
