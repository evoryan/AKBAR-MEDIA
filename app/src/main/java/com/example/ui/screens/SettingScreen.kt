package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageInfo
import androidx.compose.material.icons.filled.SystemUpdate
import kotlinx.coroutines.launch
import com.example.data.GithubApiService
import com.example.data.GithubRelease
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.os.Build
import android.provider.Settings
import com.example.BuildConfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBack: () -> Unit,
    onNavigateToUpdateEmail: () -> Unit,
    onNavigateToUpdateProfil: () -> Unit,
    onNavigateToGantiPassword: () -> Unit,
    onNavigateToGantiPin: () -> Unit,
    onNavigateToDaftarAdmin: () -> Unit,
    onNavigateToOdc: () -> Unit,
    onNavigateToOdp: () -> Unit,
    onNavigateToRasio: () -> Unit,
    onNavigateToGatewayPayment: () -> Unit,
    onNavigateToCompanySettings: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToInvoiceSettings: () -> Unit,
    onNavigateToInfoTenant: () -> Unit,
    onNavigateToSinkronisasi: () -> Unit,
    onLogout: () -> Unit
) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    val currentUser by UserSession.currentUser.collectAsState()

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Setting", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // AKUN
            Text("AKUN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(icon = Icons.Default.Email, title = "Update Email", subtitle = "Daftar email admin & verifikasi", iconTint = textMain, onClick = onNavigateToUpdateEmail)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Person, title = "Update Profil", subtitle = "Foto profil, Nama, Username", iconTint = textMain, onClick = onNavigateToUpdateProfil)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Lock, title = "Ganti Password", subtitle = "Ubah password login", iconTint = textMain, onClick = onNavigateToGantiPassword)
                    if (currentUser?.role == UserRole.SUPER_ADMIN) { HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Dialpad, title = "Ganti PIN", subtitle = "PIN verifikasi hapus transaksi", iconTint = textMain, onClick = onNavigateToGantiPin) }
                    
                    if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) { HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = onNavigateToDaftarAdmin) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TAMPILAN APLIKASI
            Text("TAMPILAN APLIKASI", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<GithubRelease?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    val currentVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }
    var showThemeDialog by remember { mutableStateOf(false) }
            var currentTheme by remember { mutableStateOf(com.example.ui.data.SettingsManager.appTheme) }
            
            var showFontScaleDialog by remember { mutableStateOf(false) }
            var currentFontScale by remember { mutableStateOf(com.example.ui.data.SettingsManager.fontScale) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(
                        icon = Icons.Default.Palette, 
                        title = "Tema Aplikasi", 
                        subtitle = currentTheme, 
                        iconTint = textMain, 
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(color = cardBorder)
                    SettingItem(
                        icon = Icons.Default.FormatSize, 
                        title = "Skala Ukuran Font", 
                        subtitle = currentFontScale, 
                        iconTint = textMain, 
                        onClick = { showFontScaleDialog = true }
                    )
                }
            }
            
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Pilih Tema", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)) },
                    containerColor = cardBg,
                    text = {
                        Column {
                            listOf("Sesuai Sistem", "Tema Gelap", "Tema Terang").forEach { themeName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            com.example.ui.data.SettingsManager.appTheme = themeName
                                            currentTheme = themeName
                                            showThemeDialog = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (themeName == currentTheme),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = primaryBg)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(themeName, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Batal", color = primaryBg)
                        }
                    }
                )
            }

            if (showFontScaleDialog) {
                AlertDialog(
                    onDismissRequest = { showFontScaleDialog = false },
                    title = { Text("Pilih Ukuran Font", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)) },
                    containerColor = cardBg,
                    text = {
                        Column {
                            listOf("Kecil", "Sedang", "Besar").forEach { scaleName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            com.example.ui.data.SettingsManager.fontScale = scaleName
                                            currentFontScale = scaleName
                                            showFontScaleDialog = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (scaleName == currentFontScale),
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = primaryBg)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(scaleName, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFontScaleDialog = false }) {
                            Text("Batal", color = primaryBg)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // ODC & ODP
            Text("ODC & ODP", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(icon = Icons.Default.DeviceHub, title = "ODC", subtitle = "Kelola ODC", iconTint = textMain, onClick = onNavigateToOdc)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Hub, title = "ODP", subtitle = "Kelola ODP", iconTint = textMain, onClick = onNavigateToOdp)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.AccountTree, title = "Rasio", subtitle = "Kelola Data Rasio", iconTint = textMain, onClick = onNavigateToRasio)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // TAMPILAN
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                Text("TAMPILAN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SettingItem(icon = Icons.Default.Edit, title = "Tampilan Aplikasi", subtitle = "Ubah nama perusahaan & info dashboard", iconTint = textMain, onClick = onNavigateToCompanySettings)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Lain-Lain
            if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN || currentUser?.username == "akbar2026") {
                Text("LAIN-LAIN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        var shownItems = 0
                        if (currentUser?.role == UserRole.SUPER_ADMIN) {
                            SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                            shownItems++
                            HorizontalDivider(color = cardBorder)
                            SettingItem(icon = Icons.Default.Backup, title = "Backup & Restore", subtitle = "Database Pelanggan", iconTint = textMain, onClick = onNavigateToBackupRestore)
                            shownItems++
                            HorizontalDivider(color = cardBorder)
                            SettingItem(icon = Icons.Default.Receipt, title = "Pengaturan Invoice", subtitle = "Atur text header dan footer invoice (Thermal)", iconTint = textMain, onClick = onNavigateToInvoiceSettings)
                            shownItems++
                        }
                        
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                            if (shownItems > 0) {
                                HorizontalDivider(color = cardBorder)
                            }
                            SettingItem(
                                icon = Icons.Default.Sync,
                                title = "Sinkronisasi",
                                subtitle = "Cek secret Mikrotik yang belum digunakan",
                                iconTint = textMain,
                                onClick = onNavigateToSinkronisasi
                            )
                            shownItems++
                        }

                        if (currentUser?.username == "akbar2026") {
                            if (shownItems > 0) {
                                HorizontalDivider(color = cardBorder)
                            }
                            SettingItem(
                                icon = Icons.Default.Business,
                                title = "Info Tenant",
                                subtitle = "Form menulis informasi tenant",
                                iconTint = textMain,
                                onClick = onNavigateToInfoTenant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // API & KONEKSI
            Text("API & KONEKSI", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            var showApiUrlDialog by remember { mutableStateOf(false) }
            var currentApiUrl by remember { mutableStateOf(com.example.ui.data.SettingsManager.apiBaseUrl) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(
                        icon = Icons.Default.Link,
                        title = "Alamat API Backend",
                        subtitle = currentApiUrl,
                        iconTint = textMain,
                        onClick = { showApiUrlDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (showApiUrlDialog) {
                var inputUrl by remember { mutableStateOf(currentApiUrl) }
                AlertDialog(
                    onDismissRequest = { showApiUrlDialog = false },
                    title = { Text("Ubah API Backend", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)) },
                    containerColor = cardBg,
                    text = {
                        Column {
                            Text("Masukkan URL API backend baru:", color = textSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputUrl,
                                onValueChange = { inputUrl = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryBg,
                                    unfocusedBorderColor = cardBorder,
                                    focusedTextColor = textMain,
                                    unfocusedTextColor = textMain
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (inputUrl.isNotBlank()) {
                                    val formattedUrl = if (inputUrl.endsWith("/")) inputUrl else "$inputUrl/"
                                    com.example.ui.data.SettingsManager.apiBaseUrl = formattedUrl
                                    currentApiUrl = formattedUrl
                                    com.example.ui.data.remote.ApiClient.updateBaseUrl(formattedUrl)
                                    Toast.makeText(context, "API URL berhasil diubah", Toast.LENGTH_SHORT).show()
                                }
                                showApiUrlDialog = false
                            }
                        ) {
                            Text("Simpan", color = primaryBg)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showApiUrlDialog = false }) {
                            Text("Batal", color = textSecondary)
                        }
                    }
                )
            }

            // SISTEM
            Text("SISTEM", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                Column {
                    SettingItem(
                        icon = Icons.Default.SystemUpdate, 
                        title = "Cek Update", 
                        subtitle = if (isCheckingUpdate) "Memeriksa..." else "Versi $currentVersion", 
                        iconTint = textMain, 
                        onClick = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                coroutineScope.launch {
                                    try {
                                        val api = GithubApiService.create()
                                        val release = api.getLatestRelease("evoryan", "AKBAR-MEDIA")
                                        updateInfo = release
                                        showUpdateDialog = true
                                    } catch (e: retrofit2.HttpException) {
                                        if (e.code() == 404) {
                                            Toast.makeText(context, "Belum ada update (404 Not Found)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal memeriksa update: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal memeriksa update: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCheckingUpdate = false
                                    }
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))


    if (showUpdateDialog && updateInfo != null) {
        val latestTag = updateInfo!!.tag_name
        val latestVersion = latestTag.trimStart('v', 'V')
        val currentClean = (currentVersion ?: "1.0").trimStart('v', 'V')
        val isNewer = isVersionNewer(latestVersion, currentClean)
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        var downloadError by remember { mutableStateOf<String?>(null) }
        
        val apkDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val existingApk = remember(showUpdateDialog) {
            val f = File(apkDir, "update.apk")
            if (f.exists() && f.length() > 500000L) f else null
        }
        var downloadedFile by remember { mutableStateOf<File?>(existingApk) }

        val targetAsset = if (BuildConfig.DEBUG) {
            updateInfo!!.assets.firstOrNull { it.browser_download_url.contains("debug", ignoreCase = true) }
                ?: updateInfo!!.assets.firstOrNull { it.browser_download_url.endsWith(".apk") }
                ?: updateInfo!!.assets.firstOrNull()
        } else {
            updateInfo!!.assets.firstOrNull { it.browser_download_url.contains("release", ignoreCase = true) }
                ?: updateInfo!!.assets.firstOrNull { it.browser_download_url.endsWith(".apk") }
                ?: updateInfo!!.assets.firstOrNull()
        }
        val url = targetAsset?.browser_download_url

        val startDownload = {
            if (url != null) {
                isDownloading = true
                downloadError = null
                coroutineScope.launch {
                    val file = downloadApk(context, url) { progress ->
                        downloadProgress = progress
                    }
                    isDownloading = false
                    if (file != null) {
                        downloadedFile = file
                        installApk(context, file)
                    } else {
                        downloadError = "Gagal mengunduh update. Periksa koneksi internet atau gunakan opsi browser."
                        Toast.makeText(context, "Gagal mengunduh update", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Link download tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
        
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showUpdateDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = primaryBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isNewer) "Update Tersedia" else "Sudah Versi Terbaru", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = { 
                Column {
                    Text("Versi saat ini: $currentVersion", fontSize = 14.sp)
                    Text("Versi rilis (GitHub Tag): $latestTag", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primaryBg)
                    
                    if (updateInfo!!.body?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Catatan Rilis:\n${updateInfo!!.body?.take(300)}${if ((updateInfo!!.body?.length ?: 0) > 300) "..." else ""}",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isDownloading) {
                        Text("Mengunduh update... ${(downloadProgress * 100).toInt()}%", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = primaryBg,
                        )
                    } else if (downloadedFile != null && downloadedFile!!.exists()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "✓ File update telah siap dipasang (${(downloadedFile!!.length() / (1024 * 1024))} MB)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF10B981)
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasInstallPermission(context)) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "⚠️ Izin 'Install aplikasi tidak dikenal' belum aktif pada perangkat ini.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE11D48)
                                    )
                                }
                            }
                        }
                    } else {
                        if (downloadError != null) {
                            Text(
                                text = downloadError ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFFFF4D4F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (isNewer) {
                            Text("Apakah Anda ingin mengunduh dan memasang versi terbaru sekarang?", fontSize = 13.sp)
                        } else {
                            Text("Aplikasi sudah di versi terbaru. Anda dapat mengunduh ulang jika diperlukan.", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tips: Jika muncul 'Aplikasi tidak terinstall', pastikan izin instalasi aktif atau hapus versi lama jika berasal dari build dev/debug berbeda.",
                        fontSize = 11.sp,
                        color = textSecondary.copy(alpha = 0.7f),
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (downloadedFile != null && downloadedFile!!.exists() && !isDownloading) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasInstallPermission(context)) {
                                    openInstallPermissionSettings(context)
                                } else {
                                    installApk(context, downloadedFile!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBg)
                        ) {
                            Text(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasInstallPermission(context)) 
                                    "Aktifkan Izin & Pasang" 
                                else 
                                    "Pasang Update Sekarang"
                            )
                        }
                        OutlinedButton(
                            onClick = { startDownload() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unduh Ulang", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { startDownload() },
                            enabled = !isDownloading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBg)
                        ) {
                            Text(if (isDownloading) "Mengunduh..." else if (isNewer) "Download & Pasang Update" else "Unduh Ulang")
                        }
                    }

                    if (url != null && !isDownloading) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membuka browser: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unduh Langsung via Browser", fontSize = 12.sp)
                        }
                    }

                    if (!isDownloading) {
                        TextButton(
                            onClick = { showUpdateDialog = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Tutup", color = textSecondary)
                        }
                    }
                }
            },
            dismissButton = {}
        )
    }

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF003C).copy(alpha = 0.1f),
                    contentColor = Color(0xFFFF003C)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF003C).copy(alpha = 0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Akbar Media Group ©2026",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF666666) else Color(0xFF999999),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
            }
        }
    }
}


fun isVersionNewer(latest: String, current: String): Boolean {
    val lParts = latest.removePrefix("v").split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
    val cParts = current.removePrefix("v").split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
    val maxLen = maxOf(lParts.size, cParts.size)
    for (i in 0 until maxLen) {
        val l = lParts.getOrElse(i) { 0 }
        val c = cParts.getOrElse(i) { 0 }
        if (l > c) return true
        if (l < c) return false
    }
    return false
}

fun hasInstallPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        true
    }
}

fun openInstallPermissionSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}

suspend fun downloadApk(
    context: android.content.Context,
    url: String,
    onProgress: (Float) -> Unit
): File? {
    return withContext(Dispatchers.IO) {
        var connection: java.net.HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: java.io.FileOutputStream? = null
        var tempFile: File? = null
        try {
            var currentUrl = url
            var redirectCount = 0

            // Follow HTTP redirects explicitly across domains/protocols (up to 10 redirects)
            while (true) {
                val u = java.net.URL(currentUrl)
                connection = (u.openConnection() as java.net.HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    setRequestProperty("Accept", "*/*")
                    connectTimeout = 20000
                    readTimeout = 30000
                }
                connection.connect()

                val code = connection.responseCode
                if ((code == java.net.HttpURLConnection.HTTP_MOVED_PERM ||
                     code == java.net.HttpURLConnection.HTTP_MOVED_TEMP ||
                     code == java.net.HttpURLConnection.HTTP_SEE_OTHER ||
                     code == 307 || code == 308) && redirectCount < 10) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (!location.isNullOrEmpty()) {
                        currentUrl = if (location.startsWith("http")) location else java.net.URL(u, location).toString()
                        redirectCount++
                        continue
                    }
                }
                if (code != java.net.HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    return@withContext null
                }
                break
            }

            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?: context.cacheDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            tempFile = File(downloadDir, "update_download.apk.tmp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            val apkFile = File(downloadDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            inputStream = connection.inputStream
            outputStream = java.io.FileOutputStream(tempFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int

            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            outputStream.close()
            outputStream = null
            inputStream.close()
            inputStream = null
            connection.disconnect()

            if (!tempFile.exists() || tempFile.length() < 100000L) {
                tempFile.delete()
                return@withContext null
            }

            if (tempFile.renameTo(apkFile)) {
                apkFile
            } else {
                tempFile.copyTo(apkFile, overwrite = true)
                tempFile.delete()
                apkFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { tempFile?.delete() } catch (_: Exception) {}
            null
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { inputStream?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }
}

fun installApk(context: android.content.Context, apkFile: File) {
    if (!apkFile.exists() || apkFile.length() < 100000L) {
        Toast.makeText(context, "File update rusak atau tidak lengkap", Toast.LENGTH_LONG).show()
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            Toast.makeText(
                context,
                "Harap aktifkan izin 'Izinkan dari sumber ini' di Pengaturan",
                Toast.LENGTH_LONG
            ).show()
            openInstallPermissionSettings(context)
            return
        }
    }

    try {
        val apkUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }

        val resInfoList = context.packageManager.queryIntentActivities(
            installIntent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(installIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal membuka installer: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
