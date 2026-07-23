package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.OdcItem
import com.example.ui.data.OdpItem
import com.example.ui.data.RasioItem
import com.example.ui.screens.Customer
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JaringanScreen(onBack: () -> Unit) {
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)
    
    val context = LocalContext.current
    var odcList by remember { mutableStateOf<List<OdcItem>>(emptyList()) }
    var odpList by remember { mutableStateOf<List<OdpItem>>(emptyList()) }
    var rasioList by remember { mutableStateOf<List<RasioItem>>(emptyList()) }
    var customerList by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var areaList by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    var filterArea by remember { mutableStateOf("All") }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    
    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("Daftar Jaringan") }
    
    LaunchedEffect(Unit) {
        try {
            UserSession.getOrFetchAreas()
            val rawAreas = ApiClient.apiService.getAreas()
            val rawOdc = ApiClient.apiService.getOdcList()
            val rawOdp = ApiClient.apiService.getOdpList()
            val rawCustomers = ApiClient.apiService.getCustomers()
            var rawRasio = emptyList<RasioItem>()
            try {
                rawRasio = ApiClient.apiService.getRasioList()
            } catch (e: Exception) {}

            areaList = rawAreas.filter { UserSession.isAreaIdAllowed(it.id) }
            odcList = rawOdc.filter { UserSession.isAreaNameAllowed(it.area) }
            customerList = rawCustomers.filter { UserSession.isAreaNameAllowed(it.area) }
            rasioList = rawRasio.filter { UserSession.isAreaNameAllowed(it.area) }

            odpList = rawOdp.filter { item ->
                val parentOdc = if (item.odcId.startsWith("-")) {
                    val absoluteId = item.odcId.removePrefix("-")
                    if (absoluteId.startsWith("100000")) {
                        val realOdpId = absoluteId.removePrefix("100000")
                        rawOdp.find { it.id == realOdpId }?.let { OdcItem(id = item.odcId, name = it.name, location = "", area = it.area) }
                    } else {
                        rawRasio.find { it.id == absoluteId }?.let { OdcItem(id = item.odcId, name = it.name, location = it.location, area = it.area) }
                    }
                } else {
                    rawOdc.find { it.id == item.odcId }
                } ?: rawOdc.find { it.name == item.portInput } ?: rawOdc.find { item.portInput.contains(it.name) }
                val displayArea = item.area.takeIf { it.isNotEmpty() } ?: parentOdc?.area.orEmpty()
                UserSession.isAreaNameAllowed(displayArea)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data jaringan", Toast.LENGTH_SHORT).show()
        }
        isBackgroundLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaringan", color = textMain, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgMain)
            )
        },
        containerColor = bgMain
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = primaryBg,
                    trackColor = cardBorder
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ringkasan
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryCardJaringan("ODC", odcList.size.toString(), cardBg, textMain, primaryBg, Modifier.weight(1f))
                        SummaryCardJaringan("ODP", odpList.size.toString(), cardBg, textMain, primaryBg, Modifier.weight(1f))
                        SummaryCardJaringan("RASIO", rasioList.size.toString(), cardBg, textMain, primaryBg, Modifier.weight(1f))
                    }
                }
                
                // Toggle Mode View
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == "Daftar Jaringan") primaryBg else Color.Transparent)
                                .clickable { activeTab = "Daftar Jaringan" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Daftar Jaringan",
                                color = if (activeTab == "Daftar Jaringan") Color.Black else textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == "Pohon Koneksi") primaryBg else Color.Transparent)
                                .clickable { activeTab = "Pohon Koneksi" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Silsilah Jaringan",
                                color = if (activeTab == "Pohon Koneksi") Color.Black else textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Filter Area (Show in both tabs)
                item {
                    val filterOptionsArea = listOf("All") + areaList.map { it.name }.distinct()
                    var filterAreaExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = filterArea,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter by Area", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { filterAreaExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Area", tint = textSecondary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain, unfocusedTextColor = textMain,
                                focusedContainerColor = cardBg, unfocusedContainerColor = cardBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = filterAreaExpanded,
                            onDismissRequest = { filterAreaExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            filterOptionsArea.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = textMain) },
                                    onClick = {
                                        filterArea = option
                                        filterAreaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                if (activeTab == "Daftar Jaringan") {
                    // Form Pencarian
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Cari ODC / ODP / RASIO...", color = textSecondary) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain, unfocusedTextColor = textMain,
                                focusedContainerColor = cardBg, unfocusedContainerColor = cardBg
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    
                    val filteredOdc = odcList.filter { (filterArea == "All" || it.area == filterArea) && (it.name.contains(searchQuery, true) || it.location.contains(searchQuery, true)) }
                    val filteredOdp = odpList.filter { (filterArea == "All" || it.area == filterArea) && (it.name.contains(searchQuery, true)) }
                    val filteredRasio = rasioList.filter { (filterArea == "All" || it.area == filterArea) && (it.name.contains(searchQuery, true) || it.location.contains(searchQuery, true)) }
                    
                    if (filteredOdc.isNotEmpty()) {
                        item { Text("Data ODC", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        items(filteredOdc) { odc ->
                            ExpandableOdcItem(odc, odpList, odcList, rasioList, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                        }
                    }
                    
                    if (filteredOdp.isNotEmpty()) {
                        item { Text("Data ODP", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        items(filteredOdp) { odp ->
                            val users = customerList.filter { it.odpId == odp.id }
                            ExpandableOdpItem(odp, odpList, odcList, rasioList, users, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                        }
                    }
                    
                    if (filteredRasio.isNotEmpty()) {
                        item { Text("Data RASIO", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        items(filteredRasio) { rasio ->
                            ExpandableRasioItem(rasio, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                        }
                    }
                } else {
                    // SILSILAH JARINGAN (TREE HIERARCHY)
                    val filteredOdc = odcList.filter { filterArea == "All" || it.area == filterArea }
                    val filteredRasio = rasioList.filter { filterArea == "All" || it.area == filterArea }
                    val filteredOdp = odpList.filter { filterArea == "All" || it.area == filterArea }

                    if (filteredOdc.isEmpty() && filteredRasio.isEmpty() && filteredOdp.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Tidak ada data jaringan di area ini", color = textSecondary)
                            }
                        }
                    } else {
                        items(filteredOdc) { odc ->
                            OdcTreeNode(odc, odpList, rasioList, customerList, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                        }
                        
                        // Standalone Rasio (Rasio whose portInput doesn't match any ODC)
                        val standaloneRasio = filteredRasio.filter { rasio ->
                            odcList.none { odc -> rasio.portInput.contains(odc.name, ignoreCase = true) }
                        }
                        if (standaloneRasio.isNotEmpty()) {
                            item {
                                Text("RASIO Tanpa Induk ODC", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(standaloneRasio) { rasio ->
                                RasioTreeNode(rasio, odpList, customerList, 0, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                            }
                        }
                        
                        // Standalone ODP (ODP whose parent is not found or odcId is '0')
                        val standaloneOdp = filteredOdp.filter { odp ->
                            odp.odcId == "0" || (
                                odp.odcId.startsWith("-").not() && odcList.none { odc -> odp.odcId == odc.id }
                            ) && (
                                odp.odcId.startsWith("-") && rasioList.none { rasio -> odp.odcId.removePrefix("-") == rasio.id }
                            ) && (
                                odp.odcId.startsWith("-") && odp.odcId.removePrefix("-").startsWith("100000") && odpList.none { parent -> odp.odcId.removePrefix("-").removePrefix("100000") == parent.id }
                            )
                        }
                        if (standaloneOdp.isNotEmpty()) {
                            item {
                                Text("ODP Tanpa Induk", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                            items(standaloneOdp) { odp ->
                                OdpTreeNode(odp, odpList, customerList, 0, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun SummaryCardJaringan(title: String, value: String, cardBg: Color, textMain: Color, primaryBg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = primaryBg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpandableOdcItem(odc: OdcItem, odpList: List<OdpItem>, odcList: List<OdcItem>, rasioList: List<RasioItem>, cardBg: Color, textMain: Color, textSecondary: Color, primaryBg: Color, cardBorder: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ODC: ${odc.name}", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand", tint = textSecondary)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lokasi: ${odc.location} | Area: ${odc.area}", color = textSecondary, fontSize = 14.sp)
                Text("Sumber Input: ${odc.portInput}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman In: ${odc.redamanIn}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman Out: ${odc.redamanOut}", color = textSecondary, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                val usedByOdp = odpList.filter { it.portInput == odc.name }
                val usedByOdc = odcList.filter { it.portInput == odc.name && it.id != odc.id }
                val terpakai = usedByOdp.size + usedByOdc.size
                val kosong = (odc.portCount - terpakai).coerceAtLeast(0)
                
                Text("Total Port: ${odc.portCount}", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Port Kosong: $kosong", color = Color(0xFF51A351), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Port Terpakai: $terpakai", color = Color(0xFFFF9900), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                if (terpakai > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Data Pemakai:", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    usedByOdc.forEach { item ->
                        Text("- ODC: ${item.name}", color = textSecondary, fontSize = 12.sp)
                    }
                    usedByOdp.forEach { item ->
                        Text("- ODP: ${item.name}", color = textSecondary, fontSize = 12.sp)
                    }
                    
                }
            }
        }
    }
}
@Composable
fun ExpandableOdpItem(odp: OdpItem, odpList: List<OdpItem>, odcList: List<OdcItem>, rasioList: List<RasioItem>, users: List<Customer>, cardBg: Color, textMain: Color, textSecondary: Color, primaryBg: Color, cardBorder: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ODP: ${odp.name}", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand", tint = textSecondary)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                val parentOdc = if (odp.odcId.startsWith("-")) {
                    val absoluteId = odp.odcId.removePrefix("-")
                    if (absoluteId.startsWith("100000")) {
                        val realOdpId = absoluteId.removePrefix("100000")
                        odpList.find { it.id == realOdpId }?.let { OdcItem(id = odp.odcId, name = it.name, location = "", area = it.area) }
                    } else {
                        rasioList.find { it.id == absoluteId }?.let { OdcItem(id = odp.odcId, name = it.name, location = it.location, area = it.area) }
                    }
                } else {
                    odcList.find { it.id == odp.odcId }
                } ?: odcList.find { it.name == odp.portInput } ?: odcList.find { odp.portInput.contains(it.name) }
                val displayArea = odp.area.takeIf { it.isNotEmpty() } ?: parentOdc?.area.orEmpty()
                Text("Area: ${displayArea.takeIf { it.isNotEmpty() } ?: "-"}", color = textSecondary, fontSize = 14.sp)
                Text("Sumber Input: ${odp.portInput}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman In: ${odp.redamanIn}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman Out: ${odp.redamanOut}", color = textSecondary, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                val usedByOdp = odpList.filter { it.portInput == odp.name && it.id != odp.id }
                val usedByOdc = odcList.filter { it.portInput == odp.name }
                val terpakai = users.size + usedByOdp.size + usedByOdc.size
                val kosong = (odp.portCount - terpakai).coerceAtLeast(0)
                
                Text("Total Port: ${odp.portCount}", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Port Kosong: $kosong", color = Color(0xFF51A351), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Port Terpakai: $terpakai", color = Color(0xFFFF9900), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                
                if (terpakai > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Data Pemakai:", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    users.forEach { user ->
                        Text("- Pelanggan: ${user.name}", color = textSecondary, fontSize = 12.sp)
                    }
                    usedByOdc.forEach { item ->
                        Text("- ODC: ${item.name}", color = textSecondary, fontSize = 12.sp)
                    }
                    usedByOdp.forEach { item ->
                        Text("- ODP: ${item.name}", color = textSecondary, fontSize = 12.sp)
                    }
                    
                }
            }
        }
    }
}
@Composable
fun ExpandableRasioItem(rasio: RasioItem, cardBg: Color, textMain: Color, textSecondary: Color, primaryBg: Color, cardBorder: Color) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RASIO: ${rasio.name}", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand", tint = textSecondary)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lokasi: ${rasio.location} | Area: ${rasio.area}", color = textSecondary, fontSize = 14.sp)
                Text("Ukuran Rasio: ${rasio.size}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman A: ${rasio.redamanOutA}", color = textSecondary, fontSize = 14.sp)
                Text("Redaman B: ${rasio.redamanOutB}", color = textSecondary, fontSize = 14.sp)
                if (rasio.portInput.isNotEmpty()) {
                    Text("Sumber Port Input: ${rasio.portInput}", color = textSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun OdcTreeNode(
    odc: OdcItem,
    odpList: List<OdpItem>,
    rasioList: List<RasioItem>,
    customerList: List<Customer>,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    primaryBg: Color,
    cardBorder: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Find children
    val rasioChildren = rasioList.filter { it.portInput.contains(odc.name, ignoreCase = true) }
    val odpChildrenDirect = odpList.filter { it.odcId == odc.id }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeviceHub, contentDescription = "ODC", tint = primaryBg, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ODC: ${odc.name}", fontWeight = FontWeight.Bold, color = textMain, fontSize = 15.sp)
                        Text("Area: ${odc.area} | Port: ${odc.portCount}", color = textSecondary, fontSize = 12.sp)
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = textSecondary
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider(color = cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (rasioChildren.isEmpty() && odpChildrenDirect.isEmpty()) {
                    Text("Belum ada perangkat terhubung", color = textSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
                } else {
                    rasioChildren.forEach { rasio ->
                        RasioTreeNode(rasio, odpList, customerList, 1, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                    }
                    odpChildrenDirect.forEach { odp ->
                        OdpTreeNode(odp, odpList, customerList, 1, cardBg, textMain, textSecondary, primaryBg, cardBorder)
                    }
                }
            }
        }
    }
}

@Composable
fun RasioTreeNode(
    rasio: RasioItem,
    odpList: List<OdpItem>,
    customerList: List<Customer>,
    indentation: Int,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    primaryBg: Color,
    cardBorder: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val odpChildren = odpList.filter { it.odcId == "-${rasio.id}" }
    
    val paddingStart = (indentation * 16).dp
    
    Column(modifier = Modifier.fillMaxWidth().padding(start = paddingStart, top = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(primaryBg.copy(alpha = 0.05f))
                .border(1.dp, cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("┕ ", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.Share, contentDescription = "Rasio", tint = Color(0xFFFF9900), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("RASIO: ${rasio.name}", fontWeight = FontWeight.SemiBold, color = textMain, fontSize = 13.sp)
                Text("Ukuran: ${rasio.size} | In: ${rasio.redamanIn}", color = textSecondary, fontSize = 11.sp)
            }
            if (odpChildren.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        if (expanded) {
            odpChildren.forEach { odp ->
                OdpTreeNode(odp, odpList, customerList, indentation + 1, cardBg, textMain, textSecondary, primaryBg, cardBorder)
            }
        }
    }
}

@Composable
fun OdpTreeNode(
    odp: OdpItem,
    odpList: List<OdpItem>,
    customerList: List<Customer>,
    indentation: Int,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    primaryBg: Color,
    cardBorder: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val childCustomers = customerList.filter { it.odpId == odp.id }
    val childOdp = odpList.filter { it.odcId == "-100000${odp.id}" }
    
    val paddingStart = (indentation * 16).dp
    
    Column(modifier = Modifier.fillMaxWidth().padding(start = paddingStart, top = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(primaryBg.copy(alpha = 0.08f))
                .border(1.dp, cardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("┕ ", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(Icons.Default.AccountTree, contentDescription = "ODP", tint = primaryBg, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("ODP: ${odp.name}", fontWeight = FontWeight.SemiBold, color = textMain, fontSize = 13.sp)
                Text("In: ${odp.redamanIn} | Out: ${odp.redamanOut}", color = textSecondary, fontSize = 11.sp)
            }
            if (childCustomers.isNotEmpty() || childOdp.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        if (expanded) {
            childOdp.forEach { subOdp ->
                OdpTreeNode(subOdp, odpList, customerList, indentation + 1, cardBg, textMain, textSecondary, primaryBg, cardBorder)
            }
            childCustomers.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (paddingStart + 24.dp), top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("┕ ", color = textSecondary, fontSize = 12.sp)
                    Icon(Icons.Default.Person, contentDescription = "Pelanggan", tint = Color(0xFF00FF4D), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(user.name, fontSize = 12.sp, color = textMain, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
