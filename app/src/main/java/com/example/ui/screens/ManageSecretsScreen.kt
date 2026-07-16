package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SecretFilter {
    ALL, ONLINE, OFFLINE, DISABLED
}

data class PPPoESecret(
    val id: String = "",
    val name: String,
    val profile: String,
    val status: String, // "Online", "Offline", "Disabled"
    val ipAddress: String = "",
    val uptime: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSecretsScreen(areaId: String, onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val errorRed = Color(0xFFFF003C)
    val successGreen = Color(0xFF00FF00)

    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(SecretFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    
    val allSecrets = remember { mutableStateListOf<PPPoESecret>() }
    var networkError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(areaId) {
        try {
            isLoading = true
            networkError = null
            val secrets = com.example.ui.data.remote.ApiClient.apiService.getMikrotikSecrets(areaId)
            allSecrets.clear()
            allSecrets.addAll(secrets)
        } catch(e: Exception) {
            networkError = "Gagal memuat: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val displayedSecrets = allSecrets.filter {
        val matchesFilter = when (currentFilter) {
            SecretFilter.ALL -> true
            SecretFilter.ONLINE -> it.status == "Online"
            SecretFilter.OFFLINE -> it.status == "Offline"
            SecretFilter.DISABLED -> it.status == "Disabled"
        }
        val matchesSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    val totalSecrets = allSecrets.size
    val totalOnline = allSecrets.count { it.status == "Online" }
    val totalOffline = allSecrets.count { it.status == "Offline" }
    val totalDisabled = allSecrets.count { it.status == "Disabled" }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Manage Secrets", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
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
            Text("Ringkasan PPPoE", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 20.sp, fontWeight = FontWeight.Bold)

            if (networkError != null) {
                Text(networkError!!, color = errorRed, fontSize = 14.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard(
                    title = "Total",
                    value = totalSecrets.toString(),
                    color = neonCyan,
                    isSelected = currentFilter == SecretFilter.ALL,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentFilter = SecretFilter.ALL
                        isLoading = true
                        coroutineScope.launch { delay(500); isLoading = false }
                    }
                )
                SummaryCard(
                    title = "Online",
                    value = totalOnline.toString(),
                    color = successGreen,
                    isSelected = currentFilter == SecretFilter.ONLINE,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentFilter = SecretFilter.ONLINE
                        isLoading = true
                        coroutineScope.launch { delay(500); isLoading = false }
                    }
                )
                SummaryCard(
                    title = "Offline",
                    value = totalOffline.toString(),
                    color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666),
                    isSelected = currentFilter == SecretFilter.OFFLINE,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentFilter = SecretFilter.OFFLINE
                        isLoading = true
                        coroutineScope.launch { delay(500); isLoading = false }
                    }
                )
                SummaryCard(
                    title = "Disable",
                    value = totalDisabled.toString(),
                    color = errorRed,
                    isSelected = currentFilter == SecretFilter.DISABLED,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        currentFilter = SecretFilter.DISABLED
                        isLoading = true
                        coroutineScope.launch { delay(500); isLoading = false }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari Secret...", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain,
                    cursorColor = neonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daftar Secrets (${currentFilter.name})",
                    color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isLoading) {
                    CircularProgressIndicator(color = neonCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }

            if (!isLoading) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayedSecrets) { secret ->
                        SecretItem(
                            secret = secret,
                            areaId = areaId,
                            cardBg = cardBg,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            neonCyan = neonCyan,
                            successGreen = successGreen,
                            errorRed = errorRed,
                            onDisable = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(areaId, mapOf("secretId" to secret.id))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Disabled")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal disable secret: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onEnable = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.enableMikrotikSecret(areaId, mapOf("secretId" to secret.id))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal enable secret: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onRemoveActive = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.removeActiveMikrotikSecret(areaId, mapOf("secretName" to secret.name))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline", ipAddress = "", uptime = "")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal remove active: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF11111A)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SecretItem(
    secret: PPPoESecret,
    areaId: String,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    successGreen: Color,
    errorRed: Color,
    onDisable: () -> Unit,
    onEnable: () -> Unit,
    onRemoveActive: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (secret.status) {
        "Online" -> successGreen
        "Offline" -> textSecondary
        "Disabled" -> errorRed
        else -> textSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = neonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(secret.name, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(secret.profile, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusIcon = when (secret.status) {
                        "Online" -> Icons.Default.CheckCircle
                        "Disabled" -> Icons.Default.Block
                        else -> Icons.Default.Cancel
                    }
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(secret.status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (secret.status == "Online") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IP Address", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                            Text(secret.ipAddress, color = neonCyan, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Uptime", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                            Text(secret.uptime, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Traffic (Live)", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        SimpleTrafficChart(areaId, secret.name)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (secret.status == "Disabled") {
                            Button(
                                onClick = onEnable,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = successGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, successGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enable Secret", fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onDisable,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = errorRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, errorRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Disable Secret", fontSize = 12.sp)
                            }
                        }
                        
                        if (secret.status == "Online") {
                            Button(
                                onClick = onRemoveActive,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFFF9900)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9900)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Active", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleTrafficChart(areaId: String, secretName: String) {
    val trafficDataRx = remember { androidx.compose.runtime.mutableStateListOf<Float>(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    val trafficDataTx = remember { androidx.compose.runtime.mutableStateListOf<Float>(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    var currentRxString by remember { mutableStateOf("0.0 Mbps") }
    var currentTxString by remember { mutableStateOf("0.0 Mbps") }
    
    val colorTx = Color(0xFF51A351) // Winbox TX Green
    val colorRx = Color(0xFF2F70B8) // Winbox RX Blue / Yellowish (usually RX is Blue/Red, let's use Blue)
    val gridColor = Color(0xFF333333)
    val bgColor = Color(0xFF111111)

    androidx.compose.runtime.LaunchedEffect(secretName) {
        val iface = "<pppoe-$secretName>"
        while (true) {
            try {
                val responseList = com.example.ui.data.remote.ApiClient.apiService.getMikrotikTraffic(areaId, iface)
                if (responseList.isNotEmpty()) {
                    val response = responseList[0]
                    val rxBits = response.rxBits?.toLongOrNull() ?: response.rx ?: ((response.rxByte ?: 0) * 8)
                    val txBits = response.txBits?.toLongOrNull() ?: response.tx ?: ((response.txByte ?: 0) * 8)
                    
                    val rxMbps = rxBits.toFloat() / 1_000_000f
                    val txMbps = txBits.toFloat() / 1_000_000f
                    
                    trafficDataRx.removeAt(0)
                    trafficDataRx.add(rxMbps)
                    
                    trafficDataTx.removeAt(0)
                    trafficDataTx.add(txMbps)
                    
                    currentRxString = response.rxString ?: String.format(java.util.Locale.US, "%.1f Mbps", rxMbps)
                    currentTxString = response.txString ?: String.format(java.util.Locale.US, "%.1f Mbps", txMbps)
                } else {
                    trafficDataRx.removeAt(0)
                    trafficDataRx.add(0f)
                    trafficDataTx.removeAt(0)
                    trafficDataTx.add(0f)
                    currentRxString = "0.0 Mbps"
                    currentTxString = "0.0 Mbps"
                }
                
            } catch(e: Exception) {
                // If API fails, we could either keep 0 or do some mock for visual purpose if not implemented.
                // We'll just push 0 to show it's flat if no data.
                trafficDataRx.removeAt(0)
                trafficDataRx.add(0f)
                trafficDataTx.removeAt(0)
                trafficDataTx.add(0f)
                
                // Fallback text if we want to show it's offline/error
                currentRxString = "Err: ${e.message?.take(15)}"
                currentTxString = "Err"
            }
            kotlinx.coroutines.delay(1000)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
            .background(Color(0xFF222222))
            .border(1.dp, Color(0xFF444444), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(colorTx))
                Spacer(modifier = Modifier.width(4.dp))
                Text("TX: $currentTxString", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(colorRx))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RX: $currentRxString", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(bgColor)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val maxData = (trafficDataRx.maxOrNull()?.coerceAtLeast(trafficDataTx.maxOrNull() ?: 0f) ?: 100f).coerceAtLeast(10f)
                val stepX = size.width / (trafficDataRx.size - 1)
                
                // Draw Grid
                val verticalLines = 5
                val horizontalLines = 3
                for (i in 0..verticalLines) {
                    val x = i * (size.width / verticalLines)
                    drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = 1f)
                }
                for (i in 0..horizontalLines) {
                    val y = i * (size.height / horizontalLines)
                    drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1f)
                }
                
                // Draw TX (Green)
                val pathTx = androidx.compose.ui.graphics.Path()
                val pathTxFill = androidx.compose.ui.graphics.Path()
                trafficDataTx.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value / maxData * size.height)
                    if (index == 0) {
                        pathTx.moveTo(x, y)
                        pathTxFill.moveTo(x, size.height)
                        pathTxFill.lineTo(x, y)
                    } else {
                        pathTx.lineTo(x, y)
                        pathTxFill.lineTo(x, y)
                    }
                }
                pathTxFill.lineTo(size.width, size.height)
                pathTxFill.close()
                drawPath(path = pathTxFill, color = colorTx.copy(alpha = 0.3f))
                drawPath(path = pathTx, color = colorTx, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                
                // Draw RX (Blue)
                val pathRx = androidx.compose.ui.graphics.Path()
                val pathRxFill = androidx.compose.ui.graphics.Path()
                trafficDataRx.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value / maxData * size.height)
                    if (index == 0) {
                        pathRx.moveTo(x, y)
                        pathRxFill.moveTo(x, size.height)
                        pathRxFill.lineTo(x, y)
                    } else {
                        pathRx.lineTo(x, y)
                        pathRxFill.lineTo(x, y)
                    }
                }
                pathRxFill.lineTo(size.width, size.height)
                pathRxFill.close()
                drawPath(path = pathRxFill, color = colorRx.copy(alpha = 0.3f))
                drawPath(path = pathRx, color = colorRx, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
