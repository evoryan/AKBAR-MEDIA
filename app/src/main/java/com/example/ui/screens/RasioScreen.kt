package com.example.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.RasioItem
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasioScreen(onBack: () -> Unit) {
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var rasioList by remember { mutableStateOf<List<RasioItem>>(emptyList()) }
    var areaList by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    var odpList by remember { mutableStateOf<List<com.example.ui.data.OdpItem>>(emptyList()) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<RasioItem?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedAreaFilter by remember { mutableStateOf<com.example.ui.screens.Area?>(null) }
    var areaFilterExpanded by remember { mutableStateOf(false) }
    
    val filteredRasioList = rasioList.filter { item ->
        val matchesArea = selectedAreaFilter == null || item.area.equals(selectedAreaFilter?.name ?: "", ignoreCase = true)
        val matchesSearch = searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true) || item.location.contains(searchQuery, ignoreCase = true)
        matchesArea && matchesSearch
    }
    
    LaunchedEffect(Unit) {
        try {
            rasioList = ApiClient.apiService.getRasioList()
            areaList = ApiClient.apiService.getAreas()
            odcList = ApiClient.apiService.getOdcList()
            odpList = ApiClient.apiService.getOdpList()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat rasio", Toast.LENGTH_SHORT).show()
        } finally {
            isBackgroundLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Rasio", color = textMain, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgMain)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editItem = null; showDialog = true },
                containerColor = primaryBg,
                contentColor = bgMain
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Rasio")
            }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        placeholder = { Text("Cari Rasio...", fontSize = 14.sp) },
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
                                imageVector = androidx.compose.material.icons.Icons.Default.Search,
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(filteredRasioList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lokasi: ${item.location} | Area: ${item.area}", color = textSecondary, fontSize = 12.sp)
                                Text("Ukuran: ${item.size}", color = textSecondary, fontSize = 12.sp)
                                Text("In: ${item.redamanIn} | Out A: ${item.redamanOutA} | Out B: ${item.redamanOutB}", color = textSecondary, fontSize = 12.sp)
                                if (item.portInput.isNotEmpty()) {
                                    Text("Sumber Port Input: ${item.portInput}", color = textSecondary, fontSize = 12.sp)
                                }
                            }
                            Row {
                                IconButton(onClick = { editItem = item; showDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryBg)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.deleteRasio(item.id)
                                            rasioList = rasioList.filter { it.id != item.id }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal hapus: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
    
    if (showDialog) {
        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var area by remember { mutableStateOf(editItem?.area ?: "") }
        var location by remember { mutableStateOf(editItem?.location ?: "") }
        var size by remember { mutableStateOf(editItem?.size ?: "") }
        var redamanIn by remember { mutableStateOf(editItem?.redamanIn ?: "") }
        var redamanOutA by remember { mutableStateOf(editItem?.redamanOutA ?: "") }
        var redamanOutB by remember { mutableStateOf(editItem?.redamanOutB ?: "") }
        var sumberPort by remember { mutableStateOf(editItem?.portInput ?: "") }
        var sumberPortExpanded by remember { mutableStateOf(false) }
        var searchPortQuery by remember { mutableStateOf("") }
        
        val sizeOptions = listOf(
            "01:99" to Pair(20.0f, 0.05f),
            "02:98" to Pair(17.0f, 0.10f),
            "03:97" to Pair(15.2f, 0.13f),
            "04:96" to Pair(14.0f, 0.18f),
            "05:95" to Pair(13.0f, 0.22f),
            "06:94" to Pair(12.2f, 0.27f),
            "07:93" to Pair(11.5f, 0.32f),
            "08:92" to Pair(11.0f, 0.36f),
            "09:91" to Pair(10.5f, 0.41f),
            "10:90" to Pair(10.0f, 0.46f),
            "15:85" to Pair(8.2f, 0.71f),
            "20:80" to Pair(7.0f, 0.97f),
            "25:75" to Pair(6.0f, 1.25f),
            "30:70" to Pair(5.2f, 1.55f),
            "35:65" to Pair(4.6f, 1.87f),
            "40:60" to Pair(4.0f, 2.22f),
            "45:55" to Pair(3.5f, 2.60f),
            "50:50" to Pair(3.0f, 3.00f)
        )

        val calculateOutputs = { currentIn: String, currentSize: String ->
            val rIn = currentIn.toFloatOrNull()
            if (rIn != null) {
                val selectedOption = sizeOptions.find { it.first == currentSize }
                if (selectedOption != null) {
                    redamanOutA = String.format(java.util.Locale.US, "%.2f", rIn - selectedOption.second.first)
                    redamanOutB = String.format(java.util.Locale.US, "%.2f", rIn - selectedOption.second.second)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah Rasio" else "Edit Rasio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Rasio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    
                    var areaExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { areaExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Area")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = areaExpanded,
                            onDismissRequest = { areaExpanded = false },
                            modifier = Modifier.background(cardBg)
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
                    var sizeExpanded by remember { mutableStateOf(false) }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Ukuran") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { sizeExpanded = !sizeExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Ukuran")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { sizeExpanded = true }
                        )
                        DropdownMenu(
                            expanded = sizeExpanded,
                            onDismissRequest = { sizeExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            sizeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first, color = textMain) },
                                    onClick = {
                                        size = option.first
                                        sizeExpanded = false
                                        calculateOutputs(redamanIn, option.first)
                                    }
                                )
                            }
                        }
                    }
                    
                    val filteredOdcForSumber = odcList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredOdpForSumber = odpList.filter { it.area.equals(area, ignoreCase = true) }
                    val filteredRasioForSumber = rasioList.filter { it.area.equals(area, ignoreCase = true) && it.id != (editItem?.id ?: "") }

                    val sumberOptions = filteredOdcForSumber.map { Triple(it.name, it.redamanOut, "ODC") } +
                                        filteredOdpForSumber.map { Triple(it.name, it.redamanOut, "ODP") } +
                                        filteredRasioForSumber.flatMap { listOf(
                                            Triple(it.name + " (Out A)", it.redamanOutA, "RASIO"),
                                            Triple(it.name + " (Out B)", it.redamanOutB, "RASIO")
                                        ) }
                    
                    Box {
                        OutlinedTextField(
                            value = sumberPort,
                            onValueChange = { sumberPort = it },
                            label = { Text("Sumber Port Input") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { sumberPortExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Sumber")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = sumberPortExpanded,
                            onDismissRequest = { sumberPortExpanded = false },
                            modifier = Modifier.background(cardBg).width(250.dp).heightIn(max = 300.dp)
                        ) {
                            OutlinedTextField(
                                value = searchPortQuery,
                                onValueChange = { searchPortQuery = it },
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
                                it.first.contains(searchPortQuery, ignoreCase = true)
                            }
                            if (finalSumberOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada hasil", color = textSecondary, fontSize = 12.sp) },
                                    onClick = {}
                                )
                            } else {
                                finalSumberOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text("${option.first} (${option.third})", color = textMain, fontSize = 13.sp) },
                                        onClick = {
                                            sumberPort = option.first
                                            redamanIn = option.second
                                            sumberPortExpanded = false
                                            calculateOutputs(option.second, size)
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
                            calculateOutputs(it, size)
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        label = { Text("Redaman Input") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOutA,
                        onValueChange = { redamanOutA = it },
                        label = { Text("Redaman Output A") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOutB,
                        onValueChange = { redamanOutB = it },
                        label = { Text("Redaman Output B") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newItem = RasioItem(
                        id = editItem?.id ?: "",
                        name = name,
                        location = location,
                        size = size,
                        redamanIn = redamanIn,
                        redamanOutA = redamanOutA,
                        redamanOutB = redamanOutB,
                        area = area,
                        portInput = sumberPort
                    )
                    coroutineScope.launch {
                        try {
                            if (editItem == null) {
                                ApiClient.apiService.addRasio(newItem)
                            } else {
                                ApiClient.apiService.updateRasio(newItem.id, newItem)
                            }
                            rasioList = ApiClient.apiService.getRasioList()
            areaList = ApiClient.apiService.getAreas()
            odcList = ApiClient.apiService.getOdcList()
            odpList = ApiClient.apiService.getOdpList()
                            showDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
}
