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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.unit.sp
import com.example.ui.data.remote.ApiClient
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import com.example.ui.data.OdpItem
import com.example.ui.data.OdcItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    var odpList by remember { mutableStateOf<List<com.example.ui.data.OdpItem>>(emptyList()) }
    var areaList by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    var rasioList by remember { mutableStateOf<List<com.example.ui.data.RasioItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdpList()
            odpList = res
        } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdpScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
        }
    }
    
    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdcList()
            odcList = res
            areaList = ApiClient.apiService.getAreas()
            rasioList = ApiClient.apiService.getRasioList()
        } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdpScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<OdpItem?>(null) }
    var itemToDelete by remember { mutableStateOf<OdpItem?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedAreaFilter by remember { mutableStateOf<com.example.ui.screens.Area?>(null) }
    var areaFilterExpanded by remember { mutableStateOf(false) }
    
    val filteredOdpList = odpList.filter { item ->
        val odc = if (item.odcId.startsWith("-")) {
            val absoluteId = item.odcId.removePrefix("-")
            if (absoluteId.startsWith("100000")) {
                val realOdpId = absoluteId.removePrefix("100000")
                odpList.find { it.id == realOdpId }?.let { OdcItem(id = item.odcId, name = it.name, location = "", area = it.area) }
            } else {
                rasioList.find { it.id == absoluteId }?.let { OdcItem(id = item.odcId, name = it.name, location = it.location, area = it.area) }
            }
        } else {
            odcList.find { it.id == item.odcId }
        } ?: odcList.find { it.name == item.portInput } ?: odcList.find { item.portInput.contains(it.name) }
        val displayArea = item.area.takeIf { it.isNotEmpty() } ?: odc?.area.orEmpty()
        val matchesArea = selectedAreaFilter == null || displayArea.equals(selectedAreaFilter?.name ?: "", ignoreCase = true)
        val matchesSearch = searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true) || item.portInput.contains(searchQuery, ignoreCase = true)
        matchesArea && matchesSearch
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kelola ODP", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editItem = null
                    showDialog = true 
                },
                containerColor = primaryBg,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah ODP")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari ODP...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain,
                        focusedBorderColor = primaryBg,
                        unfocusedBorderColor = cardBorder
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedAreaFilter?.name ?: "Semua Area",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { areaFilterExpanded = true },
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        trailingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                contentDescription = "Area Dropdown",
                                tint = textSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            disabledTextColor = textMain,
                            disabledBorderColor = cardBorder
                        ),
                        enabled = false
                    )
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { areaFilterExpanded = true }
                    )

                    DropdownMenu(
                        expanded = areaFilterExpanded,
                        onDismissRequest = { areaFilterExpanded = false },
                        modifier = Modifier
                            .background(cardBg)
                            .fillMaxWidth(0.45f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Area", color = textMain, fontSize = 14.sp) },
                            onClick = {
                                selectedAreaFilter = null
                                areaFilterExpanded = false
                            }
                        )
                        areaList.forEach { areaItem ->
                            DropdownMenuItem(
                                text = { Text(areaItem.name, color = textMain, fontSize = 14.sp) },
                                onClick = {
                                    selectedAreaFilter = areaItem
                                    areaFilterExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(filteredOdpList) { item ->
                    val odc = if (item.odcId.startsWith("-")) {
                        val absoluteId = item.odcId.removePrefix("-")
                        if (absoluteId.startsWith("100000")) {
                            val realOdpId = absoluteId.removePrefix("100000")
                            odpList.find { it.id == realOdpId }?.let { OdcItem(id = item.odcId, name = it.name, location = "", area = it.area) }
                        } else {
                            rasioList.find { it.id == absoluteId }?.let { OdcItem(id = item.odcId, name = it.name, location = it.location, area = it.area) }
                        }
                    } else {
                        odcList.find { it.id == item.odcId }
                    } ?: odcList.find { it.name == item.portInput } ?: odcList.find { item.portInput.contains(it.name) }
                    val displayArea = item.area.takeIf { it.isNotEmpty() } ?: odc?.area.orEmpty()
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Hub, contentDescription = "ODP", tint = primaryBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val parentLabel = if (item.odcId.startsWith("-") && !item.odcId.removePrefix("-").startsWith("100000")) "RASIO" else if (item.odcId.startsWith("-")) "ODP" else "ODC"
                                    Text("$parentLabel: ${odc?.name ?: "Unknown"} | Area: ${displayArea.takeIf { it.isNotEmpty() } ?: "-"}", color = textSecondary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Port: ${item.portCount} | Input: ${item.portInput}", color = primaryBg, fontSize = 14.sp)
                                }
                            }
                            Row {
                                if (currentUser?.role == UserRole.SUPER_ADMIN) {
                                    IconButton(onClick = {
                                    editItem = item
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryBg)
                                }
                                IconButton(onClick = {
                                    itemToDelete = item
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                        if (isExpanded) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorder)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman In", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanIn.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman Out", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanOut.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
            }
        }
    }

    if (showDialog) {
        var name by remember(editItem) { mutableStateOf(editItem?.name ?: "") }
        var area by remember(editItem) { mutableStateOf(editItem?.area ?: "") }
        var portCount by remember(editItem) { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var portInput by remember(editItem) { mutableStateOf(editItem?.portInput ?: "") }
        var redamanIn by remember(editItem) { mutableStateOf(editItem?.redamanIn ?: "") }
        var redamanOut by remember(editItem) { mutableStateOf(editItem?.redamanOut ?: "") }
        var selectedOdc by remember(editItem) { mutableStateOf(editItem?.odcId ?: "0") }
        

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah ODP" else "Edit ODP") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama ODP") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )

                    var areaExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = area.takeIf { it.isNotEmpty() } ?: "Pilih Area",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Area") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { areaExpanded = !areaExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Area")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { areaExpanded = true }
                        )
                        DropdownMenu(
                            expanded = areaExpanded,
                            onDismissRequest = { areaExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            areaList.forEach { a ->
                                DropdownMenuItem(
                                    text = { Text(a.name, color = textMain) },
                                    onClick = {
                                        area = a.name
                                        areaExpanded = false
                                        // Reset selected koneksi and port input if area changes
                                        selectedOdc = "0"
                                        portInput = ""
                                    }
                                )
                            }
                        }
                    }

                    val filteredOdcForKoneksi = odcList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredOdpForKoneksi = odpList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredRasioForKoneksi = rasioList.filter { it.area.equals(area, ignoreCase = true) }

                    val koneksiOptions = filteredOdcForKoneksi.map { Triple(it.name, it.id, "ODC") } +
                                         filteredOdpForKoneksi.map { Triple(it.name, "-100000${it.id}", "ODP") } +
                                         filteredRasioForKoneksi.map { Triple(it.name, "-${it.id}", "RASIO") }

                    val selectedKoneksiName = if (selectedOdc.startsWith("-")) {
                        val absoluteId = selectedOdc.removePrefix("-")
                        if (absoluteId.startsWith("100000")) {
                            val realOdpId = absoluteId.removePrefix("100000")
                            odpList.find { it.id == realOdpId }?.let { "${it.name} (ODP)" }
                        } else {
                            rasioList.find { it.id == absoluteId }?.let { "${it.name} (RASIO)" }
                        }
                    } else {
                        odcList.find { it.id == selectedOdc }?.let { "${it.name} (ODC)" }
                    } ?: "Pilih Koneksi"

                    var odcExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedKoneksiName,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Koneksi ODC") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { odcExpanded = !odcExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih ODC")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { odcExpanded = true }
                        )
                        DropdownMenu(
                            expanded = odcExpanded,
                            onDismissRequest = { odcExpanded = false },
                            modifier = Modifier.background(cardBg).heightIn(max = 250.dp).fillMaxWidth(0.9f)
                        ) {
                            if (area.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Pilih Area terlebih dahulu", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else if (koneksiOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada perangkat di area ini", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else {
                                koneksiOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text("${option.first} (${option.third})", color = textMain) },
                                        onClick = {
                                            selectedOdc = option.second
                                            odcExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    var portCountExpanded by remember { mutableStateOf(false) }
                    val portOptions = listOf("1:2" to "2", "1:4" to "4", "1:8" to "8", "1:16" to "16")
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = portOptions.find { it.second == portCount }?.first ?: portCount,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Jumlah Port") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { portCountExpanded = !portCountExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Jumlah Port")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { portCountExpanded = true }
                        )
                        DropdownMenu(
                            expanded = portCountExpanded,
                            onDismissRequest = { portCountExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            portOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first, color = textMain) },
                                    onClick = {
                                        portCount = option.second
                                        portCountExpanded = false
                                        val rIn = redamanIn.toFloatOrNull()
                                        val rSplitter = when (option.second) {
                                            "2" -> 4.0f
                                            "4" -> 7.2f
                                            "8" -> 10.5f
                                            "16" -> 13.8f
                                            else -> 0.0f
                                        }
                                        if (rIn != null && rSplitter > 0.0f) {
                                            redamanOut = String.format(java.util.Locale.US, "%.2f", rIn - rSplitter)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    var portInputExpanded by remember { mutableStateOf(false) }
                    val filteredOdcForSumber = odcList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredOdpForSumber = odpList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredRasioForSumber = rasioList.filter { it.area.equals(area, ignoreCase = true) }

                    val sumberOptions = filteredOdcForSumber.map { it.name to it.redamanOut } +
                                        filteredOdpForSumber.map { it.name to it.redamanOut } +
                                        filteredRasioForSumber.flatMap { listOf(it.name + " (Out A)" to it.redamanOutA, it.name + " (Out B)" to it.redamanOutB) }
                    
                    Box {
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Sumber Port Input") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { portInputExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Sumber")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = portInputExpanded,
                            onDismissRequest = { portInputExpanded = false },
                            modifier = Modifier.background(cardBg).heightIn(max = 250.dp)
                        ) {
                            if (area.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Pilih Area terlebih dahulu", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else if (sumberOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada perangkat di area ini", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else {
                                sumberOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.first, color = textMain) },
                                        onClick = {
                                            portInput = option.first
                                            redamanIn = option.second
                                            portInputExpanded = false
                                            
                                            // Auto-calculate redamanOut based on new redamanIn and portCount
                                            val rIn = option.second.toFloatOrNull()
                                            val rSplitter = when (portCount) {
                                                "2" -> 4.0f
                                                "4" -> 7.2f
                                                "8" -> 10.5f
                                                "16" -> 13.8f
                                                else -> 0.0f
                                            }
                                            if (rIn != null && rSplitter > 0.0f) {
                                                redamanOut = String.format(java.util.Locale.US, "%.2f", rIn - rSplitter)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { 
                            redamanIn = it
                            val rIn = it.toFloatOrNull()
                            val rSplitter = when (portCount) {
                                "2" -> 4.0f
                                "4" -> 7.2f
                                "8" -> 10.5f
                                "16" -> 13.8f
                                else -> 0.0f
                            }
                            if (rIn != null && rSplitter > 0.0f) {
                                redamanOut = String.format(java.util.Locale.US, "%.2f", rIn - rSplitter)
                            }
                        },
                        label = { Text("Redaman In") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val port = portCount.toIntOrNull() ?: 0
                    if (selectedOdc == "0" || selectedOdc.isEmpty()) {
                        Toast.makeText(context, "Silakan pilih ODC terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdpItem(
                                    id = "",
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port,
                                    portInput = portInput,
                                    redamanIn = redamanIn,
                                    redamanOut = redamanOut,
                                    area = area
                                )
                                ApiClient.apiService.addOdp(newItem)
                                odpList = ApiClient.apiService.getOdpList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdpScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch(e: Exception) {
                                Log.e("OdpScreen", "Error adding ODP", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedItem = OdpItem(
                                    id = editItem!!.id,
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port,
                                    portInput = portInput,
                                    redamanIn = redamanIn,
                                    redamanOut = redamanOut,
                                    area = area
                                )
                                ApiClient.apiService.updateOdp(updatedItem.id, updatedItem)
                                odpList = ApiClient.apiService.getOdpList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdpScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("OdpScreen", "Error updating ODP", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text("Simpan", color = primaryBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text("Konfirmasi Hapus") },
            text = { Text("Apakah Anda yakin ingin menghapus ODP '${itemToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    val odp = itemToDelete!!
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.deleteOdp(odp.id)
                            odpList = odpList.filter { it.id != odp.id }
                            itemToDelete = null
                        } catch(e: retrofit2.HttpException) {
                            val errBody = e.response()?.errorBody()?.string()
                            Log.e("OdpScreen", "HTTP Error: $errBody", e)
                            Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Hapus", color = Color(0xFFFF003C))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}
}
