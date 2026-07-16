package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AkbarMediaNavGraph
import com.example.ui.theme.AppTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Density

import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    com.example.ui.data.remote.ApiClient.init(applicationContext)
    com.example.ui.data.remote.NetworkMonitor.init(applicationContext)
    com.example.ui.data.SettingsManager.init(applicationContext)
    enableEdgeToEdge()
    setContent {
         val themeSetting = com.example.ui.data.SettingsManager.themeStateFlow.collectAsState().value
         val darkTheme = when (themeSetting) {
             "Tema Gelap" -> true
             "Tema Terang" -> false
             else -> androidx.compose.foundation.isSystemInDarkTheme()
         }
         val configuration = LocalConfiguration.current
         val density = LocalDensity.current
         
         // Sesuaikan skala tampilan untuk menghindari terlalu besar di HP tertentu
         // Sesuaikan skala untuk mengatasi tampilan kebesaran
         val baseWidthDp = 411f
         val scaleFactor = (configuration.screenWidthDp.toFloat() / baseWidthDp).coerceAtMost(0.9f)
         val customDensity = Density(
             density = density.density * scaleFactor,
             fontScale = 0.95f // Batasi skala font
         )
                  
         CompositionLocalProvider(LocalDensity provides customDensity) {
             AppTheme(darkTheme = darkTheme) { 
                 AkbarMediaNavGraph() 
             } 
         }
     }
  }
}

