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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
    
    var showAddDialog by remember { mutableStateOf(false) }
    var secretToDelete by remember { mutableStateOf<PPPoESecret?>(null) }
    var addName by remember { mutableStateOf("") }
    var addPassword by remember { mutableStateOf("") }
    var addProfile by remember { mutableStateOf("") }
    var profilesList by remember { mutableStateOf<List<com.example.ui.data.remote.MikrotikProfile>>(emptyList()) }
    var isProfilesLoading by remember { mutableStateOf(false) }
    
    val allSecrets = remember { mutableStateListOf<PPPoESecret>() }
    var networkError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showAddDialog) {
        if (showAddDialog) {
            try {
                isProfilesLoading = true
                profilesList = com.example.ui.data.remote.ApiClient.apiService.getMikrotikProfiles(areaId)
                if (profilesList.isNotEmpty()) {
                    addProfile = profilesList[0].name
                } else {
                    // Fallback list of standard profiles
                    profilesList = listOf(
                        com.example.ui.data.remote.MikrotikProfile("1", "default"),
                        com.example.ui.data.remote.MikrotikProfile("2", "1M"),
                        com.example.ui.data.remote.MikrotikProfile("3", "2M"),
                        com.example.ui.data.remote.MikrotikProfile("4", "3M"),
                        com.example.ui.data.remote.MikrotikProfile("5", "4M"),
                        com.example.ui.data.remote.MikrotikProfile("6", "5M"),
                        com.example.ui.data.remote.MikrotikProfile("7", "10M"),
                        com.example.ui.data.remote.MikrotikProfile("8", "20M")
                    )
                    addProfile = "default"
                }
            } catch(e: Exception) {
                android.widget.Toast.makeText(context, "Gagal memuat profil, menggunakan cadangan: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                // Fallback list of standard profiles on failure
                profilesList = listOf(
                    com.example.ui.data.remote.MikrotikProfile("1", "default"),
                    com.example.ui.data.remote.MikrotikProfile("2", "1M"),
                    com.example.ui.data.remote.MikrotikProfile("3", "2M"),
                    com.example.ui.data.remote.MikrotikProfile("4", "3M"),
                    com.example.ui.data.remote.MikrotikProfile("5", "4M"),
                    com.example.ui.data.remote.MikrotikProfile("6", "5M"),
                    com.example.ui.data.remote.MikrotikProfile("7", "10M"),
                    com.example.ui.data.remote.MikrotikProfile("8", "20M")
                )
                addProfile = "default"
            } finally {
                isProfilesLoading = false
            }
        }
    }

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
                title = { Text("Manage Secrets", color = textMain, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = neonCyan) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Secret", tint = Color.Black)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ringkasan PPPoE", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (networkError != null) {
                Text(networkError!!, color = errorRed, fontSize = 14.sp)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard("Total", totalSecrets.toString(), neonCyan, currentFilter == SecretFilter.ALL, Modifier.weight(1f)) {
                    currentFilter = SecretFilter.ALL
                }
                SummaryCard("Online", totalOnline.toString(), successGreen, currentFilter == SecretFilter.ONLINE, Modifier.weight(1f)) {
                    currentFilter = SecretFilter.ONLINE
                }
                SummaryCard("Offline", totalOffline.toString(), textSecondary, currentFilter == SecretFilter.OFFLINE, Modifier.weight(1f)) {
                    currentFilter = SecretFilter.OFFLINE
                }
                SummaryCard("Disable", totalDisabled.toString(), errorRed, currentFilter == SecretFilter.DISABLED, Modifier.weight(1f)) {
                    currentFilter = SecretFilter.DISABLED
                }
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                    focusedTextColor = textMain, unfocusedTextColor = textMain
                )
            )
            
            if (isLoading && allSecrets.isEmpty()) {
                CircularProgressIndicator(color = neonCyan, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                    items(displayedSecrets) { secret ->
                        SecretCard(
                            secret = secret,
                            areaId = areaId,
                            neonCyan = neonCyan,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            cardBg = cardBg,
                            successGreen = successGreen,
                            errorRed = errorRed,
                            onDelete = { secretToDelete = secret },
                            onRefresh = {
                                coroutineScope.launch {
                                    try {
                                        val secrets = com.example.ui.data.remote.ApiClient.apiService.getMikrotikSecrets(areaId)
                                        allSecrets.clear()
                                        allSecrets.addAll(secrets)
                                    } catch(e: Exception) {}
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        var profileExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text("Tambah Secret PPPoE") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isProfilesLoading) {
                        CircularProgressIndicator(color = neonCyan)
                    } else {
                        OutlinedTextField(
                            value = addName,
                            onValueChange = { addName = it },
                            label = { Text("Username PPPoE") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            )
                        )
                        OutlinedTextField(
                            value = addPassword,
                            onValueChange = { addPassword = it },
                            label = { Text("Password PPPoE") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            )
                        )
                        Box {
                            OutlinedTextField(
                                value = addProfile,
                                onValueChange = {},
                                label = { Text("Profile PPPoE") },
                                readOnly = true,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Profil", tint = textMain)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                                    focusedTextColor = textMain, unfocusedTextColor = textMain
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { profileExpanded = true })
                            DropdownMenu(
                                expanded = profileExpanded,
                                onDismissRequest = { profileExpanded = false },
                                modifier = Modifier.background(cardBg).heightIn(max = 250.dp)
                            ) {
                                profilesList.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.name, color = textMain) },
                                        onClick = {
                                            addProfile = p.name
                                            profileExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            isLoading = true
                            com.example.ui.data.remote.ApiClient.apiService.addMikrotikSecret(
                                areaId, mapOf("name" to addName, "password" to addPassword, "profile" to addProfile)
                            )
                            val secrets = com.example.ui.data.remote.ApiClient.apiService.getMikrotikSecrets(areaId)
                            allSecrets.clear()
                            allSecrets.addAll(secrets)
                            showAddDialog = false
                            addName = ""
                            addPassword = ""
                        } catch(e: Exception) {
                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Text("Simpan", color = neonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    if (secretToDelete != null) {
        AlertDialog(
            onDismissRequest = { secretToDelete = null },
            containerColor = cardBg,
            titleContentColor = errorRed,
            textContentColor = textSecondary,
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus secret '${secretToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                com.example.ui.data.remote.ApiClient.apiService.deleteMikrotikSecret(areaId, mapOf("name" to secretToDelete!!.name))
                                val secrets = com.example.ui.data.remote.ApiClient.apiService.getMikrotikSecrets(areaId)
                                allSecrets.clear()
                                allSecrets.addAll(secrets)
                                secretToDelete = null
                            } catch(e: retrofit2.HttpException) {
                                val errorMsg = try {
                                    val json = e.response()?.errorBody()?.string()
                                    val obj = org.json.JSONObject(json)
                                    obj.getString("error")
                                } catch(err: Exception) {
                                    e.message()
                                }
                                android.widget.Toast.makeText(context, "Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                            } catch(e: Exception) {
                                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { secretToDelete = null }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SecretCard(
    secret: PPPoESecret, areaId: String, neonCyan: Color, textMain: Color, textSecondary: Color, cardBg: Color,
    successGreen: Color, errorRed: Color, onDelete: () -> Unit, onRefresh: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, neonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = neonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(secret.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = secret.status,
                        color = when (secret.status) {
                            "Online" -> successGreen
                            "Offline" -> textSecondary
                            else -> errorRed
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowDropDown, 
                        contentDescription = "Expand",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp).rotate(if (expanded) 180f else 0f)
                    )
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Profile: ${secret.profile}", color = textSecondary, fontSize = 14.sp)
            if (secret.status == "Online") {
                Text("IP: ${secret.ipAddress}", color = textSecondary, fontSize = 14.sp)
                Text("Uptime: ${secret.uptime}", color = textSecondary, fontSize = 14.sp)
            }
            
            if (secret.status == "Online") {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Traffic (Live)", color = textSecondary, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                SimpleTrafficChart(areaId, secret.name)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (secret.status == "Disabled") {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.enableMikrotikSecret(areaId, mapOf("name" to secret.name))
                                    onRefresh()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = successGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, successGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enable", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(areaId, mapOf("name" to secret.name))
                                    onRefresh()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = errorRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, errorRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disable", fontSize = 12.sp)
                    }
                }
                
                if (secret.status == "Online") {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.removeActiveMikrotikSecret(areaId, mapOf("name" to secret.name))
                                    onRefresh()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFFF9900)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9900)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kick", fontSize = 12.sp)
                    }
                }
                
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = errorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, errorRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }
            }
                }
            }
        }
    }
}

@Composable
fun WinboxTrafficGraph(
    history: List<Pair<Float, Float>>,
    colorRx: Color,
    colorTx: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Draw dark background
        drawRect(color = Color(0xFF151515))
        
        // Draw horizontal grid lines
        val gridLinesCount = 4
        val gridColor = Color(0xFF282828)
        for (i in 1 until gridLinesCount) {
            val y = height * i / gridLinesCount
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(width, y),
                strokeWidth = 1f
            )
        }
        
        // Draw vertical grid lines
        val verticalGridCount = 8
        for (i in 1 until verticalGridCount) {
            val x = width * i / verticalGridCount
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, height),
                strokeWidth = 1f
            )
        }
        
        if (history.isEmpty()) return@Canvas
        
        // Find max value in history to auto-scale, with a minimum of 1.0 Mbps
        val maxVal = history.flatMap { listOf(it.first, it.second) }.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val scaleMax = maxVal * 1.15f
        
        val pointsCount = history.size
        val dx = if (pointsCount > 1) width / (pointsCount - 1) else width
        
        val rxPath = androidx.compose.ui.graphics.Path()
        val rxAreaPath = androidx.compose.ui.graphics.Path()
        
        val txPath = androidx.compose.ui.graphics.Path()
        val txAreaPath = androidx.compose.ui.graphics.Path()
        
        history.forEachIndexed { index, pair ->
            val rx = pair.first
            val tx = pair.second
            
            val x = index * dx
            val yRx = height - (rx / scaleMax) * height
            val yTx = height - (tx / scaleMax) * height
            
            if (index == 0) {
                rxPath.moveTo(x, yRx)
                rxAreaPath.moveTo(x, height)
                rxAreaPath.lineTo(x, yRx)
                
                txPath.moveTo(x, yTx)
                txAreaPath.moveTo(x, height)
                txAreaPath.lineTo(x, yTx)
            } else {
                rxPath.lineTo(x, yRx)
                rxAreaPath.lineTo(x, yRx)
                
                txPath.lineTo(x, yTx)
                txAreaPath.lineTo(x, yTx)
            }
            
            if (index == pointsCount - 1) {
                rxAreaPath.lineTo(x, height)
                rxAreaPath.close()
                
                txAreaPath.lineTo(x, height)
                txAreaPath.close()
            }
        }
        
        // Draw areas
        drawPath(
            path = rxAreaPath,
            color = colorRx.copy(alpha = 0.20f)
        )
        drawPath(
            path = txAreaPath,
            color = colorTx.copy(alpha = 0.20f)
        )
        
        // Draw solid lines
        drawPath(
            path = rxPath,
            color = colorRx,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
        drawPath(
            path = txPath,
            color = colorTx,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun SimpleTrafficChart(areaId: String, secretName: String) {
    var currentRxString by remember { mutableStateOf("0.00 Mbps") }
    var currentTxString by remember { mutableStateOf("0.00 Mbps") }
    
    var rxMbps by remember { mutableStateOf(0f) }
    var txMbps by remember { mutableStateOf(0f) }
    
    val trafficHistory = remember {
        val list = mutableStateListOf<Pair<Float, Float>>()
        repeat(50) {
            list.add(Pair(0f, 0f))
        }
        list
    }
    
    val colorTx = Color(0xFF2ECC71) // Upload: Bright Green (Winbox style)
    val colorRx = Color(0xFF3498DB) // Download: Bright Blue (Winbox style)
    
    val localContext = androidx.compose.ui.platform.LocalContext.current
    val token = remember {
        var tok = com.example.ui.data.UserSession.currentUser.value?.token
        if (tok.isNullOrEmpty()) {
            val sharedPrefs = localContext.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            tok = sharedPrefs.getString("user_token", "")
        }
        tok ?: ""
    }
    
    var lastWsMessageTime by remember { mutableStateOf(0L) }
    
    androidx.compose.runtime.LaunchedEffect(secretName) {
        val iface = "<pppoe-$secretName>"
        val baseUrl = com.example.ui.data.remote.ApiClient.BASE_URL
        val cleanBaseUrl = baseUrl.removeSuffix("/")
        val wsBaseUrl = cleanBaseUrl.replace("http://", "ws://").replace("https://", "wss://")
        val wsUrl = "$wsBaseUrl/ws/traffic?areaId=${areaId}&interface=${java.net.URLEncoder.encode(iface, "UTF-8")}&token=${token}"
        
        android.util.Log.d("SimpleTrafficChart", "Connecting WS: $wsUrl")
        
        val client = okhttp3.OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        kotlinx.coroutines.coroutineScope {
            // Coroutine 1: Maintain WebSocket connection
            launch {
                while (true) {
                    val request = okhttp3.Request.Builder().url(wsUrl).build()
                    var wsSession: okhttp3.WebSocket? = null
                    
                    val listener = object : okhttp3.WebSocketListener() {
                        override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                            android.util.Log.d("SimpleTrafficChart", "WS Opened successfully")
                            lastWsMessageTime = System.currentTimeMillis()
                        }
                        
                        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                            try {
                                val jsonObject = org.json.JSONObject(text)
                                if (jsonObject.has("error")) {
                                    android.util.Log.e("SimpleTrafficChart", "WS Data Error: " + jsonObject.getString("error"))
                                    return
                                }
                                
                                val rxVal = jsonObject.optDouble("rx", 0.0)
                                val txVal = jsonObject.optDouble("tx", 0.0)
                                val rMbps = (rxVal / 1_000_000.0).toFloat()
                                val tMbps = (txVal / 1_000_000.0).toFloat()
                                
                                rxMbps = rMbps
                                txMbps = tMbps
                                
                                currentRxString = String.format(java.util.Locale.US, "%.2f Mbps", rMbps)
                                currentTxString = String.format(java.util.Locale.US, "%.2f Mbps", tMbps)
                                
                                trafficHistory.add(Pair(rMbps, tMbps))
                                if (trafficHistory.size > 50) {
                                    trafficHistory.removeAt(0)
                                }
                                
                                lastWsMessageTime = System.currentTimeMillis()
                            } catch(e: Exception) {
                                // ignore
                            }
                        }
                        
                        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                            android.util.Log.e("SimpleTrafficChart", "WS Failure: ${t.message}", t)
                        }
                        
                        override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                            android.util.Log.d("SimpleTrafficChart", "WS Closed: $reason")
                        }
                    }
                    
                    wsSession = client.newWebSocket(request, listener)
                    
                    try {
                        while (true) {
                            kotlinx.coroutines.delay(2000)
                            if (System.currentTimeMillis() - lastWsMessageTime > 10000) {
                                android.util.Log.d("SimpleTrafficChart", "WS Timeout, reconnecting...")
                                break
                            }
                        }
                    } catch(e: Exception) {
                        // ignore
                    } finally {
                        try { wsSession?.close(1000, "Reconnecting") } catch(e: Exception) {}
                    }
                    
                    kotlinx.coroutines.delay(5000)
                }
            }
            
            // Coroutine 2: Polling fallback
            launch {
                while (true) {
                    val now = System.currentTimeMillis()
                    if (now - lastWsMessageTime > 5000) {
                        try {
                            android.util.Log.d("SimpleTrafficChart", "WS inactive, using Polling fallback for interface $iface")
                            val responseList = com.example.ui.data.remote.ApiClient.apiService.getMikrotikTraffic(areaId, iface)
                            if (responseList.isNotEmpty()) {
                                val response = responseList[0]
                                val rxBits = response.rxBits?.toLongOrNull() ?: response.rx ?: ((response.rxByte ?: 0) * 8)
                                val txBits = response.txBits?.toLongOrNull() ?: response.tx ?: ((response.txByte ?: 0) * 8)
                                
                                val rMbps = rxBits.toFloat() / 1_000_000f
                                val tMbps = txBits.toFloat() / 1_000_000f
                                rxMbps = rMbps
                                txMbps = tMbps
                                
                                currentRxString = String.format(java.util.Locale.US, "%.2f Mbps", rMbps)
                                currentTxString = String.format(java.util.Locale.US, "%.2f Mbps", tMbps)
                                
                                trafficHistory.add(Pair(rMbps, tMbps))
                                if (trafficHistory.size > 50) {
                                    trafficHistory.removeAt(0)
                                }
                            } else {
                                currentRxString = "0.00 Mbps"
                                currentTxString = "0.00 Mbps"
                                rxMbps = 0f
                                txMbps = 0f
                                
                                trafficHistory.add(Pair(0f, 0f))
                                if (trafficHistory.size > 50) {
                                    trafficHistory.removeAt(0)
                                }
                            }
                        } catch(e: Exception) {
                            android.util.Log.e("SimpleTrafficChart", "Polling fallback failed: ${e.message}", e)
                            currentRxString = "0.00 Mbps"
                            currentTxString = "0.00 Mbps"
                        }
                    }
                    kotlinx.coroutines.delay(2000)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF222222))
            .border(1.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Legend and real-time numeric text
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Download", tint = colorRx, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RX (Download):", color = Color.LightGray, fontSize = 12.sp)
            }
            Text(currentRxString, color = colorRx, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Upload", tint = colorTx, modifier = Modifier.size(16.dp).rotate(180f))
                Spacer(modifier = Modifier.width(4.dp))
                Text("TX (Upload):", color = Color.LightGray, fontSize = 12.sp)
            }
            Text(currentTxString, color = colorTx, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Custom real-time Winbox-style scrolling graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(4.dp))
        ) {
            WinboxTrafficGraph(
                history = trafficHistory,
                colorRx = colorRx,
                colorTx = colorTx,
                modifier = Modifier.fillMaxSize()
            )
            
            // Auto-scale labels overlaid on the graph
            val maxVal = trafficHistory.flatMap { listOf(it.first, it.second) }.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val scaleMax = maxVal * 1.15f
            
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.2f Mbps", scaleMax),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Text(
                    text = String.format(java.util.Locale.US, "%.2f Mbps", scaleMax / 2f),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Text(
                    text = "0.00 Mbps",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
