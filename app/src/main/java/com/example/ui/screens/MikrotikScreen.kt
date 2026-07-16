package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MikrotikScreen(
    onBack: () -> Unit,
    onNavigateToManageSecrets: (String) -> Unit = {}
) {
    
    var areas by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
            areas = res.filter { it.routerIp != null && it.routerIp.isNotEmpty() }
        } catch(e: Exception) {
        }
    }

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = bgMain,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00FFFF),
                contentColor = Color.Black,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Mikrotik")
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Status Mikrotik", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(areas) { area ->
                    MikrotikCard(area = area, onNavigateToManageSecrets = { onNavigateToManageSecrets(area.id) })
                }
            }
        }
        
        if (showAddDialog) {
            AreaFormDialog(
                initialArea = null,
                onDismiss = { showAddDialog = false },
                onSave = { newArea ->
                    coroutineScope.launch {
                        try {
                            com.example.ui.data.remote.ApiClient.apiService.addArea(newArea)
                            val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
                            areas = res.filter { it.routerIp != null && it.routerIp.isNotEmpty() }
                        } catch(e: Exception) {}
                    }
                    showAddDialog = false
                },
                bgMain = bgMain,
                textMain = textMain,
                textSecondary = Color(0xFFAAAAAA),
                primaryPurple = Color(0xFF2B0B3F),
                neonCyan = Color(0xFF00FFFF),
                cardBg = cardBg
            )
        }
    }
}

@Composable
fun MikrotikCard(area: Area, onNavigateToManageSecrets: () -> Unit) {
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val cardBorder = neonCyan.copy(alpha = 0.3f)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    
    var mikrotikStatus by remember { mutableStateOf<com.example.ui.data.remote.MikrotikStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(area.id) {
        try {
            isLoading = true
            errorMsg = null
            mikrotikStatus = com.example.ui.data.remote.ApiClient.apiService.getMikrotikStatus(area.id)
        } catch(e: Exception) {
            errorMsg = "Gagal mengambil data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(48.dp), tint = neonCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if (isLoading) "Connecting to ${area.routerIp}..." else if (errorMsg != null) "Error" else "Connected (${area.routerIp})", color = if (errorMsg != null) Color(0xFFFF003C) else neonCyan, fontSize = 14.sp)
                    }
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = neonCyan)
                }
            } else if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFFF003C), fontSize = 14.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MikrotikStatCard("Active PPPoE", mikrotikStatus?.activePppoe ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                    MikrotikStatCard("PPPoE Offline", mikrotikStatus?.offlinePppoe ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Button(
                onClick = onNavigateToManageSecrets, 
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = neonCyan),
                border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Secrets", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MikrotikStatCard(title: String, value: String, neonCyan: Color, textMain: Color, textSecondary: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF05050A))
            .border(1.dp, neonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = textSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = textMain, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
