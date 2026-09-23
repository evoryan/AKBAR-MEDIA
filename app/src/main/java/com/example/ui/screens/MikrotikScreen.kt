package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    var currentRxRateBps by remember { mutableStateOf(0L) }
    var currentTxRateBps by remember { mutableStateOf(0L) }
    val rxHistory = remember { mutableStateListOf<Float>() }
    val txHistory = remember { mutableStateListOf<Float>() }
    var trafficErrorMsg by remember { mutableStateOf<String?>(null) }
    
    var showPortDialog by remember { mutableStateOf(false) }

    // Realtime background polling for traffic rate and graph
    LaunchedEffect(area.id, selectedPort) {
        rxHistory.clear()
        txHistory.clear()
        for (i in 0 until 20) {
            rxHistory.add(0f)
            txHistory.add(0f)
        }
        var prevRx: Long? = null
        var prevTx: Long? = null
        var prevTime: Long = System.currentTimeMillis()

        while (true) {
            try {
                val trafficList = try {
                    com.example.ui.data.remote.ApiClient.apiService.getMikrotikTraffic(area.id, selectedPort)
                } catch (e: Exception) {
                    null
                }
                val trafficItem = trafficList?.firstOrNull()

                val rxBpsVal = trafficItem?.rxBits?.toLongOrNull() 
                    ?: trafficItem?.rx 
                    ?: trafficItem?.rxByte
                val txBpsVal = trafficItem?.txBits?.toLongOrNull() 
                    ?: trafficItem?.tx 
                    ?: trafficItem?.txByte

                if (rxBpsVal != null && txBpsVal != null) {
                    currentRxRateBps = rxBpsVal
                    currentTxRateBps = txBpsVal

                    if (rxHistory.size >= 25) rxHistory.removeAt(0)
                    rxHistory.add(rxBpsVal.toFloat())

                    if (txHistory.size >= 25) txHistory.removeAt(0)
                    txHistory.add(txBpsVal.toFloat())
                } else {
                    // Fallback using interface byte difference
                    val interfaces = com.example.ui.data.remote.ApiClient.apiService.getMikrotikInterfaces(area.id)
                    val matchedPort = interfaces.find { it.name == selectedPort }
                    if (matchedPort != null) {
                        val now = System.currentTimeMillis()
                        val dt = (now - prevTime).coerceAtLeast(500L) / 1000.0

                        if (prevRx != null && prevTx != null) {
                            val curRx = matchedPort.rxByte
                            val curTx = matchedPort.txByte
                            val dRx = if (curRx >= prevRx!!) curRx - prevRx!! else curRx
                            val dTx = if (curTx >= prevTx!!) curTx - prevTx!! else curTx

                            val rxBps = (dRx * 8.0 / dt).toLong().coerceAtLeast(0L)
                            val txBps = (dTx * 8.0 / dt).toLong().coerceAtLeast(0L)

                            currentRxRateBps = rxBps
                            currentTxRateBps = txBps

                            if (rxHistory.size >= 25) rxHistory.removeAt(0)
                            rxHistory.add(rxBps.toFloat())

                            if (txHistory.size >= 25) txHistory.removeAt(0)
                            txHistory.add(txBps.toFloat())
                        }

                        prevRx = matchedPort.rxByte
                        prevTx = matchedPort.txByte
                        prevTime = now
                    }
                }
            } catch (e: Exception) {
                // Silently ignore single cycle network hiccups in graph
            }
            delay(2000L)
        }
    }

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
                
                // Realtime Traffic Monitoring Graph Section
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
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF00FF66))
                                )
                                Text("Port: $selectedPort", color = neonCyan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Text("Realtime Traffic Graph", color = textSecondary, fontSize = 11.sp)
                        }
                        
                        // Realtime Speed Stats
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp, 8.dp).background(Color(0xFF00E5FF), RoundedCornerShape(2.dp)))
                                Text("RX: ", color = textSecondary, fontSize = 12.sp)
                                Text(formatBitrate(currentRxRateBps), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp, 8.dp).background(Color(0xFFFF3366), RoundedCornerShape(2.dp)))
                                Text("TX: ", color = textSecondary, fontSize = 12.sp)
                                Text(formatBitrate(currentTxRateBps), color = Color(0xFFFF3366), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // Realtime Canvas Graph
                        RealtimeTrafficChart(
                            rxData = rxHistory.toList(),
                            txData = txHistory.toList(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                        )

                        // Total Accumulated Counter Summary
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total RX: ${formatBytes(accumulatedRxBytes)}", color = textSecondary, fontSize = 11.sp)
                            Text("Total TX: ${formatBytes(accumulatedTxBytes)}", color = textSecondary, fontSize = 11.sp)
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

fun formatBitrate(bps: Long): String {
    val kbps = 1000.0
    val mbps = kbps * 1000.0
    val gbps = mbps * 1000.0

    return when {
        bps >= gbps -> String.format("%.2f Gbps", bps / gbps)
        bps >= mbps -> String.format("%.2f Mbps", bps / mbps)
        bps >= kbps -> String.format("%.1f Kbps", bps / kbps)
        else -> "$bps bps"
    }
}

@Composable
fun RealtimeTrafficChart(
    rxData: List<Float>,
    txData: List<Float>,
    modifier: Modifier = Modifier
) {
    val rxColor = Color(0xFF00E5FF)
    val txColor = Color(0xFFFF3366)
    val gridColor = Color(0xFF222233)
    val peakLabelColor = Color(0xFF888899)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0A0A14))
            .border(0.5.dp, Color(0xFF1E1E2E), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        val maxRx = rxData.maxOrNull() ?: 0f
        val maxTx = txData.maxOrNull() ?: 0f
        val maxVal = (maxOf(maxRx, maxTx).coerceAtLeast(100_000f)) * 1.15f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw horizontal grid lines (3 lines)
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Helper to generate path
            fun buildPath(data: List<Float>): Path {
                val path = Path()
                if (data.isEmpty()) return path

                val stepX = if (data.size > 1) width / (data.size - 1) else width
                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val normalizedY = ((value / maxVal).coerceIn(0f, 1f))
                    val y = height - (normalizedY * height)
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                return path
            }

            fun buildAreaPath(linePath: Path, data: List<Float>): Path {
                val areaPath = Path()
                if (data.isEmpty()) return areaPath
                areaPath.addPath(linePath)
                val stepX = if (data.size > 1) width / (data.size - 1) else width
                val lastX = (data.size - 1) * stepX
                areaPath.lineTo(lastX, height)
                areaPath.lineTo(0f, height)
                areaPath.close()
                return areaPath
            }

            // Draw RX area & line
            if (rxData.size >= 2) {
                val rxLinePath = buildPath(rxData)
                val rxAreaPath = buildAreaPath(rxLinePath, rxData)
                drawPath(
                    path = rxAreaPath,
                    color = rxColor.copy(alpha = 0.22f)
                )
                drawPath(
                    path = rxLinePath,
                    color = rxColor,
                    style = Stroke(width = 2.5f)
                )
            }

            // Draw TX area & line
            if (txData.size >= 2) {
                val txLinePath = buildPath(txData)
                val txAreaPath = buildAreaPath(txLinePath, txData)
                drawPath(
                    path = txAreaPath,
                    color = txColor.copy(alpha = 0.20f)
                )
                drawPath(
                    path = txLinePath,
                    color = txColor,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Peak scale indicator in upper right corner
        Text(
            text = "Peak: ${formatBitrate(maxVal.toLong())}",
            color = peakLabelColor,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
        )
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
