package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: String, onBack: () -> Unit) {
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val primaryBlue = Color(0xFF0F0F2A)
    val lightBlue = Color(0xFF00FFFF)
    val neonCyan = Color(0xFF00FFFF)
    val neonPink = Color(0xFFFF00FF)
    val warningOrange = Color(0xFFFF9900)
    val errorRed = Color(0xFFFF003C)
    
    // Using fake data based on customerId for demonstration
    val customerName = if (customerId == "42") "Adit" else "Pelanggan"
    val customerPhone = "081753951426"

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Image, contentDescription = "Image", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerBg
                )
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BAYAR TAGIHAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Section (Dark Purple)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryPurple)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Tambahkan Foto Rumah Mungkin ?",
                    color = textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(customerName, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = lightBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(customerPhone, color = lightBlue, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID pel : ", color = textSecondary, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(lightBlue)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(customerId, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // Usage Stats
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = textMain, modifier = Modifier.size(20.dp))
                        Text("0 kb / 0 kb", color = textMain, fontSize = 10.sp)
                        Text("0 Gb", color = textMain, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
            ) {
                val tabs = listOf("DETAIL", "BAYAR")
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = headerBg,
                    contentColor = textMain,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = neonCyan
                        )
                    },
                    divider = { }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (selectedTabIndex == index) neonCyan else textSecondary) }
                        )
                    }
                }
            }

            // Content Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // Warning Card
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(warningOrange)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Oops...!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("1 Bulan Tagihan Belum di terbayarkan", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(lightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tagihan", color = lightBlue, fontSize = 10.sp)
                    }
                }

                // Rincian Jaringan
                CardSection(title = "Rincian Jaringan", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                    // Mikrotik Secret
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgMain)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Router, contentDescription = null, tint = lightBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mikrotik Secret", color = lightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mikrotik : Talun", color = textMain, fontSize = 12.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Status Koneksi : $customerName", color = textMain, fontSize = 12.sp)
                                Text("Nonaktif", color = errorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Jalur Jaringan", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Digunakan untuk melihat jalur atas dan bawah pelanggan", color = textSecondary, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = lightBlue)
                    }
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text("PPOE / Static - Modem", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("$customerName - null", color = textSecondary, fontSize = 12.sp)
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text("ODP", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("- Nomor ODP 1111-Darmadi", color = textSecondary, fontSize = 12.sp)
                            Text("- Nomor Kabel 8", color = textSecondary, fontSize = 12.sp)
                            Text("- Nomor Port 8", color = textSecondary, fontSize = 12.sp)
                            Text("- Teknisi Pemasang null", color = textSecondary, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                border = BorderStroke(1.dp, textSecondary)
                            ) {
                                Text("EDIT", color = textMain, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { /*TODO*/ },
                                modifier = Modifier.size(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(1.dp, textSecondary)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = "View", tint = textMain, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Akses Modem", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(errorRed)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("CUSTOM+ UP", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CEK MODEM", fontWeight = FontWeight.Bold)
                    }
                }

                // Rincian Data
                CardSection(title = "Rincian Data", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                    Text("Tanggal Daftar", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("15 Nov 2025", color = textSecondary, fontSize = 12.sp)
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Tagihan Setiap Tanggal", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("10", color = textSecondary, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(errorRed)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tanggal Isolir", color = Color.White, fontSize = 10.sp)
                                Text("10", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Alamat", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Talun", color = textSecondary, fontSize = 12.sp)
                        }
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(primaryBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Lokasi", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Klik untuk membuka Lokasi", color = textSecondary, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonPinCircle, contentDescription = null, tint = lightBlue)
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text("Password (untuk aplikasi Pelanggan My WIFI)", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Kirim Password Pelanggan", color = textSecondary, fontSize = 12.sp)
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = lightBlue)
                    }
                }
                
                // Rincian Biaya
                CardSection(title = "Rincian Biaya", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Reguler", color = textSecondary, fontSize = 12.sp)
                        Text("Rp. 100.000", color = textSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PPN 0%", color = textSecondary, fontSize = 12.sp)
                        Text("Rp. 0", color = textSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Biaya Perbulan", color = textMain, fontSize = 14.sp)
                        Text("Rp. 100.000", color = lightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tambahkan Biaya Tambahan", color = lightBlue, fontSize = 12.sp)
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = lightBlue, modifier = Modifier.size(16.dp))
                    }
                }
                
                // Catatan
                CardSection(title = "Catatan", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tidak ada Catatan", color = textSecondary, fontSize = 12.sp)
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = lightBlue, modifier = Modifier.size(16.dp))
                    }
                }
                
                // KTP
                CardSection(title = "KTP", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tidak ada KTP", color = textSecondary, fontSize = 12.sp)
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = lightBlue, modifier = Modifier.size(16.dp))
                    }
                }
                } else if (selectedTabIndex == 1) {
                    PaymentHistorySection(cardBg = cardBg, cardBorder = cardBorder, textMain = textMain, textSecondary = textSecondary, neonCyan = neonCyan, neonPink = neonPink)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentHistorySection(cardBg: Color, cardBorder: Color, textMain: Color, textSecondary: Color, neonCyan: Color, neonPink: Color) {
    val history = listOf(
        mapOf("date" to "10 Jun 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Transfer Bank"),
        mapOf("date" to "10 May 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Cash"),
        mapOf("date" to "10 Apr 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Transfer Bank")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Riwayat Transaksi", color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        history.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(item["date"] ?: "", color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item["method"] ?: "", color = textSecondary, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(item["amount"] ?: "", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item["status"] ?: "", color = neonPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun CardSection(title: String, cardBg: Color, cardBorder: Color, textMain: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
