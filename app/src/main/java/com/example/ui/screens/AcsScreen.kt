package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squareup.moshi.Json
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.UserSession

data class AcsDevice(
    val id: String?,
    val username: String?,
    val isOnline: Boolean?,
    val ssid: String? = "Unknown",
    val wifiPassword: String? = "-",
    val connectedUsers: Int? = 0,
    val customerNumber: String? = "-",
    val rxPower: String? = "-",
    val areaName: String? = "",
    val serialNumber: String? = "-",
    @Json(name = "DeviceID.SerialNumber")
    val deviceIdSerialNumber: String? = "-",
    val connectedHostnames: List<String>? = emptyList(),
    @Json(name = "InternetGatewayDevice")
    val internetGatewayDevice: Any? = null,
    @Json(name = "Device")
    val deviceTr181: Any? = null,
    @Json(name = "InternetGatewayDevice.LANDevice.1.Hosts.Host")
    val landeviceHosts: Any? = null,
    @Json(name = "InternetGatewayDevice.LANDevice.1.Hosts.Host.HostName")
    val landeviceHostName: Any? = null,
    @Json(name = "CLIENT DEVICES")
    val clientDevicesUpper: Any? = null,
    @Json(name = "clientDevices")
    val clientDevicesCamel: Any? = null,
    @Json(name = "client_devices")
    val clientDevicesSnake: Any? = null,
    @Json(name = "HostName")
    val hostNameParam: Any? = null,
    @Json(name = "hosts")
    val hostsField: Any? = null,
    @Json(name = "VirtualParameters.connectedDevices")
    val vpConnectedDevices: Any? = null,
    @Json(name = "VirtualParameters.connected_devices")
    val vpConnectedDevicesSnake: Any? = null,
    @Json(name = "VirtualParameters.Hosts")
    val vpHosts: Any? = null,
    @Json(name = "VirtualParameters.Host")
    val vpHost: Any? = null,
    @Json(name = "VirtualParameters.activeDevices")
    val vpActiveDevices: Any? = null,
    @Json(name = "VirtualParameters.clients")
    val vpClients: Any? = null,
    @Json(name = "VirtualParameters")
    val virtualParametersObj: Any? = null,
    @Json(name = "virtualParameters")
    val virtualParametersObjLower: Any? = null,
    @Json(name = "connectedDevices")
    val connectedDevicesField: Any? = null,
    @Json(name = "connected_devices")
    val connectedDevicesSnakeField: Any? = null,
    @Json(name = "clients")
    val clientsField: Any? = null
)

/**
 * Extracts list of connected hostnames from GenieACS InternetGatewayDevice.LANDevice.1.Hosts.Host
 * parameter-table and HostName childParameters, Virtual Parameters, or connectedHostnames.
 */
