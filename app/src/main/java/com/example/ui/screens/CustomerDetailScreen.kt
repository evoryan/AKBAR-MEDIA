package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.OdpItem
import com.example.ui.data.remote.PaymentRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: String, onBack: () -> Unit, onNavigateToPayment: (String) -> Unit, onNavigateToAcs: (String) -> Unit = {}) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryBlue = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0F0F2A) else androidx.compose.ui.graphics.Color(0xFFE3F2FD)
    val lightBlue = Color(0xFF00FFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val neonPink = Color(0xFFFF00FF)
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var customer by remember { mutableStateOf<Customer?>(null) }
    var odpItem by remember { mutableStateOf<OdpItem?>(null) }
    var mikrotikStatus by remember { mutableStateOf<String>("-") }
    var mikrotikUptime by remember { mutableStateOf<String>("") }
    var acsDevice by remember { mutableStateOf<com.example.ui.screens.AcsDevice?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    var paymentHistory by remember { mutableStateOf<List<com.example.ui.data.remote.PaymentHistory>>(emptyList()) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var isPaying by remember { mutableStateOf(false) }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            val c = custs.find { it.id == customerId }
            customer = c
            
            
            if (c != null) {
                try {
                    paymentHistory = ApiClient.apiService.getCustomerHistory(c.id)
                } catch(e: Exception) {}

                // Get ODP if exist
                if (!c.odpId.isNullOrEmpty()) {
                    val odps = ApiClient.apiService.getOdpList()
                    odpItem = odps.find { it.id.toString() == c.odpId }
                }
                
                // Get ACS Device
                if (!c.pppoeSecret.isNullOrEmpty()) {
                    try {
                        val acsList = ApiClient.apiService.getAcsDevices()
                        acsDevice = acsList.find { it.username.trim().equals(c.pppoeSecret?.trim(), ignoreCase = true) }
                    } catch (e: Exception) {}
                    
                    // Get Mikrotik Secret Status
                    if (c.area.isNotEmpty()) {
                        val areas = ApiClient.apiService.getAreas()
                        val area = areas.find { it.name == c.area }
                        if (area != null) {
                            val secrets = ApiClient.apiService.getMikrotikSecrets(area.id)
                            val sec = secrets.find { it.name == c.pppoeSecret }
                            if (sec != null) {
                                mikrotikStatus = sec.status
                                mikrotikUptime = sec.uptime
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (showPaymentDialog && customer != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Konfirmasi Pembayaran") },
            text = { Text("Apakah Anda yakin ingin membayar tagihan untuk pelanggan ${customer?.name} sejumlah ${customer?.price}?") },
            confirmButton = {
                Button(
                    onClick = {
                        isPaying = true
                        coroutineScope.launch {
                            try {
                                val amountStr = customer?.price?.replace(Regex("[^0-9]"), "") ?: "0"
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                ApiClient.apiService.payBilling(PaymentRequest(customerId, "Admin", amount))
                                Toast.makeText(context, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
                                showPaymentDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal melakukan pembayaran: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isPaying = false
                            }
                        }
                    },
                    enabled = !isPaying
                ) {
                    if (isPaying) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    else Text("Ya, Bayar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }, enabled = !isPaying) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { onNavigateToPayment(customerId) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BAYAR TAGIHAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = neonCyan)
            }
        } else if (customer == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Pelanggan tidak ditemukan", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgMain)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Section (Profile)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(primaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            customer?.name?.take(1)?.uppercase() ?: "P",
                            color = neonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(customer?.name ?: "Nama Pelanggan", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(customer?.phone ?: "-", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = headerBg,
                    contentColor = textMain,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = neonCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Rincian", color = if (selectedTabIndex == 0) neonCyan else textSecondary) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Riwayat", color = if (selectedTabIndex == 1) neonCyan else textSecondary) }
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedTabIndex == 0) {
                        
                        // Rincian Jaringan
                        CardSection(title = "Rincian Jaringan", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Mikrotik Secret", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Sumber: ${customer?.area}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                    Text("User: ${customer?.pppoeSecret?.takeIf { it.isNotBlank() } ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (mikrotikStatus.equals("online", true)) Color(0xFF00FF00).copy(alpha = 0.2f) else if (mikrotikStatus.equals("disabled", true)) Color.Gray.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(mikrotikStatus.uppercase(), color = if (mikrotikStatus.equals("online", true)) Color.Green else if (mikrotikStatus.equals("disabled", true)) Color.LightGray else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("ODP", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Nama: ${odpItem?.name ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                    Text("Port: ${customer?.odpPort?.takeIf { it.isNotBlank() } ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                            
                            Button(
                                onClick = {
                                    if (!customer?.pppoeSecret.isNullOrBlank()) {
                                        onNavigateToAcs(customer!!.pppoeSecret!!)
                                    } else {
                                        Toast.makeText(context, "Data ACS tidak ditemukan untuk username ini", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = neonCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cek Modem (ACS)")
                            }
                        }

                        // Rincian Data
                        CardSection(title = "Rincian Data", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            DetailRow("Nama", customer?.name ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("No Telepon", customer?.phone ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Area", customer?.area ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Tgl Registrasi", customer?.registerDate?.takeIf { it.isNotBlank() } ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Tgl Isolir", customer?.isolateDate?.takeIf { it.isNotBlank() } ?: "-")
                        }

                        // Rincian Biaya
                        CardSection(title = "Rincian Biaya", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            DetailRow("Paket", customer?.packageName?.takeIf { it.isNotBlank() } ?: "-")
                            DetailRow("Harga Paket", customer?.price ?: "-")
                            if (!customer?.discount.isNullOrEmpty() && customer?.discount != "0") {
                                DetailRow("Diskon", customer?.discount ?: "-")
                            }
                            if (!customer?.additionalCost1.isNullOrEmpty() && customer?.additionalCost1 != "0") {
                                DetailRow("Biaya Tambahan 1", "Rp. ${customer?.additionalCost1}")
                            }
                            if (!customer?.additionalCost2.isNullOrEmpty() && customer?.additionalCost2 != "0") {
                                DetailRow("Biaya Tambahan 2", "Rp. ${customer?.additionalCost2}")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Biaya Perbulan", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 14.sp)
                                Text(customer?.price ?: "Rp. 0", color = lightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                    } else if (selectedTabIndex == 1) {
                        PaymentHistorySection(paymentHistory, cardBg = cardBg, cardBorder = cardBorder, textMain = textMain, textSecondary = textSecondary, neonCyan = neonCyan, neonPink = neonPink)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
        Text(value, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentHistorySection(history: List<com.example.ui.data.remote.PaymentHistory>, cardBg: Color, cardBorder: Color, textMain: Color, textSecondary: Color, neonCyan: Color, neonPink: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Riwayat Transaksi", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        if (history.isEmpty()) {
            Text("Tidak ada riwayat transaksi", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 14.sp)
        } else {
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
                            val date = item.createdAt?.substringBefore("T") ?: ""
                            Text(date, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.description, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rp. ${item.amount}", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lunas", color = neonPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
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
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
