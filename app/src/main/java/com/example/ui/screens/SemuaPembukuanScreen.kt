package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.PembukuanItem
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatInvoiceUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    if (url.startsWith("http://") || url.startsWith("https://")) return url
    val baseUrl = com.example.ui.data.SettingsManager.apiBaseUrl.removeSuffix("/")
    val path = if (url.startsWith("/")) url else "/$url"
    return "$baseUrl$path"
}

@Composable
fun PembukuanListItem(
    item: PembukuanItem,
    bgMain: Color,
    textMain: Color,
    textSecondary: Color,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgMain),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description ?: "-",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${item.type.uppercase()} • ${item.category ?: "-"}",
                    color = textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    color = textSecondary,
                    fontSize = 11.sp
                )
                
                if (!item.image_url.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFF03A9F4).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Ada Foto Invoice",
                                color = Color(0xFF0288D1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val color = if (item.type.lowercase() == "pemasukan") Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    text = "Rp. ${String.format("%,d", item.amount.toLong()).replace(",", ".")}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemuaPembukuanScreen(initialType: String = "Pilih Tipe Pembukuan", onBack: () -> Unit) {
    val context = LocalContext.current
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val primaryBlue = Color(0xFF2196F3)
    val warningYellow = Color(0xFFFF9800)
    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFF44336)
    val headerBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF1F0216) else Color(0xFFFFEBF5)

    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }
    val currentYear = remember { LocalDate.now().year }
    
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
    var detailItem by remember { mutableStateOf<PembukuanItem?>(null) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }

    var keterangan by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var tipePembukuan by remember { mutableStateOf(if (initialType.isNotEmpty()) initialType else "Pilih Tipe Pembukuan") }
    var tipeDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Pilih Tipe Pembukuan", "Pemasukan", "Pengeluaran", "Setor")

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    var pembukuanList by remember { mutableStateOf(emptyList<PembukuanItem>()) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var editingItem by remember { mutableStateOf<PembukuanItem?>(null) }
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

    fun isItemInMonth(item: PembukuanItem, filter: String): Boolean {
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
            Column(modifier = Modifier.background(if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF))) {
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
                                containerColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
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
                            selectedImageUri = null
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
                    containerColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF151522) else Color(0xFFF0F4F8),
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
                    .background(if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF))
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
                HorizontalDivider(color = textSecondary.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
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
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentTabItems) { item ->
                        PembukuanListItem(
                            item = item,
                            bgMain = cardBg,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            onClick = {
                                detailItem = item
                            },
                            onEdit = {
                                editingItem = item
                                selectedImageUri = null
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

    // DIALOG: Detail Transaksi / Pengeluaran
    if (detailItem != null) {
        val item = detailItem!!
        val fullImgUrl = formatInvoiceUrl(item.image_url)
        val isPengeluaran = item.type.lowercase() == "pengeluaran"

        AlertDialog(
            onDismissRequest = { detailItem = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPengeluaran) Icons.Default.ReceiptLong else Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isPengeluaran) errorRed else successGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isPengeluaran) "Detail Pengeluaran" else "Detail Transaksi",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nominal
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPengeluaran) errorRed.copy(alpha = 0.1f) else successGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nominal ${item.type.uppercase()}", color = textSecondary, fontSize = 11.sp)
                            Text(
                                "Rp. ${String.format("%,d", item.amount.toLong()).replace(",", ".")}",
                                color = if (isPengeluaran) errorRed else successGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Metadata
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kategori:", color = textSecondary, fontSize = 12.sp)
                            Text(item.category ?: "-", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Keterangan:", color = textSecondary, fontSize = 12.sp)
                            Text(item.description ?: "-", color = textMain, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Admin Input:", color = textSecondary, fontSize = 12.sp)
                            Text(item.admin_name ?: "Admin", color = textMain, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Waktu Catat:", color = textSecondary, fontSize = 12.sp)
                            Text(item.created_at?.take(19)?.replace("T", " ") ?: "-", color = textMain, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = textSecondary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    // Gambar Foto Invoice
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Gambar / Foto Invoice Bukti:", color = textMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        if (!fullImgUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.1f))
                                    .clickable {
                                        fullScreenImageUrl = fullImgUrl
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = fullImgUrl,
                                    contentDescription = "Foto Invoice",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Klik Perbesar", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        } else {
                            Surface(
                                color = textSecondary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tidak ada foto invoice yang dilampirkan", color = textSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val curr = detailItem
                            detailItem = null
                            if (curr != null) {
                                editingItem = curr
                                selectedImageUri = null
                                tipePembukuan = when (curr.type.lowercase()) {
                                    "pemasukan" -> "Pemasukan"
                                    "pengeluaran" -> "Pengeluaran"
                                    "setor" -> "Setor"
                                    else -> "Pilih Tipe Pembukuan"
                                }
                                keterangan = curr.description ?: ""
                                jumlah = curr.amount.toLong().toString()
                                showAddDialog = true
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { detailItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        )
    }

    // DIALOG: Fullscreen View Image Invoice
    if (fullScreenImageUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenImageUrl,
                    contentDescription = "Invoice Fullscreen",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = { fullScreenImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    // DIALOG: Input / Edit Pembukuan
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingItem = null; selectedImageUri = null; keterangan = ""; jumlah = ""; tipePembukuan = "Pilih Tipe Pembukuan" },
            containerColor = cardBg,
            title = {
                Text(
                    if (editingItem != null) "Edit Transaksi" else "Tambah Transaksi",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Bulan Input", color = textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(selectedMonth, color = primaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = { keterangan = it },
                        label = { Text("Keterangan", color = textSecondary) },
                        placeholder = { Text("Keterangan transaksi...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = jumlah,
                        onValueChange = { jumlah = it },
                        label = { Text("Jumlah (Rp)", color = textSecondary) },
                        placeholder = { Text("Nominal angka...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    Column {
                        Text("Tipe Transaksi", color = textSecondary, fontSize = 12.sp)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(textSecondary.copy(alpha = 0.08f))
                                    .clickable { tipeDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tipePembukuan, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                            }
                            DropdownMenu(
                                expanded = tipeDropdownExpanded,
                                onDismissRequest = { tipeDropdownExpanded = false },
                                containerColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF),
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

                    // Input Gambar / Foto Invoice (Opsional)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Foto Invoice / Bukti (Opsional)", color = textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val existingImgUrl = formatInvoiceUrl(editingItem?.image_url)

                        if (selectedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "New Invoice Preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus Gambar Baru", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else if (!existingImgUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = existingImgUrl,
                                    contentDescription = "Existing Invoice Preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Black.copy(alpha = 0.6f), contentColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Ganti Foto", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryBlue)
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pilih Foto Invoice / Bukti", fontSize = 13.sp)
                            }
                        }
                    }
                    
                    Button(
                        onClick = { 
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    var imageUrl: String? = editingItem?.image_url

                                    val imgUri = selectedImageUri
                                    if (imgUri != null) {
                                        val bytes = com.example.ui.util.ImageCompressor.compressImageUri(context, imgUri)
                                        if (bytes != null) {
                                            val mimeType = "image/jpeg"
                                            val reqFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                                            val part = MultipartBody.Part.createFormData("image", "invoice_${System.currentTimeMillis()}.jpg", reqFile)
                                            val uploadRes = ApiClient.apiService.uploadInvoiceImage(part)
                                            imageUrl = uploadRes.url ?: uploadRes.image_url
                                        }
                                    }

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
                                        type, category, amountDouble, keterangan, image_url = imageUrl
                                    )
                                    if (editingItem != null) {
                                        ApiClient.apiService.updatePembukuan(editingItem!!.id, req)
                                    } else {
                                        ApiClient.apiService.addPembukuan(req)
                                    }
                                    refreshData()
                                    showAddDialog = false
                                    editingItem = null
                                    selectedImageUri = null
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
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "MENYIMPAN..." else "SIMPAN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; editingItem = null; selectedImageUri = null }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}
