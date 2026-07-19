package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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

    var selectedMonth by remember { mutableStateOf("Semua Waktu") }
    var isLoading by remember { mutableStateOf(true) }
    var pembukuanData by remember { mutableStateOf<com.example.ui.data.remote.PembukuanResponse?>(null) }
    LaunchedEffect(Unit) {
        try {
            pembukuanData = com.example.ui.data.remote.ApiClient.apiService.getPembukuan()
        } catch(e: Exception) {}
        finally { isLoading = false }
    }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    val months = listOf("Semua Waktu")

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Rangkuman", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Rangkuman Pembukuan",
                color = textMain,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable { monthDropdownExpanded = true }
                        .padding(4.dp)
                ) {
                    Text(
                        selectedMonth,
                        color = primaryCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = primaryCyan,
                        modifier = Modifier.size(20.dp)
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pembayaran Tunai", color = textSecondary, fontSize = 14.sp)
                        Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.pemasukan?.toLong() ?: 0L)).replace(",", ".")}", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pemasukkan Lain-lain", color = textSecondary, fontSize = 14.sp)
                        Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.categories?.get("Lain-lain")?.toLong() ?: 0L)).replace(",", ".")}", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pengeluaran", color = neonRed, fontSize = 14.sp)
                        Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.pengeluaran?.toLong() ?: 0L)).replace(",", ".")}", color = neonRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Divider(color = Color(0xFF333333), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", color = textSecondary, fontSize = 14.sp)
                        Text(if(isLoading) "..." else "Rp. ${String.format("%,d", ((pembukuanData?.pemasukan ?: 0.0) - (pembukuanData?.pengeluaran ?: 0.0)).toLong()).replace(",", ".")}", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Uang Belum di Setor", color = textSecondary, fontSize = 14.sp)
                        Text("Rp. 12.100.000", color = neonYellow, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sudah di Setor", color = textSecondary, fontSize = 14.sp)
                        Text("Rp. 0", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Catatan : Untuk mengganti Tahun silahkan ganti tahun dari Halaman Beranda :v",
                color = textSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
