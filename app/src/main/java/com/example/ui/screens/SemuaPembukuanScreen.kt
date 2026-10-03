package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.example.ui.data.remote.ApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PembukuanListItem(
    item: com.example.ui.data.remote.PembukuanItem,
    bgMain: Color,
    textMain: Color,
    textSecondary: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgMain),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description ?: "-",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.type.uppercase()} - ${item.category ?: "-"}",
                    color = textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val color = if (item.type.lowercase() == "pemasukan") Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    text = "Rp. ${String.format("%,d", item.amount.toLong()).replace(",", ".")}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemuaPembukuanScreen(initialType: String = "Pilih Tipe Pembukuan", onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val primaryBlue = Color(0xFF2196F3)
    val warningYellow = Color(0xFFFF9800)
    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFF44336)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)

    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }
    val currentYear = remember { LocalDate.now().year }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var selectedMonth by remember { mutableStateOf(currentMonthYear) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    val months = remember {
        val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val list = mutableListOf("Semua Waktu")
        for (m in monthNames) {
            list.add("$m $currentYear")
        }
        list
    }

    // Tab Index: 0 -> Pemasukan, 1 -> Pengeluaran, 2 -> Semua
    var selectedTab by remember {
        mutableIntStateOf(
            when (initialType.lowercase()) {
                "pengeluaran" -> 1
                "pemasukan" -> 0
                else -> 0
            }
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var keterangan by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var tipePembukuan by remember { mutableStateOf(if (initialType.isNotEmpty()) initialType else "Pilih Tipe Pembukuan") }
    var tipeDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Pilih Tipe Pembukuan", "Pemasukan", "Pengeluaran", "Setor")
    
    var pembukuanList by remember { mutableStateOf(emptyList<com.example.ui.data.remote.PembukuanItem>()) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var editingItem by remember { mutableStateOf<com.example.ui.data.remote.PembukuanItem?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    fun refreshData() {
        coroutineScope.launch {
            try {
                isBackgroundLoading = true
                pembukuanList = ApiClient.apiService.getAllPembukuan(month = if (selectedMonth == "Semua Waktu") null else selectedMonth)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isBackgroundLoading = false
            }
        }
    }
    
    LaunchedEffect(selectedMonth) {
        refreshData()
    }

    fun isItemInMonth(item: com.example.ui.data.remote.PembukuanItem, filter: String): Boolean {
        if (filter == "Semua Waktu") return true
        val createdAt = item.created_at ?: return false
        val parts = filter.split(" ")
        if (parts.size < 2) return true
        val mName = parts[0]
        val yStr = parts[1]
        val monthNames = listOf("januari", "februari", "maret", "april", "mei", "juni", "juli", "agustus", "september", "oktober", "november", "desember")
        val mIdx = monthNames.indexOf(mName.lowercase())
        if (mIdx < 0) return true
        val mNum = String.format("%02d", mIdx + 1)
        return createdAt.startsWith("$yStr-$mNum")
    }

    val monthFilteredList = remember(pembukuanList, selectedMonth) {
        pembukuanList.filter { isItemInMonth(it, selectedMonth) }
    }
    val totalPemasukanBulan = remember(monthFilteredList) {
        monthFilteredList.filter { it.type.equals("pemasukan", ignoreCase = true) }.sumOf { it.amount.toLong() }
    }
    val totalPengeluaranBulan = remember(monthFilteredList) {
        monthFilteredList.filter { it.type.equals("pengeluaran", ignoreCase = true) }.sumOf { it.amount.toLong() }
    }
    val totalSetorBulan = remember(monthFilteredList) {
        monthFilteredList.filter { it.type.equals("setor", ignoreCase = true) }.sumOf { it.amount.toLong() }
    }

    val currentTabItems = remember(monthFilteredList, selectedTab, searchQuery) {
        val byType = when (selectedTab) {
            0 -> monthFilteredList.filter { it.type.equals("pemasukan", ignoreCase = true) }
            1 -> monthFilteredList.filter { it.type.equals("pengeluaran", ignoreCase = true) }
            else -> monthFilteredList
        }
        if (searchQuery.isBlank()) byType
        else byType.filter {
            it.description?.contains(searchQuery, ignoreCase = true) == true ||
            it.category?.contains(searchQuery, ignoreCase = true) == true ||
            it.type.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            Column(modifier = Modifier.background(if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF))) {
                TopAppBar(
                    title = { Text("Semua Pembukuan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bulan Pembukuan :", color = textSecondary, fontSize = 12.sp)
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { monthDropdownExpanded = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    selectedMonth,
                                    color = warningYellow,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = warningYellow)
                            }
                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false },
                                containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                            ) {
                                months.forEach { month ->
                                    DropdownMenuItem(
                                        text = { Text(month, color = textMain) },
                                        onClick = {
                                            selectedMonth = month
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { 
                            editingItem = null
                            keterangan = ""
                            jumlah = ""
                            tipePembukuan = when (selectedTab) {
                                0 -> "Pemasukan"
                                1 -> "Pengeluaran"
                                else -> "Pilih Tipe Pembukuan"
                            }
                            showAddDialog = true 
                        },
                        containerColor = if (selectedTab == 0) successGreen else if (selectedTab == 1) errorRed else primaryBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }

                // TabRow Pemisah Pemasukan dan Pengeluaran
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF151522) else androidx.compose.ui.graphics.Color(0xFFF0F4F8),
                    contentColor = primaryBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 0) successGreen else if (selectedTab == 1) errorRed else primaryBlue,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Pemasukan",
                                    color = if (selectedTab == 0) successGreen else textSecondary,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedTab == 0) successGreen.copy(alpha = 0.2f) else textSecondary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "${monthFilteredList.count { it.type.equals("pemasukan", ignoreCase = true) }}",
                                        color = if (selectedTab == 0) successGreen else textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Pengeluaran",
                                    color = if (selectedTab == 1) errorRed else textSecondary,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedTab == 1) errorRed.copy(alpha = 0.2f) else textSecondary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "${monthFilteredList.count { it.type.equals("pengeluaran", ignoreCase = true) }}",
                                        color = if (selectedTab == 1) errorRed else textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Semua",
                                    color = if (selectedTab == 2) primaryBlue else textSecondary,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedTab == 2) primaryBlue.copy(alpha = 0.2f) else textSecondary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "${monthFilteredList.size}",
                                        color = if (selectedTab == 2) primaryBlue else textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pemasukan :", color = textSecondary, fontSize = 13.sp)
                    Text(
                        "Rp. ${String.format("%,d", totalPemasukanBulan).replace(",", ".")}",
                        color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pengeluaran :", color = textSecondary, fontSize = 13.sp)
                    Text(
                        "Rp. ${String.format("%,d", totalPengeluaranBulan).replace(",", ".")}",
                        color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Divider(color = textSecondary.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sisa Saldo Kas Bulan Ini :", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    val sisaSaldo = totalPemasukanBulan - totalPengeluaranBulan
                    Text(
                        "Rp. ${String.format("%,d", sisaSaldo).replace(",", ".")}",
                        color = if (sisaSaldo >= 0) successGreen else errorRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Summary Banner khusus tab aktif
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedTab) {
                        0 -> successGreen.copy(alpha = 0.12f)
                        1 -> errorRed.copy(alpha = 0.12f)
                        else -> primaryBlue.copy(alpha = 0.12f)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            when (selectedTab) {
                                0 -> "Total Pemasukan ($selectedMonth)"
                                1 -> "Total Pengeluaran ($selectedMonth)"
                                else -> "Ringkasan Pembukuan ($selectedMonth)"
                            },
                            color = textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            when (selectedTab) {
                                0 -> "Rp. ${String.format("%,d", totalPemasukanBulan).replace(",", ".")}"
                                1 -> "Rp. ${String.format("%,d", totalPengeluaranBulan).replace(",", ".")}"
                                else -> "Rp. ${String.format("%,d", (totalPemasukanBulan - totalPengeluaranBulan)).replace(",", ".")}"
                            },
                            color = when (selectedTab) {
                                0 -> successGreen
                                1 -> errorRed
                                else -> primaryBlue
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        when (selectedTab) {
                            0 -> "${monthFilteredList.count { it.type.equals("pemasukan", ignoreCase = true) }} transaksi"
                            1 -> "${monthFilteredList.count { it.type.equals("pengeluaran", ignoreCase = true) }} pengeluaran"
                            else -> "${monthFilteredList.size} total"
                        },
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari keterangan atau kategori...", color = textSecondary) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = textSecondary,
                        unfocusedIndicatorColor = textSecondary,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    ),
                    singleLine = true
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = primaryBlue
                )
            }
            
            if (!isBackgroundLoading && currentTabItems.isEmpty()) {
                val emptyMessage = when (selectedTab) {
                    0 -> "Belum ada transaksi Pemasukan pada bulan $selectedMonth.\n(Mulai dari Rp 0 setiap awal bulan)"
                    1 -> "Belum ada transaksi Pengeluaran pada bulan $selectedMonth.\n(Mulai dari Rp 0 setiap awal bulan)"
                    else -> "Belum ada catatan pembukuan pada bulan $selectedMonth."
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = emptyMessage,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentTabItems) { item ->
                        PembukuanListItem(
                            item = item,
                            bgMain = bgMain,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            onEdit = {
                                editingItem = item
                                tipePembukuan = when (item.type.lowercase()) {
                                    "pemasukan" -> "Pemasukan"
                                    "pengeluaran" -> "Pengeluaran"
                                    "setor" -> "Setor"
                                    else -> "Pilih Tipe Pembukuan"
                                }
                                keterangan = item.description ?: ""
                                jumlah = item.amount.toLong().toString()
                                showAddDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    try {
                                        ApiClient.apiService.deletePembukuan(item.id)
                                        refreshData()
                                        android.widget.Toast.makeText(context, "Data berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Gagal menghapus data", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingItem = null; keterangan = ""; jumlah = ""; tipePembukuan = "Pilih Tipe Pembukuan" },
            containerColor = bgMain,
            title = {
                Text(
                    "Transaksi",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Bulan Input", color = textSecondary, fontSize = 12.sp)
                        Text("(Ini berdasarkan pada pemilihan bulan di beranda)", color = textSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedMonth, color = primaryBlue, fontSize = 14.sp)
                    }

                    TextField(
                        value = keterangan,
                        onValueChange = { keterangan = it },
                        label = { Text("Keterangan", color = textSecondary) },
                        placeholder = { Text("Keterangan...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    TextField(
                        value = jumlah,
                        onValueChange = { jumlah = it },
                        label = { Text("Jumlah", color = textSecondary) },
                        placeholder = { Text("Jumlah", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    Column {
                        Text("Type", color = textSecondary, fontSize = 12.sp)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { tipeDropdownExpanded = true }.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tipePembukuan, color = textMain, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                            }
                            DropdownMenu(
                                expanded = tipeDropdownExpanded,
                                onDismissRequest = { tipeDropdownExpanded = false },
                                containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                tipeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = textMain) },
                                        onClick = {
                                            tipePembukuan = option
                                            tipeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { 
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val amountLong = jumlah.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                                    val amountDouble = amountLong.toDouble()
                                    var type = "pemasukan"
                                    var category = editingItem?.category ?: "Lain-lain"
                                    
                                    if (tipePembukuan == "Pemasukan") {
                                        type = "pemasukan"
                                        if (editingItem == null) category = "Pemasukkan Lain2"
                                    } else if (tipePembukuan == "Pengeluaran") {
                                        type = "pengeluaran"
                                        if (editingItem == null) category = "Lain-lain"
                                    } else if (tipePembukuan == "Setor") {
                                        type = "setor"
                                        if (editingItem == null) category = "Lain-lain"
                                    }
                                    
                                    val req = com.example.ui.data.remote.PembukuanRequest(
                                        type, category, amountDouble, keterangan
                                    )
                                    if (editingItem != null) {
                                        ApiClient.apiService.updatePembukuan(editingItem!!.id, req)
                                    } else {
                                        ApiClient.apiService.addPembukuan(req)
                                    }
                                    refreshData()
                                    showAddDialog = false
                                    editingItem = null
                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "Gagal menyimpan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF)),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "MENYIMPAN..." else "SIMPAN", color = textMain, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
