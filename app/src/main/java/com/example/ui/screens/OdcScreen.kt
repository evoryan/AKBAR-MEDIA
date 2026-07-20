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
import androidx.compose.material.icons.filled.DeviceHub
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
import com.example.ui.data.OdcItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdcScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    var areaList by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    var odpList by remember { mutableStateOf<List<com.example.ui.data.OdpItem>>(emptyList()) }
    var rasioList by remember { mutableStateOf<List<com.example.ui.data.RasioItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            com.example.ui.data.UserSession.getOrFetchAreas()
            val res = ApiClient.apiService.getOdcList()
            odcList = res
            areaList = ApiClient.apiService.getAreas().filter { com.example.ui.data.UserSession.isAreaIdAllowed(it.id) }
            odpList = ApiClient.apiService.getOdpList()
            rasioList = ApiClient.apiService.getRasioList()
        } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<OdcItem?>(null) }
    var itemToDelete by remember { mutableStateOf<OdcItem?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedAreaFilter by remember { mutableStateOf<com.example.ui.screens.Area?>(null) }
    var areaFilterExpanded by remember { mutableStateOf(false) }
    
    val filteredOdcList = odcList.filter { item ->
        val matchesArea = selectedAreaFilter == null || item.area.equals(selectedAreaFilter?.name ?: "", ignoreCase = true)
        val matchesSearch = searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true) || item.location.contains(searchQuery, ignoreCase = true)
        matchesArea && matchesSearch && com.example.ui.data.UserSession.isAreaNameAllowed(item.area)
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kelola ODC", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah ODC")
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
                    placeholder = { Text("Cari ODC...", fontSize = 14.sp) },
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
                items(filteredOdcList) { item ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeviceHub, contentDescription = "ODC", tint = primaryBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.location + " | Area: " + item.area, color = textSecondary, fontSize = 14.sp)
                                    if (item.portCount > 0) {
                                        Text("Port: ${item.portCount} | Input: ${item.portInput}", color = primaryBg, fontSize = 12.sp)
                                    }
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
        var location by remember(editItem) { mutableStateOf(editItem?.location ?: "") }
        var portCount by remember(editItem) { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var portInput by remember(editItem) { mutableStateOf(editItem?.portInput ?: "") }
        var redamanIn by remember(editItem) { mutableStateOf(editItem?.redamanIn ?: "") }
        var redamanOut by remember(editItem) { mutableStateOf(editItem?.redamanOut ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah ODC" else "Edit ODC") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama ODC") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi") },
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
                                    }
                                )
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
                    var sumberSearchQuery by remember { mutableStateOf("") }
                    val filteredOdcForSumber = odcList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredRasioForSumber = rasioList.filter { it.area.equals(area, ignoreCase = true) }
                    val sumberOptions = filteredOdcForSumber.map { it.name to it.redamanOut } + filteredRasioForSumber.flatMap { listOf(it.name + " (Out A)" to it.redamanOutA, it.name + " (Out B)" to it.redamanOutB) }
                    
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
                            modifier = Modifier.background(cardBg).width(250.dp).heightIn(max = 300.dp)
                        ) {
                            OutlinedTextField(
                                value = sumberSearchQuery,
                                onValueChange = { sumberSearchQuery = it },
                                placeholder = { Text("Cari...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryBg,
                                    unfocusedBorderColor = cardBorder,
                                    focusedTextColor = textMain,
                                    unfocusedTextColor = textMain
                                )
                            )
                            val finalSumberOptions = sumberOptions.filter {
                                it.first.contains(sumberSearchQuery, ignoreCase = true)
                            }
                            if (finalSumberOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada hasil", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else {
                                finalSumberOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.first, color = textMain, fontSize = 13.sp) },
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
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdcItem(
                                    id = "",
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput,
                                    redamanIn = redamanIn,
                                    redamanOut = redamanOut,
                                    area = area
                                )
                                ApiClient.apiService.addOdc(newItem)
                                odcList = ApiClient.apiService.getOdcList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch(e: Exception) {
                                Log.e("OdcScreen", "Error adding ODC", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedItem = OdcItem(
                                    id = editItem!!.id,
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput,
                                    redamanIn = redamanIn,
                                    redamanOut = redamanOut,
                                    area = area
                                )
                                ApiClient.apiService.updateOdc(updatedItem.id, updatedItem)
                                odcList = ApiClient.apiService.getOdcList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("OdcScreen", "Error updating ODC", e)
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
            text = { Text("Apakah Anda yakin ingin menghapus ODC '${itemToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    val odc = itemToDelete!!
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.deleteOdc(odc.id)
                            odcList = odcList.filter { it.id != odc.id }
                            itemToDelete = null
                        } catch(e: retrofit2.HttpException) {
                            val errBody = e.response()?.errorBody()?.string()
                            Log.e("OdcScreen", "HTTP Error: $errBody", e)
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
