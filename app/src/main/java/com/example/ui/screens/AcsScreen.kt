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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.screens.Area
import com.example.ui.data.remote.ApiClient
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp


data class AcsDevice(
    val id: String?,
    val username: String?,
    val isOnline: Boolean?,
    val ssid: String? = "Unknown",
    val wifiPassword: String? = "-",
    val connectedUsers: Int? = 0,
    val customerNumber: String? = "-",
    val rxPower: String? = "-",
    val areaName: String? = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcsScreen(onBack: () -> Unit, initialSearchQuery: String = "") {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val primaryCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val successGreen = Color(0xFF00FF4D)
    val errorRed = Color(0xFFFF003C)
    val warningYellow = Color(0xFFFFB800)

    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var showOnlyOffline by remember { mutableStateOf(false) }
    
    var allDevices by remember { mutableStateOf<List<AcsDevice>>(emptyList()) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedArea by remember { mutableStateOf<com.example.ui.screens.Area?>(null) }

    LaunchedEffect(Unit) {
        try {
            isBackgroundLoading = true
            com.example.ui.data.UserSession.getOrFetchAreas()
            val raw = ApiClient.apiService.getAcsDevices()
            allDevices = raw.filter { com.example.ui.data.UserSession.isAreaNameAllowed(it.areaName ?: "") }
        } catch(e: Exception) {
            errorMsg = "Gagal memuat data ACS: ${e.message}"
        } finally {
            isBackgroundLoading = false
        }
    }

    val displayedDevices = allDevices.filter { 
        (it.username ?: "").contains(searchQuery, ignoreCase = true) &&
        (!showOnlyOffline || it.isOnline != true) &&
        (selectedArea == null || it.areaName == selectedArea?.name)
    }
    
    val totalDevices = allDevices.size
    val onlineDevices = allDevices.count { it.isOnline == true }
    val offlineDevices = allDevices.count { it.isOnline != true }

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Menu ACS", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                },
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
            
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = primaryCyan
                )
            }
            
            if (errorMsg != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMsg!!, color = errorRed, fontSize = 16.sp)
                }
                return@Scaffold
            }

            // Summary Cards

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AcsSummaryText(
                    title = "Total",
                    value = totalDevices.toString(),
                    
                    textColor = Color(0xFF0055FF),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryText(
                    title = "Online",
                    value = onlineDevices.toString(),
                    
                    textColor = Color(0xFF008844),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryText(
                    title = "Offline",
                    value = offlineDevices.toString(),
                    
                    textColor = Color(0xFFDD3344),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = true }
                )
            }

            // Filters
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = textMain, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device GenieACS", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    
                    var expandedArea by remember { mutableStateOf(false) }
                    
    var areas by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            areas = com.example.ui.data.remote.ApiClient.apiService.getAreas()
        } catch(e: Exception) {
        }
    }


                    ExposedDropdownMenuBox(
                        expanded = expandedArea,
                        onExpandedChange = { expandedArea = !expandedArea }
                    ) {
                        OutlinedTextField(
                            value = selectedArea?.name ?: "Tampilkan Semua Server Area",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = bgMain,
                                unfocusedContainerColor = bgMain,
                                focusedBorderColor = primaryCyan.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedArea) }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedArea,
                            onDismissRequest = { expandedArea = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tampilkan Semua Server Area", color = textMain) },
                                onClick = {
                                    selectedArea = null
                                    expandedArea = false
                                }
                            )
                            areas.filter { com.example.ui.data.UserSession.isAreaIdAllowed(it.id) }.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area.name, color = textMain) },
                                    onClick = {
                                        selectedArea = area
                                        expandedArea = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = bgMain,
                            unfocusedContainerColor = bgMain,
                            focusedBorderColor = primaryCyan.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                }
            }

            // Device List Header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("No", color = textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.15f))
                Text("PPPoE Username", color = textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.45f))
                Text("Aksi", color = textSecondary, fontSize = 12.sp, modifier = Modifier.weight(0.4f))
            }

            // Device List
            if (displayedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Tidak ada perangkat ACS ditemukan",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Silakan pastikan konfigurasi API ACS di menu Server Area sudah benar dan perangkat Anda terhubung.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedDevices.withIndex().toList()) { (index, device) ->
                        AcsDeviceItem(
                            index = index + 1,
                            device = device,
                            cardBg = cardBg,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            warningYellow = warningYellow,
                            primaryCyan = primaryCyan,
                            errorRed = errorRed,
                            successGreen = successGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AcsSummaryText(title: String, value: String, textColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
    ) {
        Text(title, color = textColor.copy(alpha = 0.8f), fontSize = 10.sp)
        Text(value, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AcsDeviceItem(
    index: Int,
    device: AcsDevice,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    warningYellow: Color,
    primaryCyan: Color,
    errorRed: Color,
    successGreen: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    var showSsidDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showSummonDialog by remember { mutableStateOf(false) }

    var isUpdating by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val isErrorItem = device.id?.startsWith("error_") == true
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(index.toString(), color = textMain, fontSize = 14.sp, modifier = Modifier.weight(0.15f))
                Column(modifier = Modifier.weight(0.45f)) {
                    Text(
                        text = device.username ?: "Unknown",
                        color = if (isErrorItem) errorRed else textMain,
                        fontSize = 14.sp,
                        fontWeight = if (isErrorItem) FontWeight.Bold else FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (isErrorItem) warningYellow else (if (device.isOnline == true) successGreen else errorRed))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isErrorItem) "Gagal Koneksi" else (if (device.isOnline == true) "Online" else "Offline"),
                            color = if (isErrorItem) warningYellow else (if (device.isOnline == true) successGreen else errorRed),
                            fontSize = 10.sp
                        )
                        if (!device.areaName.isNullOrEmpty()) {
                            Text(" • ${device.areaName}", color = primaryCyan, fontSize = 10.sp)
                        }
                    }
                }
                
                if (!isErrorItem) {
                    Column(modifier = Modifier.weight(0.4f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AcsActionButton("Set SSID", warningYellow, Icons.Default.Build) { showSsidDialog = true }
                        AcsActionButton("Set Password", primaryCyan, Icons.Default.Build) { showPasswordDialog = true }
                        AcsActionButton("Restart", errorRed, Icons.Default.Refresh) { showRestartDialog = true }
                        AcsActionButton("Summon", successGreen, Icons.Default.Router) { showSummonDialog = true }
                    }
                } else {
                    Box(modifier = Modifier.weight(0.4f), contentAlignment = Alignment.CenterEnd) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error Connection",
                            tint = errorRed,
                            modifier = Modifier.size(24.dp).padding(end = 4.dp)
                        )
                    }
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF22222E))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isErrorItem) {
                        Text(
                            text = "Detail Kesalahan:",
                            color = errorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = device.ssid ?: "Unknown error",
                            color = textMain,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Saran Perbaikan:\n1. Pastikan port (misal :7557) diisi jika domain/IP membutuhkan port khusus.\n2. Pastikan credentials ACS User & ACS Password sudah sesuai.\n3. Periksa koneksi internet ke GenieACS server Anda.",
                            color = textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    } else {
                        AcsDetailRow("SSID", device.ssid ?: "Unknown", textMain, textSecondary)
                        AcsDetailRow("Password", device.wifiPassword ?: "-", textMain, textSecondary)
                        AcsDetailRow("User Konek", (device.connectedUsers ?: 0).toString(), textMain, textSecondary)
                        AcsDetailRow("Nomor Pelanggan", device.customerNumber ?: "-", textMain, textSecondary)
                        AcsDetailRow("Redaman", if (device.rxPower != null && device.rxPower != "-" && device.rxPower!!.isNotEmpty()) "${device.rxPower} dBm" else "-", textMain, textSecondary)
                    }
                }
            }
        }
    }

    if (showSsidDialog) {
        AcsActionDialog(
            title = "Ubah SSID",
            description = "Silahkan ubah nama wifi anda.",
            initialValue = device.ssid ?: "",
            label = "Nama WiFi Baru",
            isUpdating = isUpdating,
            updateResult = updateResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = primaryCyan,
            onDismiss = { 
                showSsidDialog = false 
                updateResult = null 
            },
            onConfirm = { newSsid ->
                isUpdating = true
                updateResult = null
                coroutineScope.launch {
                    try {
                        val res = ApiClient.apiService.acsAction(device.id ?: "", mapOf("action" to "set_ssid", "value" to newSsid, "areaName" to (device.areaName ?: "")))
                        updateResult = res.message
                        android.widget.Toast.makeText(context, "Nama WiFi (SSID) berhasil diubah!", android.widget.Toast.LENGTH_LONG).show()
                        showSsidDialog = false
                        updateResult = null
                    } catch(e: Exception) {
                        updateResult = "Gagal: ${e.message}"
                    } finally {
                        isUpdating = false
                    }
                }
            }
        )
    }

    if (showPasswordDialog) {
        AcsActionDialog(
            title = "Ubah Password",
            description = "Silahkan ubah password wifi anda.",
            initialValue = "",
            label = "Password WiFi Baru",
            isUpdating = isUpdating,
            updateResult = updateResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = primaryCyan,
            onDismiss = { 
                showPasswordDialog = false 
                updateResult = null 
            },
            onConfirm = { newPass ->
                if (newPass.length < 8) {
                    updateResult = "Gagal: Password minimal 8 karakter!"
                } else {
                    isUpdating = true
                    updateResult = null
                    coroutineScope.launch {
                        try {
                            val res = ApiClient.apiService.acsAction(device.id ?: "", mapOf("action" to "set_password", "value" to newPass, "areaName" to (device.areaName ?: "")))
                            updateResult = res.message
                            android.widget.Toast.makeText(context, "Password WiFi berhasil diubah!", android.widget.Toast.LENGTH_LONG).show()
                            showPasswordDialog = false
                            updateResult = null
                        } catch(e: Exception) {
                            updateResult = "Gagal: ${e.message}"
                        } finally {
                            isUpdating = false
                        }
                    }
                }
            }
        )
    }

    if (showRestartDialog) {
        AcsActionDialog(
            title = "Restart Perangkat",
            description = "Apakah anda yakin ingin mereboot perangkat ini?",
            showInput = false,
            isUpdating = isUpdating,
            updateResult = updateResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = primaryCyan,
            onDismiss = { 
                showRestartDialog = false 
                updateResult = null 
            },
            onConfirm = { 
                isUpdating = true
                updateResult = null
                coroutineScope.launch {
                    try {
                        val res = ApiClient.apiService.acsAction(device.id ?: "", mapOf("action" to "reboot", "areaName" to (device.areaName ?: "")))
                        updateResult = res.message
                        android.widget.Toast.makeText(context, "Perangkat berhasil direstart!", android.widget.Toast.LENGTH_LONG).show()
                        showRestartDialog = false
                        updateResult = null
                    } catch(e: Exception) {
                        updateResult = "Gagal: ${e.message}"
                    } finally {
                        isUpdating = false
                    }
                }
            }
        )
    }

    if (showSummonDialog) {
        AcsActionDialog(
            title = "Summon Perangkat",
            description = "Apakah anda yakin ingin mengirimkan perintah summon (wake up & refresh) ke perangkat ini?",
            showInput = false,
            isUpdating = isUpdating,
            updateResult = updateResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = primaryCyan,
            onDismiss = { 
                showSummonDialog = false 
                updateResult = null 
            },
            onConfirm = { 
                isUpdating = true
                updateResult = null
                coroutineScope.launch {
                    try {
                        val res = ApiClient.apiService.acsAction(device.id ?: "", mapOf("action" to "summon", "areaName" to (device.areaName ?: "")))
                        updateResult = res.message
                        android.widget.Toast.makeText(context, "Perangkat berhasil disummon!", android.widget.Toast.LENGTH_LONG).show()
                        showSummonDialog = false
                        updateResult = null
                    } catch(e: Exception) {
                        updateResult = "Gagal: ${e.message}"
                    } finally {
                        isUpdating = false
                    }
                }
            }
        )
    }
}

