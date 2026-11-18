package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.OpenableColumns
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.data.OdcItem
import com.example.ui.data.OdpItem
import com.example.ui.data.RasioItem
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.KmzLayerDto
import com.example.ui.data.remote.MapCoordinatesRequest
import com.example.ui.data.remote.SaveKmzLayerRequest
import com.example.ui.data.remote.UpdateKmzLayerRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MapEntityType(val code: String, val label: String) {
    ODC("ODC", "ODC"),
    ODP("ODP", "ODP"),
    RASIO("RASIO", "Rasio"),
    CUSTOMER("CUSTOMER", "Pelanggan")
}

data class MapPointItem(
    val id: String,
    val type: MapEntityType,
    val name: String,
    val area: String,
    val subtitle: String,
    val statusText: String,
    val isAlertStatus: Boolean,
    val lat: Double?,
    val lng: Double?,
    val parentType: MapEntityType? = null,
    val parentId: String? = null,
    val liveState: String = "",
    val liveIp: String = "",
    val liveUptime: String = "",
    val rxPower: String = ""
) {
    val hasCoordinates: Boolean get() = lat != null && lng != null
}

private val LAT_LNG_TEXT_REGEX = Regex("""(-?\d{1,2}\.\d{3,})\s*[,;\s]\s*(-?\d{1,3}\.\d{3,})""")
private val GMAPS_AT_REGEX = Regex("""@(-?\d{1,2}\.\d{3,}),(-?\d{1,3}\.\d{3,})""")
private val GMAPS_Q_REGEX = Regex("""[?&](?:q|ll|destination)=(-?\d{1,2}\.\d{3,}),(-?\d{1,3}\.\d{3,})""")

private fun parseCoordPair(rawLat: String?, rawLng: String?, fallbackText: String? = null): Pair<Double, Double>? {
    val lat = rawLat?.trim()?.replace(',', '.')?.toDoubleOrNull()
    val lng = rawLng?.trim()?.replace(',', '.')?.toDoubleOrNull()
    if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0 && !(lat == 0.0 && lng == 0.0)) {
        return lat to lng
    }
    val text = fallbackText?.trim().orEmpty()
    if (text.isBlank()) return null
    val match = GMAPS_AT_REGEX.find(text) ?: GMAPS_Q_REGEX.find(text) ?: LAT_LNG_TEXT_REGEX.find(text)
    if (match != null) {
        val pLat = match.groupValues[1].toDoubleOrNull()
        val pLng = match.groupValues[2].toDoubleOrNull()
        if (pLat != null && pLng != null && pLat in -90.0..90.0 && pLng in -180.0..180.0 && !(pLat == 0.0 && pLng == 0.0)) {
            return pLat to pLng
        }
    }
    return null
}

