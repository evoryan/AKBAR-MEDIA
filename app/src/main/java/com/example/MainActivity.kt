package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.AkbarMediaNavGraph
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.ui.data.remote.ApiClient.init(applicationContext)
    com.example.ui.data.SettingsManager.init(applicationContext)
    enableEdgeToEdge()
    setContent { 
        AppTheme { 
            AkbarMediaNavGraph() 
        } 
    }
  }
}

