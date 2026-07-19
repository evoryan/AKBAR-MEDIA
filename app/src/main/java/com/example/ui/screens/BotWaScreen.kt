package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.compose.ui.text.SpanStyle
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotWaScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val successGreen = Color(0xFF28A745)
    val warningYellow = Color(0xFFFFC107)
    val warningBg = Color(0xFFFFF3CD)
    val infoBg = Color(0xFFD1ECF1)
    val infoText = Color(0xFF0C5460)
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    var waGatewayEnabled by remember { mutableStateOf(com.example.ui.data.SettingsManager.waGatewayEnabled) }
    var waNotifyNewBilling by remember { mutableStateOf(com.example.ui.data.SettingsManager.waNotifyNewBilling) }
    var waNotifyPaymentSuccess by remember { mutableStateOf(com.example.ui.data.SettingsManager.waNotifyPaymentSuccess) }
    var waNotifyIsolir by remember { mutableStateOf(com.example.ui.data.SettingsManager.waNotifyIsolir) }
    var waNotifyOtp by remember { mutableStateOf(com.example.ui.data.SettingsManager.waNotifyOtp) }

    var waTemplateNewBilling by remember { mutableStateOf(com.example.ui.data.SettingsManager.waTemplateNewBilling) }
    var waTemplatePaymentSuccess by remember { mutableStateOf(com.example.ui.data.SettingsManager.waTemplatePaymentSuccess) }
    var waTemplateIsolir by remember { mutableStateOf(com.example.ui.data.SettingsManager.waTemplateIsolir) }
    var waTemplateOtp by remember { mutableStateOf(com.example.ui.data.SettingsManager.waTemplateOtp) }

    var testDestination by remember { mutableStateOf("") }
    var testMessage by remember { mutableStateOf("") }
    var isTestSending by remember { mutableStateOf(false) }

    var isWaLinked by remember { mutableStateOf(false) }
    var qrSecondsLeft by remember { mutableStateOf(45) }

    // Auto countdown for QR code refresh
    LaunchedEffect(isWaLinked) {
        if (!isWaLinked) {
            while (qrSecondsLeft > 0) {
                delay(1000)
                qrSecondsLeft--
                if (qrSecondsLeft == 0) {
                    qrSecondsLeft = 45
                }
            }
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pengaturan", "QR Paket", "Riwayat")

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Bot WhatsApp", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = headerBg, contentColor = neonCyan) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, color = if (selectedTab == index) neonCyan else textSecondary) }
                    )
                }
            }
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // Status & Linkage Section
            item {
                if (isWaLinked) {
                    // Connected Status UI
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, successGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(successGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Status: Terhubung", color = successGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(successGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("BAILEYS MD", color = successGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = textSecondary.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nomor Bot WhatsApp", color = textSecondary, fontSize = 13.sp)
                                Text("+62 881-0371-60075", color = textMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sesi Tersimpan", color = textSecondary, fontSize = 13.sp)
                                Text("active_session_master", color = textMain, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Terhubung Sejak", color = textSecondary, fontSize = 13.sp)
                                Text("18 Juli 2026, 10.45", color = textMain, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Browser Sesi", color = textSecondary, fontSize = 13.sp)
                                Text("Chrome (Linux OS)", color = textMain, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        isWaLinked = false 
                                        Toast.makeText(context, "Sesi WhatsApp diputus.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC3545)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Putuskan Sesi")
                                }
                            }
                        }
                    }
                } else {
                    // Not Connected QR Code UI
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, warningYellow.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(warningYellow)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tautan WhatsApp: Belum Tertaut", color = warningYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(warningYellow.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("PROSES PAIRING", color = warningYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = textSecondary.copy(alpha = 0.15f))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Langkah-langkah Penyambungan:", color = textMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("1. Buka aplikasi WhatsApp di ponsel Anda.", color = textSecondary, fontSize = 12.sp)
                                Text("2. Ketuk Menu (titik tiga di kanan atas) atau Pengaturan, lalu pilih Perangkat Tertaut.", color = textSecondary, fontSize = 12.sp)
                                Text("3. Ketuk tombol Tautkan Perangkat dan arahkan kamera ponsel ke QR Code di bawah.", color = textSecondary, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive QR Code Container
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, textSecondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        isWaLinked = true
                                        Toast.makeText(context, "Berhasil Menghubungkan WhatsApp Bot!", Toast.LENGTH_LONG).show()
                                    }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Canvas(modifier = Modifier.size(160.dp).background(Color.White)) {
                                        val qrColor = Color(0xFF1E1E1E)
                                        // Top-Left Finder
                                        drawRect(color = qrColor, topLeft = Offset(0f, 0f), size = Size(40f, 40f))
                                        drawRect(color = Color.White, topLeft = Offset(8f, 8f), size = Size(24f, 24f))
                                        drawRect(color = qrColor, topLeft = Offset(12f, 12f), size = Size(16f, 16f))

                                        // Top-Right Finder
                                        drawRect(color = qrColor, topLeft = Offset(size.width - 40f, 0f), size = Size(40f, 40f))
                                        drawRect(color = Color.White, topLeft = Offset(size.width - 32f, 8f), size = Size(24f, 24f))
                                        drawRect(color = qrColor, topLeft = Offset(size.width - 28f, 12f), size = Size(16f, 16f))

                                        // Bottom-Left Finder
                                        drawRect(color = qrColor, topLeft = Offset(0f, size.height - 40f), size = Size(40f, 40f))
                                        drawRect(color = Color.White, topLeft = Offset(8f, size.height - 32f), size = Size(24f, 24f))
                                        drawRect(color = qrColor, topLeft = Offset(12f, size.height - 28f), size = Size(16f, 16f))

                                        // Generate structured pseudo QR noise
                                        val random = java.util.Random(424242)
                                        val cellSize = 8f
                                        val cols = (size.width / cellSize).toInt()
                                        val rows = (size.height / cellSize).toInt()
                                        for (r in 0 until rows) {
                                            for (c in 0 until cols) {
                                                // Skip finder zones
                                                if ((r < 6 && c < 6) || (r < 6 && c >= cols - 6) || (r >= rows - 6 && c < 6)) {
                                                    continue
                                                }
                                                // Center WhatsApp-like green indicator badge space
                                                if (r in (rows/2 - 2)..(rows/2 + 2) && c in (cols/2 - 2)..(cols/2 + 2)) {
                                                    continue
                                                }
                                                if (random.nextBoolean()) {
                                                    drawRect(
                                                        color = qrColor,
                                                        topLeft = Offset(c * cellSize, r * cellSize),
                                                        size = Size(cellSize, cellSize)
                                                    )
                                                }
                                            }
                                        }

                                        // Draw a centered subtle green indicator
                                        drawCircle(
                                            color = Color(0xFF25D366),
                                            radius = 16f,
                                            center = Offset(size.width / 2, size.height / 2)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Klik QR Code untuk Simulasikan Scan", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR Code diperbarui otomatis dalam: ", color = textSecondary, fontSize = 12.sp)
                                Text("${qrSecondsLeft}s", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(warningYellow.copy(alpha = 0.1f))
                                    .padding(10.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = warningYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "PENTING: Gunakan akun WhatsApp AKBAR MEDIA atau bot khusus. Sesi ini dikelola aman melalui enkripsi socket Baileys.", 
                                    color = textSecondary, 
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // WhatsApp Gateway Section
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Section Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = neonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Konfigurasi Gateway Baileys",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Main Toggle Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (waGatewayEnabled) neonCyan.copy(alpha = 0.5f) else textSecondary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Aktifkan WhatsApp Gateway",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = textMain
                                    )
                                    Text(
                                        "Kirim pesan pemberitahuan otomatis ke pelanggan menggunakan Baileys socket",
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                                Switch(
                                    checked = waGatewayEnabled,
                                    onCheckedChange = { waGatewayEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = successGreen,
                                        uncheckedThumbColor = textSecondary,
                                        uncheckedTrackColor = cardBg
                                    )
                                )
                            }
                            
                            HorizontalDivider(color = textSecondary.copy(alpha = 0.15f))
                            
                            // Status Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Build,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Engine Core", color = textSecondary, fontSize = 13.sp)
                                }
                                Text("@whiskeysockets/baileys (v6.6.0)", color = textMain, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Jeda Pengiriman (Delay)", color = textSecondary, fontSize = 13.sp)
                                }
                                Text("3 - 5 Detik (Anti-Blokir)", color = warningYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Toggles & Message Customization Cards
                    Text(
                        "Kustomisasi Template Pesan",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // 1. New Billing Template Card
                    TemplateConfigCard(
                        title = "Notifikasi Tagihan Baru",
                        description = "Kirim pesan saat tagihan bulanan baru diterbitkan",
                        isEnabled = waNotifyNewBilling,
                        onEnabledChange = { waNotifyNewBilling = it },
                        templateText = waTemplateNewBilling,
                        onTemplateTextChange = { waTemplateNewBilling = it },
                        placeholders = listOf(
                            "{nama}" to "Nama Pelanggan",
                            "{bulan}" to "Bulan Tagihan",
                            "{nominal}" to "Jumlah Tagihan"
                        ),
                        cardBg = cardBg,
                        textMain = textMain,
                        textSecondary = textSecondary,
                        successGreen = successGreen,
                        neonCyan = neonCyan
                    )

                    // 2. Payment Success Template Card
                    TemplateConfigCard(
                        title = "Notifikasi Pembayaran Lunas",
                        description = "Kirim struk pembayaran digital lunas otomatis",
                        isEnabled = waNotifyPaymentSuccess,
                        onEnabledChange = { waNotifyPaymentSuccess = it },
                        templateText = waTemplatePaymentSuccess,
                        onTemplateTextChange = { waTemplatePaymentSuccess = it },
                        placeholders = listOf(
                            "{nama}" to "Nama Pelanggan",
                            "{bulan}" to "Bulan Tagihan",
                            "{nominal}" to "Jumlah Tagihan"
                        ),
                        cardBg = cardBg,
                        textMain = textMain,
                        textSecondary = textSecondary,
                        successGreen = successGreen,
                        neonCyan = neonCyan
                    )

                    // 3. Isolir Template Card
                    TemplateConfigCard(
                        title = "Notifikasi Peringatan Isolir",
                        description = "Kirim peringatan isolir jika belum membayar tagihan",
                        isEnabled = waNotifyIsolir,
                        onEnabledChange = { waNotifyIsolir = it },
                        templateText = waTemplateIsolir,
                        onTemplateTextChange = { waTemplateIsolir = it },
                        placeholders = listOf(
                            "{nama}" to "Nama Pelanggan",
                            "{bulan}" to "Bulan Tagihan"
                        ),
                        cardBg = cardBg,
                        textMain = textMain,
                        textSecondary = textSecondary,
                        successGreen = successGreen,
                        neonCyan = neonCyan
                    )

                    // 4. OTP Template Card
                    TemplateConfigCard(
                        title = "Pesan Kode OTP Verifikasi",
                        description = "Kirim kode verifikasi OTP sistem melalui WA",
                        isEnabled = waNotifyOtp,
                        onEnabledChange = { waNotifyOtp = it },
                        templateText = waTemplateOtp,
                        onTemplateTextChange = { waTemplateOtp = it },
                        placeholders = listOf(
                            "{otp}" to "Kode Verifikasi OTP"
                        ),
                        cardBg = cardBg,
                        textMain = textMain,
                        textSecondary = textSecondary,
                        successGreen = successGreen,
                        neonCyan = neonCyan
                    )

                    // Save Button
                    Button(
                        onClick = {
                            com.example.ui.data.SettingsManager.waGatewayEnabled = waGatewayEnabled
                            com.example.ui.data.SettingsManager.waNotifyNewBilling = waNotifyNewBilling
                            com.example.ui.data.SettingsManager.waNotifyPaymentSuccess = waNotifyPaymentSuccess
                            com.example.ui.data.SettingsManager.waNotifyIsolir = waNotifyIsolir
                            com.example.ui.data.SettingsManager.waNotifyOtp = waNotifyOtp
                            
                            com.example.ui.data.SettingsManager.waTemplateNewBilling = waTemplateNewBilling
                            com.example.ui.data.SettingsManager.waTemplatePaymentSuccess = waTemplatePaymentSuccess
                            com.example.ui.data.SettingsManager.waTemplateIsolir = waTemplateIsolir
                            com.example.ui.data.SettingsManager.waTemplateOtp = waTemplateOtp
                            
                            Toast.makeText(context, "Pengaturan WhatsApp Gateway Baileys Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Konfigurasi Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test Gateway Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, warningYellow.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null,
                                    tint = warningYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uji Coba Kirim Pesan Baileys", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textMain)
                            }

                            Text(
                                "Uji langsung koneksi gateway Baileys dengan mengirim pesan percobaan ke nomor apa saja.",
                                fontSize = 12.sp,
                                color = textSecondary
                            )

                            OutlinedTextField(
                                value = testDestination,
                                onValueChange = { testDestination = it },
                                label = { Text("Nomor WhatsApp Penerima", color = textMain.copy(alpha = 0.7f)) },
                                placeholder = { Text("Contoh: 08123456789 atau 628123456789", color = textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonCyan,
                                    unfocusedBorderColor = textMain.copy(alpha = 0.3f),
                                    focusedTextColor = textMain,
                                    unfocusedTextColor = textMain
                                )
                            )

                            OutlinedTextField(
                                value = testMessage,
                                onValueChange = { testMessage = it },
                                label = { Text("Isi Pesan Uji Coba", color = textMain.copy(alpha = 0.7f)) },
                                placeholder = { Text("Ketik pesan percobaan di sini...", color = textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonCyan,
                                    unfocusedBorderColor = textMain.copy(alpha = 0.3f),
                                    focusedTextColor = textMain,
                                    unfocusedTextColor = textMain
                                )
                            )

                            Button(
                                onClick = {
                                    if (testDestination.isEmpty() || testMessage.isEmpty()) {
                                        Toast.makeText(context, "Nomor penerima dan isi pesan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isTestSending = true
                                        // Simulate api call
                                        coroutineScope.launch {
                                            delay(1500)
                                            isTestSending = false
                                            Toast.makeText(context, "Pesan Berhasil Terkirim via Baileys WhatsApp Gateway!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = successGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isTestSending
                            ) {
                                if (isTestSending) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Kirim Uji Coba", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
        } else if (selectedTab == 1) {
            BotWaPackageQrScreen()
        } else {
            BotWaHistoryScreen()
        }
    }
}
}

@Composable
fun TemplateConfigCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    templateText: String,
    onTemplateTextChange: (String) -> Unit,
    placeholders: List<Pair<String, String>>,
    cardBg: Color,
    textMain: Color,
    textSecondary: Color,
    successGreen: Color,
    neonCyan: Color
) {
    var showTestDialog by remember { mutableStateOf(false) }
    var testDestNumber by remember { mutableStateOf("") }
    var isSendingTest by remember { mutableStateOf(false) }
    val localContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (showTestDialog) {
        val previewText = remember(templateText) {
            var text = templateText
            placeholders.forEach { (tag, desc) ->
                val sampleValue = when (tag) {
                    "{nama}" -> "Ahmad"
                    "{bulan}" -> "Juli 2026"
                    "{nominal}" -> "Rp. 100.000"
                    "{otp}" -> "123456"
                    else -> "Contoh"
                }
                text = text.replace(tag, sampleValue)
            }
            text
        }

        AlertDialog(
            onDismissRequest = { if (!isSendingTest) showTestDialog = false },
            title = {
                Text(
                    text = "Uji Kirim: $title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMain
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kirim pesan percobaan menggunakan template ini untuk memastikan format pesan sudah sesuai di WhatsApp.",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    OutlinedTextField(
                        value = testDestNumber,
                        onValueChange = { testDestNumber = it },
                        label = { Text("Nomor WA Penerima") },
                        placeholder = { Text("Contoh: 08123456789") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textMain.copy(alpha = 0.2f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )

                    Text(
                        text = "Pratinjau Pesan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMain
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(textMain.copy(alpha = 0.05f))
                            .border(0.5.dp, textMain.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = previewText,
                            fontSize = 12.sp,
                            color = textMain
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (testDestNumber.trim().isEmpty()) {
                            Toast.makeText(localContext, "Harap masukkan nomor penerima!", Toast.LENGTH_SHORT).show()
                        } else {
                            isSendingTest = true
                            coroutineScope.launch {
                                delay(1500)
                                isSendingTest = false
                                showTestDialog = false
                                Toast.makeText(localContext, "Pesan uji coba berhasil dikirim ke $testDestNumber!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
                    enabled = !isSendingTest
                ) {
                    if (isSendingTest) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Kirim", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTestDialog = false },
                    enabled = !isSendingTest
                ) {
                    Text("Batal", color = textSecondary)
                }
            },
            containerColor = cardBg
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, textSecondary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textMain)
                    Text(description, fontSize = 11.sp, color = textSecondary)
                }
                Switch(
                    checked = isEnabled,
                    onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = successGreen,
                        uncheckedThumbColor = textSecondary,
                        uncheckedTrackColor = cardBg
                    )
                )
            }

            if (isEnabled) {
                HorizontalDivider(color = textSecondary.copy(alpha = 0.1f))
                
                OutlinedTextField(
                    value = templateText,
                    onValueChange = onTemplateTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan,
                        unfocusedBorderColor = textMain.copy(alpha = 0.2f),
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Variabel yang tersedia:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                    
                    Button(
                        onClick = { showTestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = neonCyan.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            tint = neonCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uji Kirim", color = neonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    placeholders.forEach { (tag, desc) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tag,
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(neonCyan.copy(alpha = 0.1f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(desc, color = textSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotWaPackageQrScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var packages by remember { mutableStateOf<List<com.example.ui.screens.InternetPackage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    fun loadPackages() {
        coroutineScope.launch {
            try {
                packages = com.example.ui.data.remote.ApiClient.apiService.getPackages()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Gagal memuat paket: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPackages()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = neonCyan)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Upload QR Pembayaran Paket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textMain
                )
                Text(
                    text = "Gambar ini akan dikirimkan secara otomatis bersama dengan pesan WA (Notifikasi Tagihan/Isolir) untuk pelanggan yang menggunakan paket tersebut.",
                    fontSize = 13.sp,
                    color = textMain.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            items(packages) { pkg ->
                var isUploading by remember { mutableStateOf(false) }
                
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let {
                        isUploading = true
                        coroutineScope.launch {
                            try {
                                val contentResolver = context.contentResolver
                                val inputStream = contentResolver.openInputStream(it)
                                val bytes = inputStream?.readBytes()
                                if (bytes != null) {
                                    val mediaType = "image/*".toMediaTypeOrNull()
                                    val requestBody = bytes.toRequestBody(mediaType)
                                    val part = okhttp3.MultipartBody.Part.createFormData("image", "qr_${pkg.id}.jpg", requestBody)
                                    com.example.ui.data.remote.ApiClient.apiService.uploadPackageImage(pkg.id, part)
                                    android.widget.Toast.makeText(context, "QR Gambar Berhasil Diupload", android.widget.Toast.LENGTH_SHORT).show()
                                    loadPackages() // Refresh packages to get new URL
                                }
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Gagal upload: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            } finally {
                                isUploading = false
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textMain)
                        Text("Harga: Rp ${java.text.NumberFormat.getInstance(java.util.Locale("id", "ID")).format(pkg.price)}", fontSize = 14.sp, color = textMain.copy(alpha=0.7f))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (pkg.qr_image_url != null) {
                            Text("✓ Gambar QR Terpasang", color = Color(0xFF28A745), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Text("Belum ada gambar QR", color = Color.Red, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Button(
                            onClick = { launcher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isUploading
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (pkg.qr_image_url != null) "Ganti Gambar" else "Upload Gambar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotWaHistoryScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var history by remember { mutableStateOf<List<com.example.ui.data.remote.WaHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    fun loadHistory() {
        coroutineScope.launch {
            try {
                history = com.example.ui.data.remote.ApiClient.apiService.getWaHistory()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Gagal memuat riwayat: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHistory()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = neonCyan)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (history.isEmpty()) {
                item {
                    Text("Belum ada riwayat pesan.", color = textMain.copy(alpha=0.6f))
                }
            } else {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(item.customer_name ?: "Unknown", fontWeight = FontWeight.Bold, color = textMain)
                                Text(
                                    text = item.status ?: "UNKNOWN", 
                                    color = if (item.status == "TERKIRIM") Color(0xFF28A745) else Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(item.phone ?: "", color = textMain.copy(alpha=0.6f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.message ?: "", color = textMain, fontSize = 13.sp)
                            if (!item.media_url.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("✓ Mengandung Gambar QR", color = neonCyan, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.created_at ?: "", color = textMain.copy(alpha=0.5f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
