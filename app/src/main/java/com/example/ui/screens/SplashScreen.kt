package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.Image
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(key1 = true) {
        delay(2000L) // Show splash for 2 seconds
        val isLoggedIn = com.example.ui.data.UserSession.loadSession(context)
        if (isLoggedIn) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }

    val neonCyan = Color(0xFF00FFFF)
    val onPrimaryContainer = Color(0xFFFFFFFF)
    val bgMain = Color(0xFF0A0A0A)
    val headerBg = Color(0xFF1F0216)
    val bgCard = Color(0xFF121212)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgMain),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_splash),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Akbar Media",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Billing System v2.4",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}
