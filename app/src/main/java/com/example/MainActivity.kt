package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AkbarMediaNavGraph
import com.example.ui.theme.AppTheme
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
         AppTheme(darkTheme = darkTheme) {
             AkbarMediaNavGraph()
         }
     }
  }
}