fun getConnectedDeviceHostnames(device: AcsDevice): List<String> {
    val results = mutableListOf<String>()

    // 1. Direct string list if available
    device.connectedHostnames?.filter { it.isNotBlank() }?.let {
        if (it.isNotEmpty()) return it
    }

    fun parseAnyHostValue(raw: Any?): List<String> {
        if (raw == null) return emptyList()
        val found = mutableListOf<String>()

        fun extractSingleString(str: String) {
            val trimmed = str.trim()
            if (trimmed.isEmpty() || trimmed == "-" || trimmed == "null" || trimmed == "[]" || trimmed == "{}") return

            // Stringified JSON array
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                val content = trimmed.substring(1, trimmed.length - 1)
                val items = content.split(",").map {
                    it.trim().removeSurrounding("\"").removeSurrounding("'").trim()
                }
                items.filter { it.isNotBlank() && !it.startsWith("{") && it != "-" && it != "null" }.forEach { found.add(it) }
                return
            }

            // Split by comma, semicolon, pipe, or newline
            if (trimmed.contains(",") || trimmed.contains(";") || trimmed.contains("\n") || trimmed.contains("|")) {
                val delimiters = Regex("[,;|\n\r]+")
                val parts = trimmed.split(delimiters).map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                parts.filter { it.isNotBlank() && it != "-" && it != "null" }.forEach { found.add(it) }
            } else {
                found.add(trimmed.removeSurrounding("\"").removeSurrounding("'"))
            }
        }

        fun parseNode(node: Any?) {
            if (node == null) return
            when (node) {
                is String -> extractSingleString(node)
                is List<*> -> {
                    for (item in node) parseNode(item)
                }
                is Map<*, *> -> {
                    val rawName = node["HostName"] ?: node["hostName"] ?: node["hostname"]
                        ?: node["Nama Perangkat (Host)"] ?: node["Nama Perangkat"]
                        ?: node["Name"] ?: node["name"]
                    
                    val nameVal = when (rawName) {
                        is String -> rawName
                        is Map<*, *> -> (rawName["_value"] ?: rawName["value"])?.toString()
                        else -> rawName?.toString()
                    }

                    if (!nameVal.isNullOrBlank() && nameVal != "Unknown" && nameVal != "null" && nameVal != "-") {
                        found.add(nameVal)
                    } else {
                        val valueObj = node["_value"] ?: node["value"]
                        if (valueObj != null && valueObj !is Map<*, *>) {
                            extractSingleString(valueObj.toString())
                        } else {
                            var foundChild = false
                            for ((k, v) in node) {
                                val keyStr = k.toString()
                                if (keyStr == "HostName" || keyStr.endsWith(".HostName") || keyStr == "Nama Perangkat (Host)" || keyStr == "Host") {
                                    parseNode(v)
                                    foundChild = true
                                }
                            }
                            if (!foundChild) {
                                val rawIp = node["IPAddress"] ?: node["ipAddress"] ?: node["ip"]
                                val ipVal = when (rawIp) {
                                    is String -> rawIp
                                    is Map<*, *> -> (rawIp["_value"] ?: rawIp["value"])?.toString()
                                    else -> rawIp?.toString()
                                }
                                if (!ipVal.isNullOrBlank() && ipVal != "null") {
                                    found.add("Host ($ipVal)")
                                } else {
                                    val rawMac = node["MACAddress"] ?: node["macAddress"] ?: node["mac"]
                                    val macVal = when (rawMac) {
                                        is String -> rawMac
                                        is Map<*, *> -> (rawMac["_value"] ?: rawMac["value"])?.toString()
                                        else -> rawMac?.toString()
                                    }
                                    if (!macVal.isNullOrBlank() && macVal != "null") {
                                        found.add("Host ($macVal)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        parseNode(raw)
        return found.distinct()
    }

    // 2. Parse InternetGatewayDevice.LANDevice.1.Hosts.Host & CLIENT DEVICES parameter-table sources
    val mainTableSources = listOfNotNull(
        device.landeviceHosts,
        device.landeviceHostName,
        device.clientDevicesUpper,
        device.clientDevicesCamel,
        device.clientDevicesSnake,
        device.hostNameParam,
        device.hostsField
    )

    for (src in mainTableSources) {
        val parsed = parseAnyHostValue(src)
        if (parsed.isNotEmpty()) {
            results.addAll(parsed)
        }
    }

    if (results.isNotEmpty()) return results.distinct()

    // 3. Parse Virtual Parameter sources
    val vpSources = listOfNotNull(
        device.vpConnectedDevices,
        device.vpConnectedDevicesSnake,
        device.vpHosts,
        device.vpHost,
        device.vpActiveDevices,
        device.vpClients,
        device.connectedDevicesField,
        device.connectedDevicesSnakeField,
        device.clientsField
    )

    for (src in vpSources) {
        val parsed = parseAnyHostValue(src)
        if (parsed.isNotEmpty()) {
            results.addAll(parsed)
        }
    }

    // 4. Parse VirtualParameters object container if present
    val vpContainers = listOfNotNull(device.virtualParametersObj, device.virtualParametersObjLower)
    for (container in vpContainers) {
        if (container is Map<*, *>) {
            for ((k, v) in container) {
                val keyStr = k.toString().lowercase()
                if (keyStr.contains("device") || keyStr.contains("host") || keyStr.contains("client") || keyStr.contains("user")) {
                    results.addAll(parseAnyHostValue(v))
                }
            }
        }
    }

    return results.distinct()
}

/**
 * Detailed connected host information extracted from TR-069 (InternetGatewayDevice.LANDevice.1.Hosts.Host)
 * or TR-181 (Device.Hosts.Host) or Virtual Parameters.
 */
data class AcsHostInfo(
    val hostName: String,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val interfaceType: String? = null,
    val isActive: Boolean = true
)

/**
 * Parses full host details (HostName._value, IPAddress._value, MACAddress._value, InterfaceType._value, Active._value)
 * from GenieACS JSON tree response according to TR-069 & TR-181 standards.
 */
fun getConnectedHostInfoList(device: AcsDevice): List<AcsHostInfo> {
    val list = mutableListOf<AcsHostInfo>()

    fun extractValString(obj: Any?): String? {
        if (obj == null) return null
        if (obj is String) return obj.trim().takeIf { it.isNotEmpty() && it != "null" && it != "-" }
        if (obj is Map<*, *>) {
            val v = obj["_value"] ?: obj["value"]
            return v?.toString()?.trim()?.takeIf { it.isNotEmpty() && it != "null" && it != "-" }
        }
        return obj.toString().trim().takeIf { it.isNotEmpty() && it != "null" && it != "-" }
    }

    fun extractValBool(obj: Any?): Boolean {
        if (obj == null) return true
        if (obj is Boolean) return obj
        if (obj is Map<*, *>) {
            val v = obj["_value"] ?: obj["value"]
            if (v is Boolean) return v
            val str = v?.toString()?.lowercase()
            if (str == "false" || str == "0") return false
            if (str == "true" || str == "1") return true
        }
        val str = obj.toString().lowercase()
        if (str == "false" || str == "0") return false
        return true
    }

    fun parseHostMap(hostMap: Map<*, *>) {
        val name = extractValString(hostMap["HostName"] ?: hostMap["hostName"] ?: hostMap["hostname"] ?: hostMap["Nama Perangkat (Host)"] ?: hostMap["Name"])
        val ip = extractValString(hostMap["IPAddress"] ?: hostMap["ipAddress"] ?: hostMap["ip"])
        val mac = extractValString(hostMap["MACAddress"] ?: hostMap["macAddress"] ?: hostMap["mac"])
        val type = extractValString(hostMap["InterfaceType"] ?: hostMap["interfaceType"] ?: hostMap["type"])
        val active = extractValBool(hostMap["Active"] ?: hostMap["active"])

        val displayName = when {
            !name.isNullOrEmpty() -> name
            !ip.isNullOrEmpty() -> "Host ($ip)"
            !mac.isNullOrEmpty() -> "Host ($mac)"
            else -> null
        }

        if (displayName != null) {
            list.add(
                AcsHostInfo(
                    hostName = displayName,
                    ipAddress = ip,
                    macAddress = mac,
                    interfaceType = type,
                    isActive = active
                )
            )
        }
    }

    fun parseHostNode(node: Any?) {
        if (node == null) return
        when (node) {
            is List<*> -> {
                for (item in node) parseHostNode(item)
            }
            is Map<*, *> -> {
                val hostProp = node["Host"] ?: node["host"]
                if (hostProp != null) {
                    parseHostNode(hostProp)
                } else if (node.containsKey("HostName") || node.containsKey("IPAddress") || node.containsKey("MACAddress") || node.containsKey("InterfaceType")) {
                    parseHostMap(node)
                } else {
                    for ((_, v) in node) {
                        parseHostNode(v)
                    }
                }
            }
        }
    }

    // 1. Check TR-069 tree: InternetGatewayDevice -> LANDevice -> 1..n -> Hosts -> Host
    val igd = device.internetGatewayDevice
    if (igd is Map<*, *>) {
        val lanDev = igd["LANDevice"] ?: igd["lanDevice"]
        if (lanDev is Map<*, *>) {
            for ((_, lanInst) in lanDev) {
                if (lanInst is Map<*, *>) {
                    val hostsObj = lanInst["Hosts"] ?: lanInst["hosts"]
                    if (hostsObj is Map<*, *>) {
                        val hostArray = hostsObj["Host"] ?: hostsObj["host"]
                        parseHostNode(hostArray)
                    }
                }
            }
        }
    }

    // 2. Check TR-181 tree: Device -> Hosts -> Host
    val devTr181 = device.deviceTr181
    if (devTr181 is Map<*, *>) {
        val hostsObj = devTr181["Hosts"] ?: devTr181["hosts"]
        if (hostsObj is Map<*, *>) {
            val hostArray = hostsObj["Host"] ?: hostsObj["host"]
            parseHostNode(hostArray)
        }
    }

    // 3. Check directly mapped landeviceHosts, hostsField, etc.
    if (list.isEmpty()) {
        parseHostNode(device.landeviceHosts)
        parseHostNode(device.hostsField)
        parseHostNode(device.clientDevicesUpper)
        parseHostNode(device.clientDevicesCamel)
        parseHostNode(device.clientDevicesSnake)
        parseHostNode(device.vpConnectedDevices)
        parseHostNode(device.vpHosts)
    }

    // 4. Fallback to getConnectedDeviceHostnames
    if (list.isEmpty()) {
        val plainHostnames = getConnectedDeviceHostnames(device)
        for (hn in plainHostnames) {
            list.add(AcsHostInfo(hostName = hn, isActive = true))
        }
    }

    return list.distinctBy { it.hostName + (it.macAddress ?: "") }
}

/**
 * Extracts clean ONT Serial Number from DeviceID.SerialNumber, serialNumber field,
 * or parses OUI-ProductClass-SerialNumber formatted GenieACS ID (e.g., 202B1D-F670L-ZTEGC882194D -> ZTEGC882194D).
 */
fun getOntSerialNumber(device: AcsDevice): String {
    val rawSn = device.deviceIdSerialNumber?.takeIf { it.isNotBlank() && it != "-" }
        ?: device.serialNumber?.takeIf { it.isNotBlank() && it != "-" }

    if (!rawSn.isNullOrEmpty()) return rawSn

    val devId = device.id ?: return "-"
    if (devId.startsWith("error_")) return "-"

    if (devId.contains("-")) {
        val parts = devId.split("-")
        if (parts.size >= 3) {
            return parts.last()
        }
    }
    return devId
}

/**
 * Calculates background color for RX Power (Redaman):
 * - 0.0 dBm s/d -15.0 dBm = Biru (#0284C7)
 * - -15.1 dBm s/d -27.0 dBm = Hijau (#10B981)
 * - -27.1 dBm s/d -50.0 dBm = Merah (#EF4444)
 */
fun getRxPowerColor(rxPowerStr: String?): Color {
    if (rxPowerStr.isNullOrEmpty() || rxPowerStr == "-") return Color(0xFF64748B)
    val cleanStr = rxPowerStr.replace("dBm", "", ignoreCase = true).trim()
    val value = cleanStr.toFloatOrNull() ?: return Color(0xFF64748B)

    return when {
        value >= -15.0f && value <= 0.0f -> Color(0xFF0284C7)  // Biru (0.0 dBm - -15.0 dBm)
        value >= -27.0f && value < -15.0f -> Color(0xFF10B981) // Hijau (-15.1 dBm - -27.0 dBm)
        value >= -50.0f && value < -27.0f -> Color(0xFFEF4444) // Merah (-27.1 dBm - -50.0 dBm)
        value > 0.0f -> Color(0xFF0284C7)                       // Biru
        else -> Color(0xFFEF4444)                              // Merah
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcsScreen(onBack: () -> Unit, initialSearchQuery: String = "") {
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF1F5F9)
    val headerBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF1F0216) else Color(0xFFE2E8F0)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF475569)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF262636) else Color(0xFFCBD5E1)
    val primaryCyan = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0265DC)
    val successGreen = Color(0xFF00FF4D)
    val errorRed = Color(0xFFFF003C)
    val warningYellow = Color(0xFFFFB800)

    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    var showOnlyOffline by remember { mutableStateOf(false) }
    
    var allDevices by remember { mutableStateOf<List<AcsDevice>>(emptyList()) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Currently selected device for Detail Dialog
    var selectedDeviceForDetail by remember { mutableStateOf<AcsDevice?>(null) }

    // Action dialog states
    var activeActionDevice by remember { mutableStateOf<AcsDevice?>(null) }
    var showSsidDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showSummonDialog by remember { mutableStateOf(false) }

    var isUpdating by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }

    fun loadAcsDevices() {
        coroutineScope.launch {
            try {
                isBackgroundLoading = true
                errorMsg = null
                UserSession.getOrFetchAreas()
                val raw = ApiClient.apiService.getAcsDevices()
                allDevices = raw.filter { UserSession.isAreaNameAllowed(it.areaName ?: "") }
            } catch(e: Exception) {
                errorMsg = "Gagal memuat data ACS: ${e.message}"
            } finally {
                isBackgroundLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAcsDevices()
    }

    // Search filter includes username, tag / customer number, SN ONT, SSID, and areaName
    val displayedDevices = allDevices.filter { device ->
        val query = searchQuery.trim().lowercase()
        val sn = getOntSerialNumber(device).lowercase()
        val matchesSearch = query.isEmpty() ||
            (device.username ?: "").lowercase().contains(query) ||
            (device.customerNumber ?: "").lowercase().contains(query) ||
            sn.contains(query) ||
            (device.ssid ?: "").lowercase().contains(query) ||
            (device.areaName ?: "").lowercase().contains(query)

        val matchesStatus = !showOnlyOffline || device.isOnline != true
        val matchesArea = selectedArea == null || device.areaName == selectedArea?.name

        matchesSearch && matchesStatus && matchesArea
    }
    
    val totalDevices = allDevices.size
    val onlineDevices = allDevices.count { it.isOnline == true }
    val offlineDevices = allDevices.count { it.isOnline != true }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Menu ACS", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Remote & Monitoring Modem ONT TR-069", color = textSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { loadAcsDevices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh ACS", tint = textMain)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = primaryCyan
                )
            }
            
            if (errorMsg != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, errorRed.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = errorRed, modifier = Modifier.size(40.dp))
                            Text(
                                "Gagal Terhubung ke ACS (NBI Service)",
                                color = textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                errorMsg!!,
                                color = errorRed,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Tips: Pastikan service 'genieacs-nbi' aktif di port 7557 pada VPS Anda dan URL ACS pada menu 'Kelola Area' mengarah ke port NBI (contoh: http://ip-vps:7557).",
                                color = textSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { loadAcsDevices() },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coba Lagi", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                return@Scaffold
            }

            // Summary Statistics Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AcsSummaryCard(
                    title = "Total",
                    value = totalDevices.toString(),
                    textColor = Color(0xFF38BDF8),
                    bgColor = cardBg,
                    border = cardBorder,
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryCard(
                    title = "Online",
                    value = onlineDevices.toString(),
                    textColor = successGreen,
                    bgColor = cardBg,
                    border = cardBorder,
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryCard(
                    title = "Offline",
                    value = offlineDevices.toString(),
                    textColor = errorRed,
                    bgColor = cardBg,
                    border = cardBorder,
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = true }
                )
            }

            // Filter & Search Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Device GenieACS", color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    var expandedArea by remember { mutableStateOf(false) }
                    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
                    
                    LaunchedEffect(Unit) {
                        try {
                            areas = ApiClient.apiService.getAreas()
                        } catch(e: Exception) {
                            // Handled silently
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
                                unfocusedBorderColor = cardBorder,
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
                            areas.filter { UserSession.isAreaIdAllowed(it.id) }.forEach { area ->
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
                        placeholder = { Text("Cari username, tag, SN ONT, SSID...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = bgMain,
                            unfocusedContainerColor = bgMain,
                            focusedBorderColor = primaryCyan.copy(alpha = 0.5f),
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                }
            }

            // Section Info Label
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Perangkat ONT (${displayedDevices.size})",
                    color = textMain,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap kartu untuk detail",
                    color = primaryCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Device List - Modern Card List UI
            if (displayedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp),
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
                            text = "Silakan pastikan kata kunci pencarian/tag/SN benar atau perangkat terhubung ke GenieACS server.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedDevices) { device ->
                        AcsDeviceCard(
                            device = device,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            primaryCyan = primaryCyan,
                            successGreen = successGreen,
                            errorRed = errorRed,
                            warningYellow = warningYellow,
                            onClick = {
                                selectedDeviceForDetail = device
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Detail Dialog (Tap to open detail with SN ONT)
    if (selectedDeviceForDetail != null) {
        val device = selectedDeviceForDetail!!
        AcsDeviceDetailModal(
            device = device,
            cardBg = cardBg,
            cardBorder = cardBorder,
            textMain = textMain,
            textSecondary = textSecondary,
            primaryCyan = primaryCyan,
            successGreen = successGreen,
            errorRed = errorRed,
            warningYellow = warningYellow,
            onDismiss = { selectedDeviceForDetail = null },
            onSetSsid = {
                activeActionDevice = device
                showSsidDialog = true
            },
            onSetPassword = {
                activeActionDevice = device
                showPasswordDialog = true
            },
            onRestart = {
                activeActionDevice = device
                showRestartDialog = true
            },
            onSummon = {
                activeActionDevice = device
                showSummonDialog = true
            }
        )
    }

    // Action Dialogs
    if (showSsidDialog && activeActionDevice != null) {
        val device = activeActionDevice!!
        AcsActionDialog(
            title = "Ubah SSID WiFi",
            description = "Silakan masukkan nama WiFi (SSID) baru untuk perangkat ${device.username}:",
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

    if (showPasswordDialog && activeActionDevice != null) {
        val device = activeActionDevice!!
        AcsActionDialog(
            title = "Ubah Password WiFi",
            description = "Silakan masukkan password WiFi baru untuk perangkat ${device.username}:",
            initialValue = "",
            label = "Password WiFi Baru (Min 8 Karakter)",
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

    if (showRestartDialog && activeActionDevice != null) {
        val device = activeActionDevice!!
        AcsActionDialog(
            title = "Restart Perangkat ONT",
            description = "Apakah Anda yakin ingin mengirim perintah restart (reboot) ke perangkat ${device.username}?",
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

    if (showSummonDialog && activeActionDevice != null) {
        val device = activeActionDevice!!
        AcsActionDialog(
            title = "Summon Perangkat ONT",
            description = "Apakah Anda yakin ingin mengirimkan perintah summon (wake up & refresh telemetri) ke perangkat ${device.username}?",
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
fun AcsSummaryCard(
    title: String,
    value: String,
    textColor: Color,
    bgColor: Color,
    border: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(title, color = textColor.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AcsDeviceCard(
    device: AcsDevice,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    primaryCyan: Color,
    successGreen: Color,
    errorRed: Color,
    warningYellow: Color,
    onClick: () -> Unit
) {
    val isErrorItem = device.id?.startsWith("error_") == true
    val isOnline = device.isOnline == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, if (isErrorItem) errorRed.copy(alpha = 0.4f) else cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Header: Status & Server Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isErrorItem -> warningYellow.copy(alpha = 0.15f)
                        isOnline -> successGreen.copy(alpha = 0.15f)
                        else -> errorRed.copy(alpha = 0.15f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isErrorItem -> warningYellow.copy(alpha = 0.4f)
                            isOnline -> successGreen.copy(alpha = 0.4f)
                            else -> errorRed.copy(alpha = 0.4f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isErrorItem -> warningYellow
                                        isOnline -> successGreen
                                        else -> errorRed
                                    }
                                )
                        )
                        Text(
                            text = when {
                                isErrorItem -> "Gagal Koneksi"
                                isOnline -> "ONLINE"
                                else -> "OFFLINE"
                            },
                            color = when {
                                isErrorItem -> warningYellow
                                isOnline -> successGreen
                                else -> errorRed
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!device.areaName.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryCyan.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryCyan.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = device.areaName,
                            color = primaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Main Details Row: Username & Tag Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = primaryCyan.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.SignalWifi4Bar else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (isOnline) successGreen else errorRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = device.username ?: "Unknown Device",
                        color = if (isErrorItem) errorRed else textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Tag Badge (Replaces "No. Pelanggan")
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryCyan.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(11.dp))
                            Text(
                                text = "Tag: ${device.customerNumber ?: "-"}",
                                color = primaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Detail",
                    tint = primaryCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.6f))

            // Footer info: WiFi SSID & Password directly on card, Connected Users, & RX Power (Redaman)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WiFi SSID & Password
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = textSecondary, modifier = Modifier.size(13.dp))
                        Text(
                            text = "SSID: ${device.ssid ?: "Unknown"}",
                            color = textMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = warningYellow, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Pass: ${device.wifiPassword ?: "-"}",
                            color = warningYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                // Badges: Connected users & RX Power (Redaman with WHITE TEXT)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF222233)
                    ) {
                        Text(
                            text = "${device.connectedUsers ?: 0} User",
                            color = textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    val rxVal = device.rxPower ?: "-"
                    if (rxVal != "-" && rxVal.isNotEmpty()) {
                        val rxBgColor = getRxPowerColor(rxVal)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = rxBgColor,
                            border = androidx.compose.foundation.BorderStroke(1.dp, rxBgColor)
                        ) {
                            Text(
                                text = "$rxVal dBm",
                                color = Color.White, // WHITE TEXT COLOR
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcsDeviceDetailModal(
    device: AcsDevice,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    primaryCyan: Color,
    successGreen: Color,
    errorRed: Color,
    warningYellow: Color,
    onDismiss: () -> Unit,
    onSetSsid: () -> Unit,
    onSetPassword: () -> Unit,
    onRestart: () -> Unit,
    onSummon: () -> Unit
) {
    val isErrorItem = device.id?.startsWith("error_") == true
    val isOnline = device.isOnline == true

    // Clean ONT Serial Number extracted from device properties or GenieACS ID
    val ontSnDisplay = getOntSerialNumber(device)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = primaryCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Detail Perangkat ONT",
                            color = textMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = device.username ?: "Unknown",
                            color = primaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = textSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isErrorItem -> warningYellow.copy(alpha = 0.15f)
                        isOnline -> successGreen.copy(alpha = 0.15f)
                        else -> errorRed.copy(alpha = 0.15f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isErrorItem -> warningYellow.copy(alpha = 0.4f)
                            isOnline -> successGreen.copy(alpha = 0.4f)
                            else -> errorRed.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isErrorItem -> warningYellow
                                        isOnline -> successGreen
                                        else -> errorRed
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isErrorItem -> "Gagal Terhubung ke ACS"
                                isOnline -> "ONLINE • Terhubung ke ACS"
                                else -> "OFFLINE • Tidak Merespon"
                            },
                            color = when {
                                isErrorItem -> warningYellow
                                isOnline -> successGreen
                                else -> errorRed
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isErrorItem) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = errorRed.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, errorRed.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Detail Kesalahan:", color = errorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(device.ssid ?: "Unknown error", color = textMain, fontSize = 12.sp)
                            Text(
                                "Saran Perbaikan:\n1. Pastikan port NBI (misal :7557) diisi pada URL ACS Server Area.\n2. Pastikan credentials ACS User & Password di Kelola Area sudah sesuai.\n3. Periksa koneksi internet ke GenieACS VPS server Anda.",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // Telemetry Grid (Label changed to "SN ONT")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D0D16))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AcsDetailRowModal("SN ONT", ontSnDisplay, primaryCyan, textSecondary)
                        AcsDetailRowModal("Nama PPPoE", device.username ?: "-", textMain, textSecondary)
                        AcsDetailRowModal("Tag / No. Pelanggan", device.customerNumber ?: "-", primaryCyan, textSecondary)
                        AcsDetailRowModal("Server Area", device.areaName ?: "Pusat", primaryCyan, textSecondary)
                        AcsDetailRowModal("SSID WiFi", device.ssid ?: "Unknown", textMain, textSecondary)
                        AcsDetailRowModal("Password WiFi", device.wifiPassword ?: "-", warningYellow, textSecondary)
                        AcsDetailRowModal("Jumlah Perangkat Terhubung", "${device.connectedUsers ?: 0} Perangkat", primaryCyan, textSecondary)
                        
                        val rxVal = device.rxPower ?: "-"
                        val rxBgColor = getRxPowerColor(rxVal)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Redaman (RX Power)", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (rxVal != "-") {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = rxBgColor
                                ) {
                                    Text(
                                        text = "$rxVal dBm",
                                        color = Color.White, // WHITE TEXT
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text("-", color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Tindakan & Remote Kontrol Perangkat:", color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Action Grid Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AcsModalActionButton(
                                text = "Set SSID",
                                icon = Icons.Default.Wifi,
                                bgColor = warningYellow,
                                modifier = Modifier.weight(1f),
                                onClick = onSetSsid
                            )
                            AcsModalActionButton(
                                text = "Set Password",
                                icon = Icons.Default.Key,
                                bgColor = primaryCyan,
                                modifier = Modifier.weight(1f),
                                onClick = onSetPassword
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AcsModalActionButton(
                                text = "Restart (Reboot)",
                                icon = Icons.Default.Refresh,
                                bgColor = errorRed,
                                modifier = Modifier.weight(1f),
                                onClick = onRestart
                            )
                            AcsModalActionButton(
                                text = "Summon (Refresh)",
                                icon = Icons.Default.Router,
                                bgColor = successGreen,
                                modifier = Modifier.weight(1f),
                                onClick = onSummon
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tutup Detail", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AcsModalActionButton(
    text: String,
    icon: ImageVector,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = Color.Black),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun AcsDetailRowModal(label: String, value: String, valueColor: Color, labelColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
