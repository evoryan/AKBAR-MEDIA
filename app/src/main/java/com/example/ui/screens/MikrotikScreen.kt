package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
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
    val statuses = remember { mutableStateMapOf<String, com.example.ui.data.remote.MikrotikStatus?>() }
    val isStatusesLoading = remember { mutableStateMapOf<String, Boolean>() }
    val statusErrors = remember { mutableStateMapOf<String, String?>() }

    LaunchedEffect(Unit) {
        try {
            val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
            val filteredAreas = res.filter { it.routerIp != null && it.routerIp.isNotEmpty() && com.example.ui.data.UserSession.isAreaIdAllowed(it.id) }
            areas = filteredAreas
            
            // Mark as loading
            filteredAreas.forEach { area ->
                isStatusesLoading[area.id] = true
                statusErrors[area.id] = null
            }
            
            // Fetch statuses in parallel using MikrotikRepository
            val parallelResults = com.example.ui.data.remote.MikrotikRepository.getMikrotikStatusesInParallel(filteredAreas)
            
            parallelResults.forEach { (areaId, result) ->
                isStatusesLoading[areaId] = false
                result.fold(
                    onSuccess = { status ->
                        statuses[areaId] = status
                    },
                    onFailure = { error ->
                        statusErrors[areaId] = "Gagal mengambil data: ${error.message}"
                    }
                )
            }
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
                    MikrotikCard(
                        area = area,
                        preloadedStatus = statuses[area.id],
                        preloadedIsLoading = isStatusesLoading[area.id],
                        preloadedErrorMsg = statusErrors[area.id],
                        onNavigateToManageSecrets = { onNavigateToManageSecrets(area.id) }
                    )
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
                            val filteredAreas = res.filter { it.routerIp != null && it.routerIp.isNotEmpty() }
                            areas = filteredAreas
                            
                            // Load all in parallel
                            filteredAreas.forEach { area ->
                                isStatusesLoading[area.id] = true
                                statusErrors[area.id] = null
                            }
                            
                            val parallelResults = com.example.ui.data.remote.MikrotikRepository.getMikrotikStatusesInParallel(filteredAreas)
                            parallelResults.forEach { (areaId, result) ->
                                isStatusesLoading[areaId] = false
                                result.fold(
                                    onSuccess = { status ->
                                        statuses[areaId] = status
                                    },
                                    onFailure = { error ->
                                        statusErrors[areaId] = "Gagal mengambil data: ${error.message}"
                                    }
                                )
                            }
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
fun MikrotikCard(
    area: Area,
    preloadedStatus: com.example.ui.data.remote.MikrotikStatus? = null,
    preloadedIsLoading: Boolean? = null,
    preloadedErrorMsg: String? = null,
    onNavigateToManageSecrets: () -> Unit
) {
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val cardBorder = neonCyan.copy(alpha = 0.3f)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    
    var mikrotikStatus by remember { mutableStateOf<com.example.ui.data.remote.MikrotikStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    // Traffic Counter states
    var selectedPortChangedTrigger by remember { mutableStateOf(0) }
    val selectedPort = remember(area.id, selectedPortChangedTrigger) { com.example.ui.data.SettingsManager.getSelectedPort(area.id) }
    
    var currentRxBytes by remember { mutableStateOf(0L) }
    var currentTxBytes by remember { mutableStateOf(0L) }
    var accumulatedRxBytes by remember { mutableStateOf(0L) }
    var accumulatedTxBytes by remember { mutableStateOf(0L) }
    var trafficErrorMsg by remember { mutableStateOf<String?>(null) }
    
    var showPortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(area.id, preloadedStatus, preloadedIsLoading, preloadedErrorMsg, selectedPortChangedTrigger) {
        // Initial setup of accumulated bytes from SharedPreferences
        accumulatedRxBytes = com.example.ui.data.SettingsManager.getTrafficAccumRx(area.id)
        accumulatedTxBytes = com.example.ui.data.SettingsManager.getTrafficAccumTx(area.id)
        currentRxBytes = com.example.ui.data.SettingsManager.getTrafficLastRx(area.id)
        currentTxBytes = com.example.ui.data.SettingsManager.getTrafficLastTx(area.id)

        if (preloadedIsLoading != null) {
            isLoading = preloadedIsLoading
            mikrotikStatus = preloadedStatus
            errorMsg = preloadedErrorMsg
        } else {
            isLoading = true
            errorMsg = null
        }
        
        // 1. Fetch main Mikrotik Status (PPPoE active/offline/cpu)
        try {
            if (preloadedIsLoading == null) {
                mikrotikStatus = com.example.ui.data.remote.ApiClient.apiService.getMikrotikStatus(area.id)
            }
        } catch(e: Exception) {
            errorMsg = "Gagal mengambil status: ${e.message}"
        } finally {
            if (preloadedIsLoading == null) {
                isLoading = false
            }
        }

        // 2. Fetch traffic interfaces in an isolated try-catch block
        try {
            trafficErrorMsg = null
            val interfaces = com.example.ui.data.remote.ApiClient.apiService.getMikrotikInterfaces(area.id)
            val matchedPort = interfaces.find { it.name == selectedPort }
            if (matchedPort != null) {
                val currentRx = matchedPort.rxByte
                val currentTx = matchedPort.txByte
                
                val lastRx = com.example.ui.data.SettingsManager.getTrafficLastRx(area.id)
                val lastTx = com.example.ui.data.SettingsManager.getTrafficLastTx(area.id)
                
                var accumRx = com.example.ui.data.SettingsManager.getTrafficAccumRx(area.id)
                var accumTx = com.example.ui.data.SettingsManager.getTrafficAccumTx(area.id)
                
                // RX Accumulation with reboot check
                val deltaRx = if (lastRx == 0L) {
                    0L // First read/fresh port switch baseline
                } else if (currentRx >= lastRx) {
                    currentRx - lastRx
                } else {
                    currentRx // Reboot/reset detected, count since start of reboot
                }
                accumRx += deltaRx
                
                // TX Accumulation with reboot check
                val deltaTx = if (lastTx == 0L) {
                    0L // First read/fresh port switch baseline
                } else if (currentTx >= lastTx) {
                    currentTx - lastTx
                } else {
                    currentTx // Reboot/reset detected, count since start of reboot
                }
                accumTx += deltaTx
                
                // Persist & Update State
                com.example.ui.data.SettingsManager.setTrafficLastRx(area.id, currentRx)
                com.example.ui.data.SettingsManager.setTrafficLastTx(area.id, currentTx)
                com.example.ui.data.SettingsManager.setTrafficAccumRx(area.id, accumRx)
                com.example.ui.data.SettingsManager.setTrafficAccumTx(area.id, accumTx)
                
                currentRxBytes = currentRx
                currentTxBytes = currentTx
                accumulatedRxBytes = accumRx
                accumulatedTxBytes = accumTx
            } else {
                trafficErrorMsg = "Port '$selectedPort' tidak ditemukan di Mikrotik"
            }
        } catch(e: Exception) {
            trafficErrorMsg = "Traffic counter offline atau butuh update backend"
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(48.dp), tint = neonCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if (isLoading) "Connecting to ${area.routerIp}..." else if (errorMsg != null) "Error" else "Connected (${area.routerIp})", color = if (errorMsg != null) Color(0xFFFF003C) else neonCyan, fontSize = 14.sp)
                    }
                }
                
                // 3-Dot Options Menu
                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = textMain)
                    }
                    
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(cardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pilih Port Monitor", color = textMain) },
                            onClick = {
                                menuExpanded = false
                                showPortDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset Akumulasi Traffic", color = textMain) },
                            onClick = {
                                menuExpanded = false
                                com.example.ui.data.SettingsManager.setTrafficAccumRx(area.id, 0L)
                                com.example.ui.data.SettingsManager.setTrafficAccumTx(area.id, 0L)
                                com.example.ui.data.SettingsManager.setTrafficLastRx(area.id, 0L)
                                com.example.ui.data.SettingsManager.setTrafficLastTx(area.id, 0L)
                                accumulatedRxBytes = 0L
                                accumulatedTxBytes = 0L
                                currentRxBytes = 0L
                                currentTxBytes = 0L
                            }
                        )
                    }
                }
            }
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonCyan,
                    trackColor = Color.Transparent
                )
            }
            if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFFF003C), fontSize = 14.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MikrotikStatCard("Active PPPoE", if (isLoading) "..." else (mikrotikStatus?.activePppoe ?: "-"), neonCyan, textMain, textSecondary, Modifier.weight(1f))
                    MikrotikStatCard("PPPoE Offline", if (isLoading) "..." else (mikrotikStatus?.offlinePppoe ?: "-"), neonCyan, textMain, textSecondary, Modifier.weight(1f))
                }
                
                // Traffic Counter Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF05050A))
                        .border(1.dp, neonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Port Terpilih: $selectedPort", color = neonCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Traffic Counter", color = textSecondary, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(neonCyan.copy(alpha = 0.15f)))
                        
                        if (trafficErrorMsg != null) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Data Traffic Offline", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(trafficErrorMsg!!, color = textSecondary, fontSize = 11.sp)
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("RX (Download)", color = textSecondary, fontSize = 11.sp)
                                    Text(if (isLoading) "..." else formatBytes(accumulatedRxBytes), color = textMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Live: ${formatBytes(currentRxBytes)}", color = textSecondary, fontSize = 11.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TX (Upload)", color = textSecondary, fontSize = 11.sp)
                                    Text(if (isLoading) "..." else formatBytes(accumulatedTxBytes), color = textMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Live: ${formatBytes(currentTxBytes)}", color = textSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
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

    // Dialog for selecting ports
    if (showPortDialog) {
        var isInterfacesLoading by remember { mutableStateOf(false) }
        var interfacesList by remember { mutableStateOf<List<com.example.ui.data.remote.MikrotikInterface>>(emptyList()) }
        var interfacesError by remember { mutableStateOf<String?>(null) }
        
        LaunchedEffect(Unit) {
            isInterfacesLoading = true
            interfacesError = null
            try {
                interfacesList = com.example.ui.data.remote.ApiClient.apiService.getMikrotikInterfaces(area.id)
            } catch (e: Exception) {
                interfacesError = "Gagal mengambil daftar port: ${e.message}"
                // Fallback to default list if there's an error
                interfacesList = listOf(
                    com.example.ui.data.remote.MikrotikInterface("ether1", "ether"),
                    com.example.ui.data.remote.MikrotikInterface("ether2", "ether"),
                    com.example.ui.data.remote.MikrotikInterface("ether3", "ether"),
                    com.example.ui.data.remote.MikrotikInterface("ether4", "ether"),
                    com.example.ui.data.remote.MikrotikInterface("ether5", "ether"),
                    com.example.ui.data.remote.MikrotikInterface("bridge", "bridge")
                )
            } finally {
                isInterfacesLoading = false
            }
        }

        AlertDialog(
            onDismissRequest = { showPortDialog = false },
            title = { Text("Pilih Port Monitor", color = textMain) },
            containerColor = cardBg,
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih port Mikrotik di bawah ini untuk dihitung traffic counter-nya:", color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isInterfacesLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = neonCyan)
                        }
                    } else if (interfacesError != null && interfacesList.isEmpty()) {
                        Text(interfacesError ?: "Error", color = Color.Red, fontSize = 14.sp)
                    } else {
                        Box(modifier = Modifier.heightIn(max = 250.dp)) {
                            LazyColumn {
                                items(interfacesList) { item ->
                                    val isSelected = item.name == selectedPort
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) neonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable {
                                                com.example.ui.data.SettingsManager.setSelectedPort(area.id, item.name)
                                                com.example.ui.data.SettingsManager.setTrafficLastRx(area.id, 0L)
                                                com.example.ui.data.SettingsManager.setTrafficLastTx(area.id, 0L)
                                                selectedPortChangedTrigger++
                                                showPortDialog = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Router, 
                                            contentDescription = null, 
                                            tint = if (isSelected) neonCyan else textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(item.name, color = textMain, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                                            Text("Type: ${item.type}", color = textSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPortDialog = false }) {
                    Text("Tutup", color = neonCyan)
                }
            }
        )
    }
}

fun formatBytes(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    val tb = gb * 1024.0
    
    return when {
        bytes >= tb -> String.format("%.2f TB", bytes / tb)
        bytes >= gb -> String.format("%.2f GB", bytes / gb)
        bytes >= mb -> String.format("%.2f MB", bytes / mb)
        bytes >= kb -> String.format("%.2f KB", bytes / kb)
        else -> "$bytes B"
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
