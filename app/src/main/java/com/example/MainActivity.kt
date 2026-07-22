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
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit

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

    // Schedule background WorkManager Sync (Immediate startup sync + Periodic background sync)
    try {
        val workManager = WorkManager.getInstance(applicationContext)
        val oneTimeSync = OneTimeWorkRequestBuilder<com.example.ui.data.local.SyncWorker>().build()
        workManager.enqueue(oneTimeSync)

        val periodicSync = PeriodicWorkRequestBuilder<com.example.ui.data.local.SyncWorker>(
            15, TimeUnit.MINUTES
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "AkbarMediaSync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSync
        )
    } catch (e: Throwable) {
        android.util.Log.e("MainActivity", "Failed to schedule SyncWorker", e)
    }

    enableEdgeToEdge()
    setContent {
         val themeSetting = com.example.ui.data.SettingsManager.themeStateFlow.collectAsState().value
         val fontScaleSetting = com.example.ui.data.SettingsManager.fontScaleStateFlow.collectAsState().value
         val darkTheme = when (themeSetting) {
             "Tema Gelap" -> true
             "Tema Terang" -> false
             else -> androidx.compose.foundation.isSystemInDarkTheme()
         }
         val fontScaleMultiplier = when (fontScaleSetting) {
             "Kecil" -> 0.95f
             "Sedang" -> 1.15f
             "Besar" -> 1.35f
             else -> 0.95f
         }
         val configuration = LocalConfiguration.current
         val density = LocalDensity.current
         
         // Sesuaikan skala tampilan untuk menghindari terlalu besar di HP tertentu
         // Sesuaikan skala untuk mengatasi tampilan kebesaran
         val baseWidthDp = 411f
         val scaleFactor = (configuration.screenWidthDp.toFloat() / baseWidthDp).coerceAtMost(0.9f)
         val customDensity = Density(
             density = density.density * scaleFactor,
             fontScale = fontScaleMultiplier
         )
                  
         CompositionLocalProvider(LocalDensity provides customDensity) {
             AppTheme(darkTheme = darkTheme) { 
                 AkbarMediaNavGraph() 
             } 
         }
     }
  }
}