private fun formatCoord(lat: Double?, lng: Double?): String {
    return if (lat != null && lng != null) {
        String.format(java.util.Locale.US, "%.6f, %.6f", lat, lng)
    } else {
        "Belum ada koordinat"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgMain = if (isDarkTheme) Color(0xFF0A0A0A) else Color(0xFFF1F5F9)
    val headerBg = if (isDarkTheme) Color(0xFF1F0216) else Color(0xFFE2E8F0)
    val cardBg = if (isDarkTheme) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkTheme) Color(0xFF2B2B3C) else Color(0xFFCBD5E1)
    val textMain = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFFAAAAAA) else Color(0xFF475569)
    val neonCyan = if (isDarkTheme) Color(0xFF00FFFF) else Color(0xFF0265DC)

    val odcColor = Color(0xFFA855F7)
    val odpColor = Color(0xFF3B82F6)
    val rasioColor = Color(0xFFF59E0B)
    val custOkColor = Color(0xFF10B981)
    val custOfflineColor = Color(0xFFF97316)
    val custAlertColor = Color(0xFFEF4444)

    var isLoading by remember { mutableStateOf(true) }
    var areaList by remember { mutableStateOf<List<Area>>(emptyList()) }
    var odcList by remember { mutableStateOf<List<OdcItem>>(emptyList()) }
    var odpList by remember { mutableStateOf<List<OdpItem>>(emptyList()) }
    var rasioList by remember { mutableStateOf<List<RasioItem>>(emptyList()) }
    var customerList by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var pppoeLiveMap by remember { mutableStateOf<Map<String, PPPoESecret>>(emptyMap()) }
    var acsLiveMap by remember { mutableStateOf<Map<String, AcsDevice>>(emptyMap()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isAutoLive by remember { mutableStateOf(true) }
    var isLiveSyncing by remember { mutableStateOf(false) }
    var liveCountdownSec by remember { mutableIntStateOf(15) }
    var lastLiveSyncText by remember { mutableStateOf("--:--:--") }
    var liveStatusFilter by remember { mutableStateOf("ALL") } // ALL, ONLINE, OFFLINE, ISOLIR

    var activeLayer by remember { mutableStateOf("ALL") } // ALL, CUSTOMER, ODP, ODC, RASIO
    var selectedArea by remember { mutableStateOf("Semua Area") }
    var areaDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showLines by remember { mutableStateOf(true) }
    var googleMapType by remember { mutableStateOf("y") } // y=Hybrid, s=Satellite, m=Roadmap, p=Terrain
    var activeViewTab by remember { mutableIntStateOf(0) } // 0 = Peta, 1 = Daftar Titik
    var listPinFilter by remember { mutableStateOf("ALL") } // ALL, PINNED, UNPINNED

    var kmzLayers by remember { mutableStateOf<List<KmzLayerDto>>(emptyList()) }
    var showKmzManagerDialog by remember { mutableStateOf(false) }
    var isImportingKmz by remember { mutableStateOf(false) }
    var focusKmzIdPending by remember { mutableStateOf<String?>(null) }
    var selectedKmzFeature by remember { mutableStateOf<KmzFeatureClickInfo?>(null) }

    var selectedPoint by remember { mutableStateOf<MapPointItem?>(null) }
    var editingPoint by remember { mutableStateOf<MapPointItem?>(null) }
    var pickFromMapTarget by remember { mutableStateOf<MapPointItem?>(null) }
    var coordInput by remember { mutableStateOf("") }
    var isSavingCoord by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var mapReady by remember { mutableStateOf(false) }

    fun hasAnyLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    var hasLocationPermission by remember { mutableStateOf(hasAnyLocationPermission()) }
    var userLocation by remember { mutableStateOf<Triple<Double, Double, Float>?>(null) }
    var centerMyLocationNonce by remember { mutableIntStateOf(0) }

    fun readBestLastKnownLocation() {
        if (!hasAnyLocationPermission()) return
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        try {
            val candidates = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            ).mapNotNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
            val best = candidates.maxWithOrNull(
                compareBy<Location> { it.time }.thenBy { -it.accuracy }
            )
            if (best != null && !(best.latitude == 0.0 && best.longitude == 0.0)) {
                userLocation = Triple(best.latitude, best.longitude, best.accuracy.coerceAtLeast(10f))
            }
        } catch (_: SecurityException) {
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            readBestLastKnownLocation()
            centerMyLocationNonce += 1
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            readBestLastKnownLocation()
            centerMyLocationNonce += 1
        }
    }

    DisposableEffect(hasLocationPermission, lifecycleOwner) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val listener = LocationListener { loc ->
                if (!(loc.latitude == 0.0 && loc.longitude == 0.0)) {
                    userLocation = Triple(loc.latitude, loc.longitude, loc.accuracy.coerceAtLeast(10f))
                }
            }
            readBestLastKnownLocation()
            try {
                lm?.allProviders?.forEach { provider ->
                    if (provider == LocationManager.GPS_PROVIDER || provider == LocationManager.NETWORK_PROVIDER) {
                        runCatching {
                            lm.requestLocationUpdates(provider, 4000L, 3f, listener, Looper.getMainLooper())
                        }
                    }
                }
            } catch (_: SecurityException) {
            }
            onDispose {
                runCatching { lm?.removeUpdates(listener) }
            }
        }
    }

    suspend fun refreshRealtimeTelemetry(areas: List<Area>) {
        if (isLiveSyncing) return
        isLiveSyncing = true
        try {
            coroutineScope {
                val secretJobs = areas.take(8).map { area ->
                    async { runCatching { ApiClient.apiService.getMikrotikSecrets(area.id) }.getOrNull().orEmpty() }
                }
                val acsJob = async { runCatching { ApiClient.apiService.getAcsDevices() }.getOrNull().orEmpty() }
                val custJob = async { runCatching { ApiClient.apiService.getCustomers() }.getOrNull() }

                val secretLists = secretJobs.awaitAll()
                val secretMap = mutableMapOf<String, PPPoESecret>()
                secretLists.forEach { list ->
                    list.forEach { sec ->
                        val k = sec.name.trim().lowercase()
                        if (k.isNotBlank()) secretMap[k] = sec
                    }
                }
                if (secretMap.isNotEmpty()) {
                    pppoeLiveMap = secretMap
                }

                val acsList = acsJob.await()
                if (acsList.isNotEmpty()) {
                    val acsMap = mutableMapOf<String, AcsDevice>()
                    acsList.forEach { dev ->
                        dev.username?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { acsMap[it] = dev }
                        dev.serialNumber?.trim()?.lowercase()?.takeIf { it.isNotBlank() }?.let { acsMap[it] = dev }
                    }
                    acsLiveMap = acsMap
                }

                custJob.await()?.let { rawCustomers ->
                    customerList = rawCustomers.filter { it.status != "TERHAPUS" && UserSession.isAreaNameAllowed(it.area) }
                }

                lastLiveSyncText = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            }
        } finally {
            isLiveSyncing = false
        }
    }

    fun loadAllMapData(silent: Boolean = false) {
        coroutineScope.launch {
            if (!silent) isLoading = true
            try {
                UserSession.getOrFetchAreas()
                val rawAreas = runCatching { ApiClient.apiService.getAreas() }.getOrDefault(emptyList())
                val rawOdc = runCatching { ApiClient.apiService.getOdcList() }.getOrDefault(emptyList())
                val rawOdp = runCatching { ApiClient.apiService.getOdpList() }.getOrDefault(emptyList())
                val rawRasio = runCatching { ApiClient.apiService.getRasioList() }.getOrDefault(emptyList())
                val rawCustomers = runCatching { ApiClient.apiService.getCustomers() }.getOrDefault(emptyList())
                val rawKmz = runCatching { ApiClient.apiService.getKmzLayers() }.getOrNull()

                val allowedAreas = rawAreas.filter { UserSession.isAreaIdAllowed(it.id) }
                areaList = allowedAreas
                odcList = rawOdc.filter { UserSession.isAreaNameAllowed(it.area) }
                rasioList = rawRasio.filter { UserSession.isAreaNameAllowed(it.area) }
                odpList = rawOdp.filter { odp ->
                    val parentArea = odcList.find { it.id == odp.odcId }?.area
                    UserSession.isAreaNameAllowed(odp.area.ifBlank { parentArea.orEmpty() })
                }
                customerList = rawCustomers.filter { it.status != "TERHAPUS" && UserSession.isAreaNameAllowed(it.area) }
                if (rawKmz != null) {
                    kmzLayers = rawKmz
                }
                refreshRealtimeTelemetry(allowedAreas)
                liveCountdownSec = 15
            } catch (e: Exception) {
                if (!silent) {
                    Toast.makeText(context, "Gagal memuat data peta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (!silent) isLoading = false
            }
        }
    }

    LaunchedEffect(isAutoLive, lifecycleOwner) {
        if (!isAutoLive) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            liveCountdownSec = 15
            while (true) {
                delay(1_000L)
                if (liveCountdownSec > 1) {
                    liveCountdownSec -= 1
                } else {
                    val areasToSync = areaList
                    if (areasToSync.isNotEmpty()) {
                        refreshRealtimeTelemetry(areasToSync)
                    } else {
                        loadAllMapData(silent = true)
                    }
                    liveCountdownSec = 15
                }
            }
        }
    }

    val kmzFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            isImportingKmz = true
            try {
                var displayName = "layer.kmz"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) {
                        displayName = cursor.getString(nameIdx) ?: displayName
                    }
                }
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw IllegalStateException("File tidak dapat dibaca")

                val parsed = withContext(Dispatchers.Default) {
                    KmzParser.parseBytes(bytes, displayName)
                }
                if (parsed.featureCount == 0) {
                    Toast.makeText(context, "Tidak ada titik/jalur ditemukan di $displayName", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val savedLayer = runCatching {
                    ApiClient.apiService.saveKmzLayer(
                        SaveKmzLayerRequest(
                            name = parsed.documentName,
                            fileName = displayName,
                            featuresJson = parsed.featuresJson,
                            featureCount = parsed.featureCount,
                            isVisible = true
                        )
                    )
                }.getOrElse {
                    KmzLayerDto(
                        id = "local_${System.currentTimeMillis()}",
                        name = parsed.documentName,
                        fileName = displayName,
                        featuresJson = parsed.featuresJson,
                        featureCount = parsed.featureCount,
                        isVisible = true
                    )
                }

                val hydratedLayer = if (savedLayer.featuresJson == "[]" || savedLayer.featuresJson.isBlank()) {
                    savedLayer.copy(featuresJson = parsed.featuresJson, featureCount = parsed.featureCount)
                } else {
                    savedLayer
                }

                kmzLayers = listOf(hydratedLayer) + kmzLayers.filterNot { it.id == hydratedLayer.id }
                activeViewTab = 0
                focusKmzIdPending = hydratedLayer.id
                Toast.makeText(
                    context,
                    "Berhasil import ${parsed.documentName} (${parsed.featureCount} objek)",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses file KMZ/KML: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isImportingKmz = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllMapData()
    }

    BackHandler(enabled = selectedPoint != null || selectedKmzFeature != null || editingPoint != null || pickFromMapTarget != null || showKmzManagerDialog) {
        when {
            showKmzManagerDialog -> showKmzManagerDialog = false
            editingPoint != null -> editingPoint = null
            pickFromMapTarget != null -> pickFromMapTarget = null
            selectedKmzFeature != null -> selectedKmzFeature = null
            selectedPoint != null -> selectedPoint = null
        }
    }

    val allPoints = remember(odcList, odpList, rasioList, customerList, pppoeLiveMap, acsLiveMap) {
        val result = mutableListOf<MapPointItem>()

        odcList.forEach { odc ->
            val coords = parseCoordPair(odc.latitude, odc.longitude, odc.location)
            result.add(
                MapPointItem(
                    id = odc.id,
                    type = MapEntityType.ODC,
                    name = odc.name.ifBlank { "ODC #${odc.id}" },
                    area = odc.area.ifBlank { "-" },
                    subtitle = "${odc.portCount} Port • In: ${odc.redamanIn.ifBlank { "-" }} / Out: ${odc.redamanOut.ifBlank { "-" }}",
                    statusText = odc.location.ifBlank { "ODC" },
                    isAlertStatus = false,
                    lat = coords?.first,
                    lng = coords?.second
                )
            )
        }

        rasioList.forEach { rasio ->
            val coords = parseCoordPair(rasio.latitude, rasio.longitude, rasio.location)
            result.add(
                MapPointItem(
                    id = rasio.id,
                    type = MapEntityType.RASIO,
                    name = rasio.name.ifBlank { "Rasio #${rasio.id}" },
                    area = rasio.area.ifBlank { "-" },
                    subtitle = "Size ${rasio.size.ifBlank { "-" }} • In ${rasio.redamanIn.ifBlank { "-" }} • Out ${rasio.redamanOutA}/${rasio.redamanOutB}",
                    statusText = "RASIO ${rasio.size}".trim(),
                    isAlertStatus = false,
                    lat = coords?.first,
                    lng = coords?.second
                )
            )
        }

        odpList.forEach { odp ->
            val coords = parseCoordPair(odp.latitude, odp.longitude, null)
            val resolvedArea = odp.area.ifBlank {
                odcList.find { it.id == odp.odcId }?.area.orEmpty()
            }.ifBlank { "-" }
            var parentType: MapEntityType? = null
            var parentId: String? = null
            if (odp.odcId.startsWith("-")) {
                val absId = odp.odcId.removePrefix("-")
                if (absId.startsWith("100000")) {
                    parentType = MapEntityType.ODP
                    parentId = absId.removePrefix("100000")
                } else {
                    parentType = MapEntityType.RASIO
                    parentId = absId
                }
            } else if (odp.odcId.isNotBlank() && odp.odcId != "0") {
                parentType = MapEntityType.ODC
                parentId = odp.odcId
            }

            result.add(
                MapPointItem(
                    id = odp.id,
                    type = MapEntityType.ODP,
                    name = odp.name.ifBlank { "ODP #${odp.id}" },
                    area = resolvedArea,
                    subtitle = "${odp.portCount} Port • In: ${odp.redamanIn.ifBlank { "-" }} / Out: ${odp.redamanOut.ifBlank { "-" }}",
                    statusText = "ODP (${odp.portCount} Port)",
                    isAlertStatus = false,
                    lat = coords?.first,
                    lng = coords?.second,
                    parentType = parentType,
                    parentId = parentId
                )
            )
        }

        customerList.forEach { cust ->
            val coords = parseCoordPair(cust.latitude, cust.longitude, cust.getEffectiveAddress())
            val isIsolir = cust.status.contains("ISOLIR", ignoreCase = true)
            val isPaid = cust.status.contains("LUNAS", ignoreCase = true)
            val odpName = odpList.find { it.id == cust.odpId }?.name
            val odpInfo = if (!odpName.isNullOrBlank()) {
                "ODP: $odpName${if (!cust.odpPort.isNullOrBlank()) " (Port ${cust.odpPort})" else ""}"
            } else {
                cust.getEffectiveAddress().ifBlank { "Belum ada ODP" }
            }

            val secretKey = (cust.pppoeSecret ?: cust.username).trim().lowercase()
            val liveSec = pppoeLiveMap[secretKey] ?: pppoeLiveMap[cust.username.trim().lowercase()]
            val liveAcs = acsLiveMap[secretKey]
                ?: acsLiveMap[cust.getEffectiveSnModem()?.trim()?.lowercase().orEmpty()]
            val resolvedLiveState = when {
                isIsolir || liveSec?.status.equals("Disabled", ignoreCase = true) -> "ISOLIR"
                liveSec?.status.equals("Online", ignoreCase = true) || liveAcs?.isOnline == true -> "ONLINE"
                liveSec?.status.equals("Offline", ignoreCase = true) || liveAcs?.isOnline == false -> "OFFLINE"
                else -> ""
            }

            result.add(
                MapPointItem(
                    id = cust.id,
                    type = MapEntityType.CUSTOMER,
                    name = cust.name.ifBlank { cust.username },
                    area = cust.area.ifBlank { "-" },
                    subtitle = "${cust.packageName?.takeIf { it.isNotBlank() } ?: "-"} • $odpInfo",
                    statusText = when {
                        isIsolir -> "ISOLIR"
                        isPaid -> "LUNAS"
                        else -> "BELUM BAYAR"
                    },
                    isAlertStatus = isIsolir,
                    lat = coords?.first,
                    lng = coords?.second,
                    parentType = if (!cust.odpId.isNullOrBlank()) MapEntityType.ODP else null,
                    parentId = cust.odpId,
                    liveState = resolvedLiveState,
                    liveIp = liveSec?.ipAddress.orEmpty().ifBlank { liveAcs?.ipAddress.orEmpty() },
                    liveUptime = liveSec?.uptime.orEmpty().ifBlank { liveAcs?.uptime.orEmpty() },
                    rxPower = liveAcs?.rxPower.orEmpty()
                )
            )
        }

        result
    }

    val latestAllPoints by rememberUpdatedState(allPoints)
    LaunchedEffect(allPoints) {
        selectedPoint?.let { currentSel ->
            allPoints.find { it.type == currentSel.type && it.id == currentSel.id }?.let { refreshed ->
                selectedPoint = refreshed
            }
        }
    }

    val filteredPoints = remember(allPoints, activeLayer, selectedArea, searchQuery, liveStatusFilter) {
        allPoints.filter { item ->
            val matchLayer = activeLayer == "ALL" || item.type.code == activeLayer
            val matchArea = selectedArea == "Semua Area" || item.area.equals(selectedArea, ignoreCase = true)
            val matchLive = when (liveStatusFilter) {
                "ONLINE" -> item.type != MapEntityType.CUSTOMER || item.liveState == "ONLINE"
                "OFFLINE" -> item.type == MapEntityType.CUSTOMER && item.liveState == "OFFLINE"
                "ISOLIR" -> item.type == MapEntityType.CUSTOMER && (item.isAlertStatus || item.liveState == "ISOLIR")
                else -> true
            }
            val q = searchQuery.trim()
            val matchSearch = q.isBlank() ||
                item.name.contains(q, ignoreCase = true) ||
                item.subtitle.contains(q, ignoreCase = true) ||
                item.area.contains(q, ignoreCase = true)
            matchLayer && matchArea && matchLive && matchSearch
        }
    }

    val onlineCustCount = remember(allPoints) { allPoints.count { it.type == MapEntityType.CUSTOMER && it.liveState == "ONLINE" } }
    val offlineCustCount = remember(allPoints) { allPoints.count { it.type == MapEntityType.CUSTOMER && it.liveState == "OFFLINE" } }
    val isolirCustCount = remember(allPoints) { allPoints.count { it.type == MapEntityType.CUSTOMER && (it.isAlertStatus || it.liveState == "ISOLIR") } }

    val pinnedPoints = remember(filteredPoints) { filteredPoints.filter { it.hasCoordinates } }
    val unpinnedCount = remember(filteredPoints) { filteredPoints.count { !it.hasCoordinates } }

    val listDisplayPoints = remember(filteredPoints, listPinFilter) {
        when (listPinFilter) {
            "PINNED" -> filteredPoints.filter { it.hasCoordinates }
            "UNPINNED" -> filteredPoints.filter { !it.hasCoordinates }
            else -> filteredPoints
        }
    }

    fun buildPayloadJson(): String {
        val nodesArray = JSONArray()
        pinnedPoints.forEach { p ->
            val obj = JSONObject().apply {
                put("key", "${p.type.code}_${p.id}")
                put("id", p.id)
                put("type", p.type.code)
                put("name", p.name)
                put("area", p.area)
                put("subtitle", p.subtitle)
                put("status", p.statusText)
                put("liveState", p.liveState)
                put("alert", p.isAlertStatus)
                put("lat", p.lat)
                put("lng", p.lng)
                if (p.parentType != null && !p.parentId.isNullOrBlank()) {
                    put("parentKey", "${p.parentType.code}_${p.parentId}")
                }
            }
            nodesArray.put(obj)
        }

        val kmzArray = JSONArray()
        kmzLayers.filter { it.isVisible }.forEach { layer ->
            val parsedFeatures = runCatching { JSONArray(layer.featuresJson) }.getOrNull() ?: JSONArray()
            kmzArray.put(
                JSONObject().apply {
                    put("id", layer.id)
                    put("name", layer.name)
                    put("features", parsedFeatures)
                }
            )
        }

        return JSONObject().apply {
            put("nodes", nodesArray)
            put("kmzLayers", kmzArray)
            put("focusKmzId", focusKmzIdPending ?: "")
            put("showLines", showLines)
            put("mapType", googleMapType)
            put("pickMode", pickFromMapTarget != null)
            put("dark", isDarkTheme)
            put("centerMyLocationNonce", centerMyLocationNonce)
            userLocation?.let { (uLat, uLng, uAcc) ->
                put(
                    "myLocation",
                    JSONObject().apply {
                        put("lat", uLat)
                        put("lng", uLng)
                        put("accuracy", uAcc.toDouble())
                    }
                )
            }
        }.toString()
    }

    LaunchedEffect(
        mapReady,
        pinnedPoints,
        kmzLayers,
        showLines,
        googleMapType,
        pickFromMapTarget,
        isDarkTheme,
        activeViewTab,
        focusKmzIdPending,
        userLocation,
        centerMyLocationNonce
    ) {
        if (mapReady && activeViewTab == 0) {
            val payload = buildPayloadJson()
            webViewRef?.evaluateJavascript("window.renderNetworkMap && window.renderNetworkMap($payload);", null)
            if (focusKmzIdPending != null) {
                focusKmzIdPending = null
            }
        }
    }

    fun saveCoordinates(point: MapPointItem, latStr: String, lngStr: String) {
        coroutineScope.launch {
            isSavingCoord = true
            try {
                ApiClient.apiService.updateMapCoordinates(
                    MapCoordinatesRequest(
                        entityType = point.type.code,
                        id = point.id,
                        latitude = latStr,
                        longitude = lngStr
                    )
                )
                Toast.makeText(context, "Koordinat ${point.name} tersimpan!", Toast.LENGTH_SHORT).show()
                editingPoint = null
                pickFromMapTarget = null
                selectedPoint = point.copy(lat = latStr.toDoubleOrNull(), lng = lngStr.toDoubleOrNull())
                loadAllMapData()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyimpan koordinat: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isSavingCoord = false
            }
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Google Maps & KMZ", color = textMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Jaringan ODC, ODP, Pelanggan & Overlay KMZ", color = textSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            kmzFilePicker.launch(
                                arrayOf(
                                    "application/vnd.google-earth.kmz",
                                    "application/vnd.google-earth.kml+xml",
                                    "application/zip",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        modifier = Modifier.testTag("btn_import_kmz")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Import KMZ", tint = neonCyan)
                    }
                    IconButton(
                        onClick = { showKmzManagerDialog = true },
                        modifier = Modifier.testTag("btn_manage_kmz")
                    ) {
                        BadgedBox(
                            badge = {
                                if (kmzLayers.isNotEmpty()) {
                                    Badge(containerColor = neonCyan, contentColor = Color.Black) {
                                        Text("${kmzLayers.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Kelola Layer KMZ", tint = textMain)
                        }
                    }
                    IconButton(onClick = { loadAllMapData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Segarkan Peta", tint = textMain)
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
        ) {
            if (isLoading || isImportingKmz) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonCyan,
                    trackColor = cardBorder
                )
            }

            // Pick-on-map helper banner
            if (pickFromMapTarget != null) {
                Surface(
                    color = neonCyan.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ketuk peta untuk pin: ${pickFromMapTarget?.name}",
                                color = textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Sentuh lokasi yang tepat pada peta",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        TextButton(onClick = { pickFromMapTarget = null }) {
                            Text("Batal", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Mode switcher: Peta vs Daftar Titik
            TabRow(
                selectedTabIndex = activeViewTab,
                containerColor = cardBg,
                contentColor = neonCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeViewTab == 0,
                    onClick = { activeViewTab = 0 },
                    modifier = Modifier.testTag("tab_map_view"),
                    text = {
                        Text(
                            "Peta (${pinnedPoints.size})",
                            fontSize = 13.sp,
                            fontWeight = if (activeViewTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeViewTab == 0) neonCyan else textSecondary
                        )
                    }
                )
                Tab(
                    selected = activeViewTab == 1,
                    onClick = { activeViewTab = 1 },
                    modifier = Modifier.testTag("tab_map_list"),
                    text = {
                        Text(
                            "Daftar Titik (${filteredPoints.size})",
                            fontSize = 13.sp,
                            fontWeight = if (activeViewTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeViewTab == 1) neonCyan else textSecondary
                        )
                    }
                )
            }

            // Filter Area & Search row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari ODC, ODP, atau Pelanggan...", fontSize = 12.sp, color = textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = textMain),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = areaDropdownExpanded,
                        onExpandedChange = { areaDropdownExpanded = !areaDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedArea,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaDropdownExpanded) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = textMain),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = cardBg,
                                unfocusedContainerColor = cardBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = areaDropdownExpanded,
                            onDismissRequest = { areaDropdownExpanded = false },
                            containerColor = headerBg
                        ) {
                            val areaOpts = listOf("Semua Area") + areaList.map { it.name }.distinct().sorted()
                            areaOpts.forEach { areaOpt ->
                                DropdownMenuItem(
                                    text = { Text(areaOpt, color = if (selectedArea == areaOpt) neonCyan else textMain, fontSize = 12.sp) },
                                    onClick = {
                                        selectedArea = areaOpt
                                        areaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Layer Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val layers = listOf(
                    "ALL" to "Semua",
                    "CUSTOMER" to "Pelanggan",
                    "ODP" to "ODP",
                    "ODC" to "ODC",
                    "RASIO" to "Rasio"
                )
                layers.forEach { (code, label) ->
                    val isSelected = activeLayer == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeLayer = code },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = neonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = neonCyan,
                            labelColor = textSecondary
                        )
                    )
                }
                if (activeViewTab == 0) {
                    FilterChip(
                        selected = showLines,
                        onClick = { showLines = !showLines },
                        leadingIcon = { Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(15.dp)) },
                        label = { Text("Jalur FO", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF10B981),
                            labelColor = textSecondary
                        )
                    )
                    FilterChip(
                        selected = kmzLayers.any { it.isVisible },
                        onClick = { showKmzManagerDialog = true },
                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(15.dp)) },
                        label = { Text("KMZ (${kmzLayers.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFFF59E0B),
                            labelColor = textSecondary
                        )
                    )
                }
            }

            // Realtime Live Bar + Google Maps Mode Selector
            if (activeViewTab == 0) {
                Surface(
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isLiveSyncing -> neonCyan
                                            isAutoLive -> custOkColor
                                            else -> textSecondary
                                        }
                                    )
                            )
                            Text(
                                text = if (isLiveSyncing) {
                                    "LIVE • Menyinkronkan status PPPoE..."
                                } else {
                                    "LIVE • On:$onlineCustCount Off:$offlineCustCount Iso:$isolirCustCount • $lastLiveSyncText"
                                },
                                color = textMain,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            color = if (isAutoLive) custOkColor.copy(alpha = 0.16f) else bgMain,
                            border = BorderStroke(1.dp, if (isAutoLive) custOkColor else cardBorder),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    isAutoLive = !isAutoLive
                                    if (isAutoLive) {
                                        liveCountdownSec = 15
                                        coroutineScope.launch {
                                            if (areaList.isNotEmpty()) refreshRealtimeTelemetry(areaList)
                                            else loadAllMapData(silent = true)
                                        }
                                    }
                                }
                        ) {
                            Text(
                                text = if (isAutoLive) "Auto ${liveCountdownSec}d" else "Manual",
                                color = if (isAutoLive) custOkColor else textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Google Maps:", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    val gmapModes = listOf(
                        "y" to "Hybrid",
                        "s" to "Satelit",
                        "m" to "Jalan",
                        "p" to "Medan"
                    )
                    gmapModes.forEach { (code, label) ->
                        val active = googleMapType == code
                        Surface(
                            color = if (active) neonCyan.copy(alpha = 0.18f) else cardBg,
                            border = BorderStroke(1.dp, if (active) neonCyan else cardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { googleMapType = code }
                        ) {
                            Text(
                                text = label,
                                color = if (active) neonCyan else textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    listOf("ALL" to "Semua", "ONLINE" to "Online", "OFFLINE" to "Offline", "ISOLIR" to "Isolir").forEach { (stCode, stLabel) ->
                        val stActive = liveStatusFilter == stCode
                        Surface(
                            color = if (stActive) Color(0xFF10B981).copy(alpha = 0.18f) else cardBg,
                            border = BorderStroke(1.dp, if (stActive) Color(0xFF10B981) else cardBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { liveStatusFilter = stCode }
                        ) {
                            Text(
                                text = stLabel,
                                color = if (stActive) Color(0xFF10B981) else textSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (stActive) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (activeViewTab == 0) {
                // Quick unpinned alert strip so user knows if items need coordinates
                if (unpinnedCount > 0) {
                    Surface(
                        color = cardBg,
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable {
                                listPinFilter = "UNPINNED"
                                activeViewTab = 1
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$unpinnedCount data belum memiliki koordinat GPS",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Atur Lokasi →",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                ) {
                    val mapHtml = remember {
                        runCatching {
                            context.assets.open("network_map.html").bufferedReader().use { it.readText() }
                        }.getOrElse {
                            "<html><body style='background:#0f172a;color:#fff;padding:16px'>Map asset tidak ditemukan</body></html>"
                        }
                    }
                    DisposableEffect(activeViewTab) {
                        onDispose {
                            mapReady = false
                            webViewRef?.destroy()
                            webViewRef = null
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            @SuppressLint("SetJavaScriptEnabled")
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                webChromeClient = WebChromeClient()
                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun onMarkerClick(type: String, id: String) {
                                            coroutineScope.launch {
                                                selectedKmzFeature = null
                                                selectedPoint = latestAllPoints.find { it.type.code == type && it.id == id }
                                            }
                                        }

                                        @JavascriptInterface
                                        fun onKmzFeatureClick(
                                            layerName: String,
                                            featureName: String,
                                            folderName: String,
                                            description: String,
                                            lat: Double,
                                            lng: Double
                                        ) {
                                            coroutineScope.launch {
                                                selectedPoint = null
                                                selectedKmzFeature = KmzFeatureClickInfo(
                                                    layerName = layerName,
                                                    featureName = featureName,
                                                    folderName = folderName,
                                                    description = description,
                                                    lat = if (lat.isNaN() || lat == 0.0 && lng == 0.0) null else lat,
                                                    lng = if (lng.isNaN() || lat == 0.0 && lng == 0.0) null else lng
                                                )
                                            }
                                        }

                                        @JavascriptInterface
                                        fun onMapPicked(lat: Double, lng: Double) {
                                            coroutineScope.launch {
                                                val target = pickFromMapTarget
                                                if (target != null) {
                                                    coordInput = String.format(java.util.Locale.US, "%.6f, %.6f", lat, lng)
                                                    editingPoint = target
                                                    pickFromMapTarget = null
                                                }
                                            }
                                        }
                                    },
                                    "AndroidMapBridge"
                                )
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        mapReady = true
                                        val payload = buildPayloadJson()
                                        view?.evaluateJavascript("window.renderNetworkMap && window.renderNetworkMap($payload);", null)
                                    }
                                }
                                loadDataWithBaseURL("https://maps.google.com", mapHtml, "text/html", "UTF-8", null)
                                webViewRef = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Legend overlay bottom-start
                    Surface(
                        color = cardBg.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 10.dp, end = 68.dp, bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MapLegendDot(Color(0xFF2563EB), "Saya", textMain)
                            MapLegendDot(odcColor, "ODC", textMain)
                            MapLegendDot(odpColor, "ODP", textMain)
                            MapLegendDot(rasioColor, "Rasio", textMain)
                            MapLegendDot(custOkColor, "Online", textMain)
                            MapLegendDot(custOfflineColor, "Off", textMain)
                            MapLegendDot(custAlertColor, "Isolir", textMain)
                        }
                    }

                    // Floating My Location button bottom-end
                    SmallFloatingActionButton(
                        onClick = {
                            if (!hasLocationPermission) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                readBestLastKnownLocation()
                                if (userLocation != null) {
                                    centerMyLocationNonce += 1
                                } else {
                                    Toast.makeText(context, "Mencari sinyal GPS lokasi Anda...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        containerColor = cardBg.copy(alpha = 0.95f),
                        contentColor = Color(0xFF3B82F6),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .size(48.dp)
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), CircleShape)
                            .testTag("btn_my_location")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Lokasi Saya"
                        )
                    }
                }
            } else {
                // Daftar Titik tab (Native Compose list for browsing and assigning coordinates)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val pinTabs = listOf(
                        "ALL" to "Semua (${filteredPoints.size})",
                        "PINNED" to "Terpetakan (${pinnedPoints.size})",
                        "UNPINNED" to "Belum Pin ($unpinnedCount)"
                    )
                    pinTabs.forEach { (code, label) ->
                        FilterChip(
                            selected = listPinFilter == code,
                            onClick = { listPinFilter = code },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = neonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = neonCyan,
                                labelColor = textSecondary
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
                ) {
                    if (listDisplayPoints.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada data titik untuk filter ini.", color = textSecondary, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(listDisplayPoints, key = { "${it.type.code}_${it.id}" }) { point ->
                            val badgeColor = when (point.type) {
                                MapEntityType.ODC -> odcColor
                                MapEntityType.ODP -> odpColor
                                MapEntityType.RASIO -> rasioColor
                                MapEntityType.CUSTOMER -> when {
                                    point.liveState == "ISOLIR" || point.isAlertStatus -> custAlertColor
                                    point.liveState == "OFFLINE" -> custOfflineColor
                                    else -> custOkColor
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, cardBorder),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (point.hasCoordinates) {
                                            activeViewTab = 0
                                            selectedPoint = point
                                            webViewRef?.evaluateJavascript(
                                                "window.focusNetworkNode && window.focusNetworkNode('${point.type.code}_${point.id}');",
                                                null
                                            )
                                        } else {
                                            coordInput = ""
                                            editingPoint = point
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = point.type.label.uppercase(),
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = point.name,
                                            color = textMain,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${point.area} • ${point.subtitle}",
                                            color = textSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatCoord(point.lat, point.lng),
                                            color = if (point.hasCoordinates) neonCyan else Color(0xFFF59E0B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            coordInput = if (point.hasCoordinates) formatCoord(point.lat, point.lng) else ""
                                            editingPoint = point
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (point.hasCoordinates) Icons.Default.EditLocationAlt else Icons.Default.AddLocationAlt,
                                            contentDescription = "Atur Koordinat",
                                            tint = neonCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Point Detail Card (Bottom docked)
            selectedPoint?.let { point ->
                val accent = when (point.type) {
                    MapEntityType.ODC -> odcColor
                    MapEntityType.ODP -> odpColor
                    MapEntityType.RASIO -> rasioColor
                    MapEntityType.CUSTOMER -> if (point.isAlertStatus) custAlertColor else custOkColor
                }
                Surface(
                    color = cardBg,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = accent.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        point.type.label.uppercase(),
                                        color = accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    point.name,
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { selectedPoint = null },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = textSecondary)
                            }
                        }

                        Text("${point.area} • ${point.subtitle}", color = textSecondary, fontSize = 12.sp)
                        if (point.type == MapEntityType.CUSTOMER && (point.liveState.isNotBlank() || point.liveIp.isNotBlank() || point.rxPower.isNotBlank())) {
                            val liveColor = when (point.liveState) {
                                "ONLINE" -> custOkColor
                                "OFFLINE" -> custOfflineColor
                                "ISOLIR" -> custAlertColor
                                else -> textSecondary
                            }
                            val liveInfoParts = listOfNotNull(
                                point.liveState.takeIf { it.isNotBlank() }?.let { "PPPoE $it" },
                                point.liveIp.takeIf { it.isNotBlank() }?.let { "IP: $it" },
                                point.liveUptime.takeIf { it.isNotBlank() }?.let { "Up: $it" },
                                point.rxPower.takeIf { it.isNotBlank() && it != "-" }?.let { "Rx: $it" }
                            )
                            Text(
                                text = liveInfoParts.joinToString(" • "),
                                color = liveColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "Koordinat: ${formatCoord(point.lat, point.lng)}",
                            color = neonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (point.hasCoordinates) {
                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("geo:${point.lat},${point.lng}?q=${point.lat},${point.lng}(${Uri.encode(point.name)})")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${point.lat},${point.lng}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                                    border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.6f))
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    coordInput = if (point.hasCoordinates) formatCoord(point.lat, point.lng) else ""
                                    editingPoint = point
                                },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Atur Pin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (point.type == MapEntityType.CUSTOMER) {
                                Button(
                                    onClick = { onNavigateToCustomerDetail(point.id) },
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black)
                                ) {
                                    Text("Detail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            // Selected KMZ Feature Card (Bottom docked)
            selectedKmzFeature?.let { kmzInfo ->
                Surface(
                    color = cardBg,
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "KMZ",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    kmzInfo.featureName,
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { selectedKmzFeature = null },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = textSecondary)
                            }
                        }

                        Text(
                            text = if (kmzInfo.folderName.isNotBlank()) {
                                "${kmzInfo.layerName} • ${kmzInfo.folderName}"
                            } else {
                                kmzInfo.layerName
                            },
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                        if (kmzInfo.description.isNotBlank()) {
                            Text(
                                text = kmzInfo.description,
                                color = textMain,
                                fontSize = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (kmzInfo.lat != null && kmzInfo.lng != null) {
                            Text(
                                "Koordinat: ${formatCoord(kmzInfo.lat, kmzInfo.lng)}",
                                color = neonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("geo:${kmzInfo.lat},${kmzInfo.lng}?q=${kmzInfo.lat},${kmzInfo.lng}(${Uri.encode(kmzInfo.featureName)})")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (_: Exception) {
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${kmzInfo.lat},${kmzInfo.lng}")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                                border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Buka di Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Kelola Layer KMZ / KML
    if (showKmzManagerDialog) {
        AlertDialog(
            onDismissRequest = { showKmzManagerDialog = false },
            containerColor = cardBg,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Layer File .KMZ / .KML", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(
                        onClick = {
                            kmzFilePicker.launch(
                                arrayOf(
                                    "application/vnd.google-earth.kmz",
                                    "application/vnd.google-earth.kml+xml",
                                    "application/zip",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        }
                    ) {
                        Text("+ Import", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            text = {
                if (kmzLayers.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LayersClear, contentDescription = null, tint = textSecondary, modifier = Modifier.size(36.dp))
                        Text(
                            "Belum ada file .KMZ / .KML diunggah.\nImport file KMZ dari Google Earth untuk menampilkan jalur kabel FO & tiang di Google Maps.",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        kmzLayers.forEach { layer ->
                            Surface(
                                color = bgMain,
                                border = BorderStroke(1.dp, cardBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = layer.isVisible,
                                        onCheckedChange = { checked ->
                                            kmzLayers = kmzLayers.map {
                                                if (it.id == layer.id) it.copy(isVisible = checked) else it
                                            }
                                            if (!layer.id.startsWith("local_")) {
                                                coroutineScope.launch {
                                                    runCatching {
                                                        ApiClient.apiService.updateKmzLayer(
                                                            layer.id,
                                                            UpdateKmzLayerRequest(isVisible = checked)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = neonCyan)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                if (!layer.isVisible) {
                                                    kmzLayers = kmzLayers.map {
                                                        if (it.id == layer.id) it.copy(isVisible = true) else it
                                                    }
                                                }
                                                focusKmzIdPending = layer.id
                                                activeViewTab = 0
                                                showKmzManagerDialog = false
                                            }
                                    ) {
                                        Text(
                                            layer.name.ifBlank { layer.fileName },
                                            color = textMain,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${layer.featureCount} objek • ${layer.fileName}",
                                            color = textSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            kmzLayers = kmzLayers.filterNot { it.id == layer.id }
                                            if (!layer.id.startsWith("local_")) {
                                                coroutineScope.launch {
                                                    runCatching { ApiClient.apiService.deleteKmzLayer(layer.id) }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Layer KMZ", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKmzManagerDialog = false }) {
                    Text("Tutup", color = neonCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog Atur / Edit Koordinat GPS
    editingPoint?.let { point ->
        AlertDialog(
            onDismissRequest = { if (!isSavingCoord) editingPoint = null },
            containerColor = cardBg,
            title = {
                Text("Atur Koordinat • ${point.name}", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Masukkan koordinat (contoh: -7.562140, 110.824310) atau tempel tautan Google Maps.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = coordInput,
                        onValueChange = { coordInput = it },
                        label = { Text("Latitude, Longitude", fontSize = 12.sp) },
                        placeholder = { Text("-7.562140, 110.824310", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            pickFromMapTarget = point
                            editingPoint = null
                            activeViewTab = 0
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                        border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pilih Titik Langsung di Peta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSavingCoord,
                    onClick = {
                        val parsed = parseCoordPair(null, null, coordInput)
                        if (parsed == null) {
                            Toast.makeText(context, "Format koordinat tidak valid. Contoh: -7.56214, 110.82431", Toast.LENGTH_SHORT).show()
                        } else {
                            saveCoordinates(
                                point,
                                String.format(java.util.Locale.US, "%.6f", parsed.first),
                                String.format(java.util.Locale.US, "%.6f", parsed.second)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black)
                ) {
                    Text(if (isSavingCoord) "Menyimpan..." else "Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSavingCoord,
                    onClick = { editingPoint = null }
                ) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}

@Composable
private fun MapLegendDot(color: Color, label: String, textMain: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, color = textMain, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}


