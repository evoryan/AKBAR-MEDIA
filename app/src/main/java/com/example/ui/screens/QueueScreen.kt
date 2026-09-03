package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.MikrotikQueue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgMain = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val headerBg = if (isDark) Color(0xFF1F0216) else Color(0xFFFFEBF5)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (isDark) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF00FFFF).copy(alpha = 0.3f) else Color(0xFF0066FF).copy(alpha = 0.3f)
    val neonCyan = if (isDark) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val neonGreen = Color(0xFF00FF4D)
    val errorRed = Color(0xFFFF003C)

    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var areaDropdownExpanded by remember { mutableStateOf(false) }

    var queueList by remember { mutableStateOf<List<MikrotikQueue>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, ACTIVE, DISABLED

    fun loadQueues(areaId: String) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = ApiClient.apiService.getMikrotikQueues(areaId)
                queueList = result
            } catch (e: Exception) {
                errorMessage = e.message ?: "Gagal memuat queue dari MikroTik"
                queueList = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val fetchedAreas = UserSession.getOrFetchAreas()
            val allowed = fetchedAreas.filter { it.routerIp.isNotEmpty() && UserSession.isAreaIdAllowed(it.id) }
            areas = allowed
            if (allowed.isNotEmpty()) {
                selectedArea = allowed.first()
                loadQueues(allowed.first().id)
            }
        } catch (e: Exception) {
            errorMessage = "Gagal memuat daftar area"
        }
    }

    val filteredQueues = remember(queueList, searchQuery, filterStatus) {
        queueList.filter { queue ->
            val matchesSearch = searchQuery.isBlank() ||
                    queue.name.contains(searchQuery, ignoreCase = true) ||
                    queue.target.contains(searchQuery, ignoreCase = true) ||
                    queue.comment.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "ACTIVE" -> !queue.disabled
                "DISABLED" -> queue.disabled
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val totalQueues = queueList.size
    val activeQueues = queueList.count { !it.disabled }
    val disabledQueues = queueList.count { it.disabled }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MikroTik Queue",
                            color = textMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            selectedArea?.name ?: "Pilih Area",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textMain
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedArea?.let { loadQueues(it.id) }
                                ?: Toast.makeText(context, "Pilih area terlebih dahulu", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = textMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bgMain)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Area Selector Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { areaDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Router,
                                contentDescription = null,
                                tint = neonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedArea?.let { "Area: ${it.name} (${it.routerIp})" } ?: "Pilih Area / Router",
                                color = textMain,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                    }
                }
                DropdownMenu(
                    expanded = areaDropdownExpanded,
                    onDismissRequest = { areaDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(cardBg)
                        .border(1.dp, cardBorder)
                ) {
                    if (areas.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Tidak ada area terkonfigurasi", color = textSecondary) },
                            onClick = { areaDropdownExpanded = false }
                        )
                    } else {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold)
                                        Text(area.routerIp, color = textSecondary, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedArea = area
                                    areaDropdownExpanded = false
                                    loadQueues(area.id)
                                }
                            )
                        }
                    }
                }
            }

            // Summary Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QueueStatCard(
                    title = "Total Queue",
                    value = if (isLoading) "..." else "$totalQueues",
                    color = neonCyan,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    modifier = Modifier.weight(1f)
                )
                QueueStatCard(
                    title = "Aktif",
                    value = if (isLoading) "..." else "$activeQueues",
                    color = neonGreen,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    modifier = Modifier.weight(1f)
                )
                QueueStatCard(
                    title = "Disabled",
                    value = if (isLoading) "..." else "$disabledQueues",
                    color = errorRed,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari Queue, Target IP, atau Komentar...", color = textSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )

            // Filter Chips (Semua, Aktif, Nonaktif)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("Semua ($totalQueues)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = neonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = neonCyan,
                        labelColor = textSecondary
                    )
                )
                FilterChip(
                    selected = filterStatus == "ACTIVE",
                    onClick = { filterStatus = "ACTIVE" },
                    label = { Text("Aktif ($activeQueues)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = neonGreen.copy(alpha = 0.2f),
                        selectedLabelColor = neonGreen,
                        labelColor = textSecondary
                    )
                )
                FilterChip(
                    selected = filterStatus == "DISABLED",
                    onClick = { filterStatus = "DISABLED" },
                    label = { Text("Disabled ($disabledQueues)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = errorRed.copy(alpha = 0.2f),
                        selectedLabelColor = errorRed,
                        labelColor = textSecondary
                    )
                )
            }

            // Content Area: Loading, Error, or List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = neonCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Mengambil data Queue dari MikroTik...", color = textSecondary, fontSize = 14.sp)
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = errorRed, modifier = Modifier.size(48.dp))
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            color = errorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { selectedArea?.let { loadQueues(it.id) } },
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Coba Lagi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (filteredQueues.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (searchQuery.isNotEmpty()) "Tidak ada queue yang cocok dengan pencarian"
                            else "Tidak ada data queue di router ini",
                            color = textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredQueues, key = { it.id }) { queue ->
                        QueueItemCard(
                            queue = queue,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            neonCyan = neonCyan,
                            neonGreen = neonGreen,
                            errorRed = errorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueStatCard(
    title: String,
    value: String,
    color: Color,
    cardBg: Color,
    cardBorder: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = color.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun QueueItemCard(
    queue: MikrotikQueue,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    neonGreen: Color,
    errorRed: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (queue.disabled) cardBorder.copy(alpha = 0.4f) else cardBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row: Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (queue.disabled) Color.Gray else neonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = queue.name,
                        color = if (queue.disabled) textSecondary else textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (queue.disabled) Color.DarkGray.copy(alpha = 0.5f)
                            else neonGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (queue.disabled) "DISABLED" else "AKTIF",
                        color = if (queue.disabled) Color.LightGray else neonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Target IP/Interface
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lan,
                    contentDescription = null,
                    tint = neonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Target: ${queue.target}",
                    color = textMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comment if present
            if (queue.comment.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = queue.comment,
                        color = textSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.2f), thickness = 0.5.dp)

            // Bandwidth Row (Max Limit & Rate)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max Limit", color = textSecondary, fontSize = 11.sp)
                    Text(
                        formatBandwidthPair(queue.maxLimit),
                        color = neonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (queue.rate.isNotBlank() && queue.rate != "0/0" && queue.rate != "-") {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Current Rate", color = textSecondary, fontSize = 11.sp)
                        Text(
                            formatBandwidthPair(queue.rate),
                            color = neonGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Traffic & Packets Row
            if (queue.bytes.isNotBlank() && queue.bytes != "-") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Traffic (Bytes)", color = textSecondary, fontSize = 11.sp)
                        Text(
                            formatBytesPair(queue.bytes),
                            color = textMain,
                            fontSize = 12.sp
                        )
                    }

                    if (queue.packets.isNotBlank() && queue.packets != "-") {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Packets", color = textSecondary, fontSize = 11.sp)
                            Text(
                                queue.packets,
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatBandwidthPair(pair: String): String {
    if (pair.isBlank() || pair == "-") return "Unlimited"
    val parts = pair.split("/")
    if (parts.size == 2) {
        val up = formatRateValue(parts[0].trim())
        val down = formatRateValue(parts[1].trim())
        return "↑ $up / ↓ $down"
    }
    return pair
}

private fun formatRateValue(v: String): String {
    val num = v.toLongOrNull() ?: return v
    if (num <= 0) return "0"
    if (num >= 1_000_000_000) return "${num / 1_000_000_000} Gbps"
    if (num >= 1_000_000) return "${num / 1_000_000} Mbps"
    if (num >= 1_000) return "${num / 1_000} kbps"
    return "$num bps"
}

private fun formatBytesPair(pair: String): String {
    if (pair.isBlank() || pair == "-") return "-"
    val parts = pair.split("/")
    if (parts.size == 2) {
        val up = formatByteSize(parts[0].trim().toLongOrNull() ?: 0L)
        val down = formatByteSize(parts[1].trim().toLongOrNull() ?: 0L)
        return "↑ $up / ↓ $down"
    }
    val bytes = pair.toLongOrNull() ?: return pair
    return formatByteSize(bytes)
}

private fun formatByteSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
