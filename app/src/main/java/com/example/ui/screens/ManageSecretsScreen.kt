package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
    var isLoading by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf(SecretFilter.ALL) }
    
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
        when (currentFilter) {
            SecretFilter.ALL -> true
            SecretFilter.ONLINE -> it.status == "Online"
            SecretFilter.OFFLINE -> it.status == "Offline"
            SecretFilter.DISABLED -> it.status == "Disabled"
        }
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Total Secrets",
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
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        title = "Offline",
                        value = totalOffline.toString(),
                        color = textSecondary,
                        isSelected = currentFilter == SecretFilter.OFFLINE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            currentFilter = SecretFilter.OFFLINE
                            isLoading = true
                            coroutineScope.launch { delay(500); isLoading = false }
                        }
                    )
                    SummaryCard(
                        title = "Disabled",
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
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daftar Secrets (${currentFilter.name})",
                    color = textMain,
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
                            cardBg = cardBg,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            neonCyan = neonCyan,
                            successGreen = successGreen,
                            errorRed = errorRed,
                            onDisable = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Disabled")
                            },
                            onEnable = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline")
                            },
                            onRemoveActive = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline", ipAddress = "", uptime = "")
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
            Text(title, color = Color(0xFFAAAAAA), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Klik Disini", color = color.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
fun SecretItem(
    secret: PPPoESecret,
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = neonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(secret.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(secret.profile, color = textSecondary, fontSize = 12.sp)
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
                            Text("IP Address", color = textSecondary, fontSize = 12.sp)
                            Text(secret.ipAddress, color = neonCyan, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Uptime", color = textSecondary, fontSize = 12.sp)
                            Text(secret.uptime, color = textMain, fontSize = 12.sp)
                        }
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