@Composable
fun AcsDetailRow(label: String, value: String, textMain: Color, textSecondary: Color) {
    Row(modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AcsActionButton(text: String, bgColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
            Text(text, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AcsActionDialog(
    title: String,
    description: String,
    initialValue: String = "",
    label: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isUpdating: Boolean,
    updateResult: String?,
    showInput: Boolean = true,
    bgMain: Color = Color(0xFF05050A),
    textMain: Color = Color(0xFFFFFFFF),
    textSecondary: Color = Color(0xFFAAAAAA),
    cardBg: Color = Color(0xFF11111A),
    primaryCyan: Color = Color(0xFF00FFFF)
) {
    var inputValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        containerColor = bgMain,
        title = { Text(title, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(description, color = textSecondary, fontSize = 14.sp)
                
                if (showInput) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text(label, color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                }

                if (isUpdating) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = primaryCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Menunggu respon dari GenieACS...", color = primaryCyan, fontSize = 12.sp)
                    }
                } else if (updateResult != null) {
                    val isSuccess = updateResult.contains("berhasil", ignoreCase = true)
                    Text(
                        text = updateResult,
                        color = if (isSuccess) Color(0xFF00FF00) else Color(0xFFFF003C),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(inputValue) },
                enabled = !isUpdating,
                colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black)
            ) {
                Text("Konfirmasi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUpdating) {
                Text("Batal", color = textMain)
            }
        }
    )
}
