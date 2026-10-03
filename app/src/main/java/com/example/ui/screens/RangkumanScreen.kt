package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangkumanScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    
    val primaryCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val neonGreen = Color(0xFF00FF4D)
    val neonRed = Color(0xFFFF003C)
    val neonYellow = Color(0xFFFFC107)

    val currentMonthYear = remember {
        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }
    val currentYear = remember { java.time.LocalDate.now().year }
    val months = remember {
        val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val list = mutableListOf<String>()
        for (m in monthNames) {
            list.add("$m $currentYear")
        }
        list.add("Semua Waktu")
        list
    }

    var selectedMonth by remember { mutableStateOf(currentMonthYear) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    var pembukuanData by remember { mutableStateOf<com.example.ui.data.remote.PembukuanResponse?>(null) }
    var totalUangBelumDiSetor by remember { mutableStateOf(0.0) }
    var totalSudahDiSetor by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()

    fun loadData(m: String) {
        coroutineScope.launch {
            try {
                isBackgroundLoading = true
                val queryMonth = if (m == "Semua Waktu") null else m
                pembukuanData = com.example.ui.data.remote.ApiClient.apiService.getPembukuan(month = queryMonth)
                val uangAdmin = try { com.example.ui.data.remote.ApiClient.apiService.getUangDiAdmin() } catch(e: Exception) { emptyList() }
                
                var sumSisa = 0.0
                var sumSetor = 0.0
                uangAdmin.forEach { item ->
                    val diterima = item.totalDiterima ?: 0.0
                    val setor = item.setor ?: 0.0
                    sumSisa += (diterima - setor)
                    sumSetor += setor
                }
                totalUangBelumDiSetor = sumSisa
                totalSudahDiSetor = sumSetor
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                isBackgroundLoading = false
            }
        }
    }

    LaunchedEffect(selectedMonth) {
        loadData(selectedMonth)
    }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Rangkuman Pembukuan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
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
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = primaryCyan
                )
            }
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        "Rangkuman Pembukuan Akhir Bulan",
                        color = textMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Laporan Arus Kas & Tutup Buku Bulanan",
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryCyan.copy(alpha = 0.12f))
                                .clickable { monthDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                tint = primaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                selectedMonth,
                                color = primaryCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = primaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false },
                            containerColor = cardBg
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
                
                // Card 1: Pemasukan
                item {
                    val pemasukanTotal = pembukuanData?.pemasukan ?: 0.0
                    val cashVal = pembukuanData?.categories?.get("Transaksi Cash") ?: 0.0
                    val lainVal = pembukuanData?.categories?.get("Pemasukkan Lain2") ?: (pembukuanData?.categories?.get("Lain-lain") ?: 0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pemasukan", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", pemasukanTotal.toLong()).replace(",", ".")}",
                                    color = neonGreen, fontSize = 17.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Divider(color = textSecondary.copy(alpha = 0.2f), thickness = 0.5.dp)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pembayaran Tagihan (Cash/Transfer)", color = textSecondary, fontSize = 13.sp)
                                Text(if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", cashVal.toLong()).replace(",", ".")}", color = textMain, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pemasukkan Lain-lain", color = textSecondary, fontSize = 13.sp)
                                Text(if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", lainVal.toLong()).replace(",", ".")}", color = textMain, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Card 2: Pengeluaran
                item {
                    val pengeluaranTotal = pembukuanData?.pengeluaran ?: 0.0
                    val categories = pembukuanData?.categories ?: emptyMap()
                    val pengeluaranKeys = listOf(
                        "Gaji Karyawan", "Pasang Baru", "Perbaikan Alat", "Bayar Bandwidth",
                        "Bayar Kang Tagih", "Listrik / PDAM / Pulsa", "Bayar Marketing", "Lain-lain"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pengeluaran", color = neonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", pengeluaranTotal.toLong()).replace(",", ".")}",
                                    color = neonRed, fontSize = 17.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Divider(color = textSecondary.copy(alpha = 0.2f), thickness = 0.5.dp)

                            pengeluaranKeys.forEach { cat ->
                                val valAmount = categories[cat] ?: 0.0
                                if (valAmount > 0.0 || !isBackgroundLoading) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, color = textSecondary, fontSize = 13.sp)
                                        Text("Rp. ${String.format("%,d", valAmount.toLong()).replace(",", ".")}", color = textMain, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Card 3: Laba Bersih
                item {
                    val labaBersih = ((pembukuanData?.pemasukan ?: 0.0) - (pembukuanData?.pengeluaran ?: 0.0)).toLong()
                    val isPositive = labaBersih >= 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isPositive) neonGreen.copy(alpha = 0.5f) else neonRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Laba Bersih / Sisa Kas Akhir Bulan",
                                color = textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", labaBersih).replace(",", ".")}",
                                color = if (isPositive) neonGreen else neonRed,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isPositive) "Arus kas surplus untuk bulan $selectedMonth" else "Arus kas defisit untuk bulan $selectedMonth",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                
                // Card 4: Setoran Admin
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, primaryCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Rekap Setoran Admin", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Divider(color = textSecondary.copy(alpha = 0.2f), thickness = 0.5.dp)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Uang Belum di Setor", color = textSecondary, fontSize = 13.sp)
                                Text(if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", totalUangBelumDiSetor.toLong()).replace(",", ".")}", color = neonYellow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sudah di Setor", color = textSecondary, fontSize = 13.sp)
                                Text(if (isBackgroundLoading) "..." else "Rp. ${String.format("%,d", totalSudahDiSetor.toLong()).replace(",", ".")}", color = neonGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "Catatan: Pembukuan dimulai dari Rp 0 setiap awal bulan. Rangkuman akhir bulan merekapitulasi seluruh mutasi keuangan bulan terpilih.",
                        color = textSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
