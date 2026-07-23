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
import androidx.compose.material.icons.filled.*
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
import com.example.ui.data.UserSession
import com.example.ui.data.local.AppDatabase
import com.example.ui.data.local.GangguanEntity
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.PembukuanRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GangguanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)

    val listGangguan by db.gangguanDao().getAllGangguan().collectAsState(initial = emptyList())
    val currentUserState by UserSession.currentUser.collectAsState()
    val adminRealName = currentUserState?.name ?: "Admin"

    var listArea by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            listArea = UserSession.getOrFetchAreas().filter { UserSession.isAreaIdAllowed(it.id) }
        } catch (e: Exception) {
            Log.e("GangguanScreen", "Gagal memuat daftar area", e)
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGangguan by remember { mutableStateOf<GangguanEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Form fields (Shared for add & edit)
    var selectedAreaName by remember { mutableStateOf("") }
    var complaintDescription by remember { mutableStateOf("") }
    var teknisiName by remember { mutableStateOf("") }
    var biayaInput by remember { mutableStateOf("") }
    var statusChoice by remember { mutableStateOf("OTW") }
    var reporterName by remember { mutableStateOf(adminRealName) }

    val filteredGangguanList = listGangguan.filter {
        val areaAllowed = UserSession.isAreaNameAllowed(it.customerName)
        val matchesSearch = searchQuery.isEmpty() || 
            it.customerName.contains(searchQuery, ignoreCase = true) || 
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.reporter.contains(searchQuery, ignoreCase = true) ||
            it.teknisi.contains(searchQuery, ignoreCase = true)
        areaAllowed && matchesSearch
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Data Gangguan", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedAreaName = if (listArea.isNotEmpty()) listArea.first().name else ""
                    complaintDescription = ""
                    teknisiName = ""
                    biayaInput = ""
                    statusChoice = "OTW"
                    reporterName = adminRealName
                    showAddDialog = true
                },
                containerColor = primaryBg,
                contentColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Gangguan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Cari area, gangguan, teknisi...", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain,
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                )
            )

            if (filteredGangguanList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Aman Terkendali!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textMain
                        )
                        Text(
                            "Tidak ada laporan gangguan saat ini.",
                            fontSize = 14.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredGangguanList, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingGangguan = item
                                    selectedAreaName = item.customerName
                                    complaintDescription = item.description
                                    teknisiName = item.teknisi
                                    biayaInput = if (item.biaya > 0.0) item.biaya.toLong().toString() else ""
                                    statusChoice = item.status
                                    reporterName = item.reporter
                                },
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = BoxDefaults.borderStroke
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            if (item.customerName.isBlank()) "Area Umum" else item.customerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textMain
                                        )
                                        Text(
                                            "Reporter: ${item.reporter} | ${item.date}",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }

                                    // Status Badge ("OTW" / "SELESAI")
                                    val (badgeBg, badgeText, badgeLabel) = when (item.status) {
                                        "OTW" -> Triple(Color(0xFFFF9500).copy(alpha = 0.15f), Color(0xFFFF9500), "OTW")
                                        else -> Triple(Color(0xFF34C759).copy(alpha = 0.15f), Color(0xFF34C759), "Selesai")
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            badgeLabel,
                                            color = badgeText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    item.description,
                                    fontSize = 14.sp,
                                    color = textMain
                                )

                                if (item.teknisi.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Teknisi: ${item.teknisi}",
                                            fontSize = 13.sp,
                                            color = textSecondary
                                        )
                                    }
                                }

                                if (item.biaya > 0.0) {
                                    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"))
                                    format.maximumFractionDigits = 0
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Biaya: ${format.format(item.biaya)} (Masuk Pengeluaran)",
                                            fontSize = 13.sp,
                                            color = textMain,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (item.status == "SELESAI" && !item.resolverAdmin.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Diselesaikan oleh Admin: ${item.resolverAdmin}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF34C759),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = cardBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Action buttons depending on current status
                                    if (item.status == "OTW") {
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val currentAdminName = UserSession.currentUser.value?.name ?: "Admin"
                                                    db.gangguanDao().updateStatusAndResolver(item.id, "SELESAI", currentAdminName)
                                                    Toast.makeText(context, "Selesai diselesaikan oleh $currentAdminName", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Konfirmasi Selesai", color = Color(0xFF34C759), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    if (currentUserState?.role == com.example.ui.data.UserRole.SUPER_ADMIN) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    db.gangguanDao().deleteGangguan(item.id)
                                                    Toast.makeText(context, "Laporan dihapus", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF3B30), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Gangguan Dialog
    if (showAddDialog) {
        var areaDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = cardBg,
            title = { Text("Tambah Laporan Gangguan", color = textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pilihan Area Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedAreaName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Area", color = textSecondary) },
                            placeholder = { Text("Klik untuk memilih area...", color = textSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { areaDropdownExpanded = !areaDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { areaDropdownExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain,
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                disabledBorderColor = cardBorder,
                                disabledTextColor = textMain
                            ),
                            enabled = false // Disable direct text input to force clicking the dropdown/box
                        )

                        DropdownMenu(
                            expanded = areaDropdownExpanded,
                            onDismissRequest = { areaDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(cardBg)
                        ) {
                            if (listArea.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada area tersedia", color = textSecondary) },
                                    onClick = {}
                                )
                            } else {
                                listArea.forEach { area ->
                                    DropdownMenuItem(
                                        text = { Text(area.name, color = textMain) },
                                        onClick = {
                                            selectedAreaName = area.name
                                            areaDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Detail Gangguan (Mandatory)
                    OutlinedTextField(
                        value = complaintDescription,
                        onValueChange = { complaintDescription = it },
                        label = { Text("Detail Gangguan", color = textSecondary) },
                        placeholder = { Text("LOS merah, koneksi lambat, dll...", color = textSecondary) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Teknisi TextField
                    OutlinedTextField(
                        value = teknisiName,
                        onValueChange = { teknisiName = it },
                        label = { Text("Teknisi", color = textSecondary) },
                        placeholder = { Text("Nama teknisi...", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Biaya TextField
                    OutlinedTextField(
                        value = biayaInput,
                        onValueChange = { biayaInput = it },
                        label = { Text("Biaya (Rp)", color = textSecondary) },
                        placeholder = { Text("Contoh: 50000", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Reporter (Read-only real name of current admin)
                    OutlinedTextField(
                        value = adminRealName,
                        onValueChange = {},
                        label = { Text("Pelapor (Admin)", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textSecondary,
                            unfocusedTextColor = textSecondary,
                            focusedBorderColor = cardBorder,
                            unfocusedBorderColor = cardBorder,
                            disabledTextColor = textSecondary,
                            disabledBorderColor = cardBorder,
                            disabledLabelColor = textSecondary
                        ),
                        enabled = false,
                        readOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (complaintDescription.isBlank()) {
                            Toast.makeText(context, "Detail gangguan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedAreaName.isBlank()) {
                            Toast.makeText(context, "Silakan pilih area terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val biayaDouble = biayaInput.toDoubleOrNull() ?: 0.0
                        
                        coroutineScope.launch {
                            // Insert to local room db
                            db.gangguanDao().insertGangguan(
                                GangguanEntity(
                                    customerName = selectedAreaName,
                                    description = complaintDescription,
                                    status = "OTW",
                                    date = today,
                                    reporter = adminRealName,
                                    teknisi = teknisiName,
                                    biaya = biayaDouble,
                                    resolverAdmin = null
                                )
                            )

                            // Automatically post to expense database (pembukuan) if biaya > 0
                            if (biayaDouble > 0.0) {
                                try {
                                    ApiClient.apiService.addPembukuan(
                                        PembukuanRequest(
                                            type = "pengeluaran",
                                            category = "Operasional",
                                            amount = biayaDouble,
                                            description = "Biaya Gangguan: Area $selectedAreaName - $complaintDescription (Teknisi: $teknisiName)"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("GangguanScreen", "Gagal menyimpan pengeluaran otomatis", e)
                                }
                            }

                            Toast.makeText(context, "Laporan gangguan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBg,
                        contentColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White
                    )
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    // Edit Gangguan Dialog
    editingGangguan?.let { item ->
        var areaDropdownExpanded by remember { mutableStateOf(false) }
        var statusDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingGangguan = null },
            containerColor = cardBg,
            title = { Text("Edit Laporan Gangguan", color = textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pilihan Area Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedAreaName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Area", color = textSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { areaDropdownExpanded = !areaDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { areaDropdownExpanded = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain,
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                disabledBorderColor = cardBorder,
                                disabledTextColor = textMain
                            ),
                            enabled = false
                        )

                        DropdownMenu(
                            expanded = areaDropdownExpanded,
                            onDismissRequest = { areaDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(cardBg)
                        ) {
                            listArea.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area.name, color = textMain) },
                                    onClick = {
                                        selectedAreaName = area.name
                                        areaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Detail Gangguan (Mandatory)
                    OutlinedTextField(
                        value = complaintDescription,
                        onValueChange = { complaintDescription = it },
                        label = { Text("Detail Gangguan", color = textSecondary) },
                        placeholder = { Text("LOS merah, koneksi lambat, dll...", color = textSecondary) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Teknisi TextField
                    OutlinedTextField(
                        value = teknisiName,
                        onValueChange = { teknisiName = it },
                        label = { Text("Teknisi", color = textSecondary) },
                        placeholder = { Text("Nama teknisi...", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Biaya TextField
                    OutlinedTextField(
                        value = biayaInput,
                        onValueChange = { biayaInput = it },
                        label = { Text("Biaya (Rp)", color = textSecondary) },
                        placeholder = { Text("Contoh: 50000", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    // Status Dropdown / Choice
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (statusChoice == "OTW") "OTW (Dalam Proses)" else "Selesai",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status Gangguan", color = textSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { statusDropdownExpanded = !statusDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusDropdownExpanded = true },
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

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(cardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("OTW (Dalam Proses)", color = textMain) },
                                onClick = {
                                    statusChoice = "OTW"
                                    statusDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Selesai", color = textMain) },
                                onClick = {
                                    statusChoice = "SELESAI"
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Reporter (Read-only original name of admin)
                    OutlinedTextField(
                        value = reporterName,
                        onValueChange = {},
                        label = { Text("Pelapor (Admin)", color = textSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textSecondary,
                            unfocusedTextColor = textSecondary,
                            focusedBorderColor = cardBorder,
                            unfocusedBorderColor = cardBorder,
                            disabledTextColor = textSecondary,
                            disabledBorderColor = cardBorder
                        ),
                        enabled = false,
                        readOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (complaintDescription.isBlank()) {
                            Toast.makeText(context, "Detail gangguan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedAreaName.isBlank()) {
                            Toast.makeText(context, "Silakan pilih area terlebih dahulu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val biayaDouble = biayaInput.toDoubleOrNull() ?: 0.0
                        val costChanged = biayaDouble != item.biaya

                        // Determine resolverAdmin
                        val finalResolverAdmin = if (statusChoice == "SELESAI") {
                            // If it was already resolved, keep the original resolver or set to current logged-in admin
                            item.resolverAdmin ?: adminRealName
                        } else {
                            null
                        }

                        coroutineScope.launch {
                            // Update local room db
                            db.gangguanDao().insertGangguan(
                                GangguanEntity(
                                    id = item.id,
                                    customerName = selectedAreaName,
                                    description = complaintDescription,
                                    status = statusChoice,
                                    date = item.date, // keep original date
                                    reporter = item.reporter, // keep original reporter
                                    teknisi = teknisiName,
                                    biaya = biayaDouble,
                                    resolverAdmin = finalResolverAdmin
                                )
                            )

                            // Automatically post to expense database (pembukuan) if biaya has changed and is positive
                            if (costChanged && biayaDouble > 0.0) {
                                try {
                                    ApiClient.apiService.addPembukuan(
                                        PembukuanRequest(
                                            type = "pengeluaran",
                                            category = "Operasional",
                                            amount = biayaDouble,
                                            description = "Penyesuaian Biaya Gangguan: Area $selectedAreaName - $complaintDescription (Teknisi: $teknisiName)"
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("GangguanScreen", "Gagal menyimpan pengeluaran otomatis", e)
                                }
                            }

                            Toast.makeText(context, "Laporan gangguan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            editingGangguan = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBg,
                        contentColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White
                    )
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingGangguan = null }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}

private object BoxDefaults {
    val borderStroke: androidx.compose.foundation.BorderStroke
        @Composable
        get() = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)
        )
}
