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
    onNavigateToBotWa: () -> Unit = {},
    onNavigateToManageTenant: () -> Unit = {},
    onLogout: () -> Unit
) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF1F5F9)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF0F172A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0265DC)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF475569)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFCBD5E1)
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
    var latestReleaseTag by remember { mutableStateOf<String?>(null) }
    
    val currentVersion by com.example.ui.data.SettingsManager.appVersionFlow.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                com.example.ui.data.SettingsManager.refreshAppVersion(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val release = fetchLatestGithubRelease()
            updateInfo = release
            latestReleaseTag = release.tag_name
        } catch (_: Exception) {
            // Silently fallback to currentVersion if offline
        }
    }
    var showThemeDialog by remember { mutableStateOf(false) }
            var currentTheme by remember { mutableStateOf(com.example.ui.data.SettingsManager.appTheme) }
            
            var showFontScaleDialog by remember { mutableStateOf(false) }
            var currentFontScale by remember { mutableStateOf(com.example.ui.data.SettingsManager.fontScale) }
            
            var showServerDialog by remember { mutableStateOf(false) }
            var currentApiUrl by remember { mutableStateOf(com.example.ui.data.SettingsManager.apiBaseUrl) }
            var customUrlInput by remember { mutableStateOf(currentApiUrl) }
            var isTestingConnection by remember { mutableStateOf(false) }
            
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
                            // Menu Pengaturan Gateway Payment (API & Koneksi) disembunyikan sesuai instruksi
                            SettingItem(icon = Icons.Default.Backup, title = "Backup & Restore", subtitle = "Database Pelanggan", iconTint = textMain, onClick = onNavigateToBackupRestore)
                            shownItems++
                            HorizontalDivider(color = cardBorder)
                            SettingItem(icon = Icons.Default.Receipt, title = "Pengaturan Invoice", subtitle = "Kop logo & identitas faktur", iconTint = textMain, onClick = onNavigateToInvoiceSettings)
                            shownItems++
                            HorizontalDivider(color = cardBorder)
                            SettingItem(
                                icon = Icons.Default.Chat,
                                title = "Pengaturan WhatsApp",
                                subtitle = "WA Gateway, notifikasi otomatis & template pesan",
                                iconTint = textMain,
                                onClick = onNavigateToBotWa
                            )
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

                        if (currentUser?.role == UserRole.SUPER_ADMIN) {
                            if (shownItems > 0) {
                                HorizontalDivider(color = cardBorder)
                            }
                            val currentDb = currentUser?.db_name?.takeIf { it.isNotBlank() } ?: "Database"
                            SettingItem(
                                icon = Icons.Default.Business,
                                title = "Info Tenant",
                                subtitle = "Kelola informasi tenant [DB: $currentDb]",
                                iconTint = textMain,
                                onClick = onNavigateToInfoTenant
                            )
                            shownItems++
                        }

                        if (currentUser?.username == "akbar2026") {
                            if (shownItems > 0) {
                                HorizontalDivider(color = cardBorder)
                            }
                            SettingItem(
                                icon = Icons.Default.AdminPanelSettings,
                                title = "Kelola Tenant (Master)",
                                subtitle = "Panel kontrol manajemen tenant & DB (manage_tenant.sh)",
                                iconTint = primaryBg,
                                onClick = onNavigateToManageTenant
                            )
                            shownItems++
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
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
                        icon = Icons.Default.Dns,
                        title = "Pengaturan Server API",
                        subtitle = currentApiUrl,
                        iconTint = textMain,
                        onClick = {
                            customUrlInput = com.example.ui.data.SettingsManager.apiBaseUrl
                            showServerDialog = true
                        }
                    )
                    HorizontalDivider(color = cardBorder)

                    val isNewerAvailable = latestReleaseTag?.let { isVersionNewer(it, currentVersion) } == true
                    val displaySubtitle = when {
                        isCheckingUpdate -> "Memeriksa..."
                        isNewerAvailable -> "Versi $currentVersion (Update $latestReleaseTag tersedia)"
                        else -> "Versi $currentVersion (Terbaru)"
                    }
                    SettingItem(
                        icon = Icons.Default.SystemUpdate, 
                        title = "Cek Update", 
                        subtitle = displaySubtitle, 
                        iconTint = textMain, 
                        onClick = {
                            if (!isCheckingUpdate) {
                                isCheckingUpdate = true
                                coroutineScope.launch {
                                    try {
                                        val release = fetchLatestGithubRelease()
                                        updateInfo = release
                                        latestReleaseTag = release.tag_name
                                        showUpdateDialog = true
                                    } catch (e: retrofit2.HttpException) {
                                        if (e.code() == 404) {
                                            Toast.makeText(context, "Belum ada update di GitHub (404 Not Found)", Toast.LENGTH_SHORT).show()
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
        val isNewer = isVersionNewer(latestTag, currentVersion)
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        var downloadError by remember { mutableStateOf<String?>(null) }
        
        val cleanTag = latestTag.trim().trimStart('v', 'V').replace(Regex("[^a-zA-Z0-9.-]"), "_")
        val apkFileName = "akbarmedia_v${cleanTag}.apk"
        val apkDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val targetAsset = selectBestApkAsset(updateInfo!!)
        val url = targetAsset?.browser_download_url

        val existingApk = remember(showUpdateDialog, latestTag) {
            val f = File(apkDir, apkFileName)
            if (f.exists() && f.length() > 1000000L) {
                val apkVer = getApkVersion(context, f)
                if (apkVer != null && (apkVer.contains(cleanTag) || compareAppVersions(apkVer, currentVersion) > 0)) {
                    f
                } else {
                    f.delete()
                    null
                }
            } else null
        }
        var downloadedFile by remember { mutableStateOf<File?>(existingApk) }

        val startDownload = {
            if (url != null) {
                isDownloading = true
                downloadError = null
                coroutineScope.launch {
                    try {
                        apkDir.listFiles()?.forEach { file ->
                            if (file.name.endsWith(".apk") || file.name.endsWith(".tmp")) {
                                file.delete()
                            }
                        }
                    } catch (_: Exception) {}

                    val file = downloadApk(
                        context = context,
                        url = url,
                        fileName = apkFileName,
                        expectedSize = targetAsset.size
                    ) { progress ->
                        downloadProgress = progress
                    }
                    isDownloading = false
                    if (file != null && file.exists()) {
                        downloadedFile = file
                        val parsedApkVer = getApkVersion(context, file)
                        val verToApply = if (parsedApkVer != null && compareAppVersions(parsedApkVer, latestTag) > 0) {
                            parsedApkVer
                        } else {
                            latestTag
                        }
                        com.example.ui.data.SettingsManager.appliedAppVersion = verToApply
                        com.example.ui.data.SettingsManager.refreshAppVersion(context)
                        installApk(context, file)
                    } else {
                        downloadError = "Gagal mengunduh file update dari GitHub. Periksa koneksi internet atau gunakan opsi browser."
                        Toast.makeText(context, "Gagal mengunduh update", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Link file APK tidak ditemukan pada rilis GitHub", Toast.LENGTH_SHORT).show()
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
                    if (targetAsset != null) {
                        val sizeMb = (targetAsset.size ?: 0L).toFloat() / (1024f * 1024f)
                        val sizeStr = if (sizeMb > 0f) String.format(java.util.Locale.US, " (%.1f MB)", sizeMb) else ""
                        Text("File: ${targetAsset.name ?: "app-release.apk"}$sizeStr", fontSize = 12.sp, color = textSecondary)
                    }
                    
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
                                    "✓ File update siap dipasang (${(downloadedFile!!.length() / (1024 * 1024))} MB)",
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
                            Text("Tekan tombol di bawah untuk mengunduh dan memasang versi terbaru langsung dari GitHub:", fontSize = 13.sp)
                        } else {
                            Text("Aplikasi sudah di versi terbaru ($currentVersion). Anda dapat mengunduh ulang jika diperlukan.", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tips: Pastikan izin instalasi sumber tidak dikenal aktif di Pengaturan Android.",
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
                                val parsedApkVer = getApkVersion(context, downloadedFile!!)
                                val verToApply = if (parsedApkVer != null && compareAppVersions(parsedApkVer, latestTag) > 0) parsedApkVer else latestTag
                                com.example.ui.data.SettingsManager.appliedAppVersion = verToApply
                                com.example.ui.data.SettingsManager.refreshAppVersion(context)
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
                            Text("Unduh Ulang dari GitHub", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { startDownload() },
                            enabled = !isDownloading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBg)
                        ) {
                            Text(if (isDownloading) "Mengunduh..." else if (isNewer) "Download & Pasang Update" else "Unduh Ulang dari GitHub")
                        }
                    }

                    if (url != null && !isDownloading) {
                        OutlinedButton(
                            onClick = {
                                com.example.ui.data.SettingsManager.appliedAppVersion = latestTag
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

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { if (!isTestingConnection) showServerDialog = false },
            containerColor = cardBg,
            title = { Text("Pengaturan Server API", color = textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "URL Server API yang sedang digunakan:",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("URL Server API", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    Text("Preset Pilihan URL:", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    OutlinedButton(
                        onClick = { customUrlInput = "http://amg.akbarmediagroup.my.id/" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                            Text("Domain Utama (Rekomendasi)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryBg)
                            Text("http://amg.akbarmediagroup.my.id/", fontSize = 11.sp, color = textSecondary)
                        }
                    }

                    OutlinedButton(
                        onClick = { customUrlInput = "http://103.253.245.25:4100/" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                            Text("VPS IP Direct (Cadangan)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textMain)
                            Text("http://103.253.245.25:4100/", fontSize = 11.sp, color = textSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (customUrlInput.isBlank()) return@Button
                            val formatted = if (customUrlInput.endsWith("/")) customUrlInput else "$customUrlInput/"
                            coroutineScope.launch {
                                isTestingConnection = true
                                try {
                                    com.example.ui.data.SettingsManager.apiBaseUrl = formatted
                                    currentApiUrl = formatted
                                    val pingRes = com.example.ui.data.remote.ApiClient.apiService.ping()
                                    Toast.makeText(context, "Koneksi Berhasil: ${pingRes.status}", Toast.LENGTH_SHORT).show()
                                    showServerDialog = false
                                } catch (e: Exception) {
                                    com.example.ui.data.SettingsManager.apiBaseUrl = formatted
                                    currentApiUrl = formatted
                                    Toast.makeText(context, "URL Tersimpan! Peringatan koneksi: ${e.message}", Toast.LENGTH_LONG).show()
                                    showServerDialog = false
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBg),
                        enabled = !isTestingConnection
                    ) {
                        Text(if (isTestingConnection) "Menguji..." else "Simpan & Uji", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showServerDialog = false },
                    enabled = !isTestingConnection
                ) {
                    Text("Batal", color = textSecondary)
                }
            }
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


fun getApkVersion(context: android.content.Context, apkFile: File?): String? {
    if (apkFile == null || !apkFile.exists()) return null
    return try {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(apkFile.absolutePath, android.content.pm.PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(apkFile.absolutePath, 0)
        }
        info?.versionName
    } catch (_: Exception) {
        null
    }
}

fun compareAppVersions(v1: String, v2: String): Int {
    fun parseVersionParts(v: String): List<Int> {
        val cleaned = v.trim().trimStart('v', 'V', '.').trim()
        val parts = cleaned.split(".").filter { it.isNotBlank() }
        val nums = parts.map { part ->
            part.filter { it.isDigit() }.toIntOrNull() ?: 0
        }
        return if (nums.isEmpty()) listOf(0) else nums
    }

    val p1 = parseVersionParts(v1)
    val p2 = parseVersionParts(v2)
    val maxLen = maxOf(p1.size, p2.size)
    for (i in 0 until maxLen) {
        val l = p1.getOrElse(i) { 0 }
        val c = p2.getOrElse(i) { 0 }
        if (l > c) return 1
        if (l < c) return -1
    }
    return 0
}

fun isVersionNewer(latest: String, current: String): Boolean {
    return compareAppVersions(latest, current) > 0
}

suspend fun fetchLatestGithubRelease(): com.example.data.GithubRelease {
    val api = com.example.data.GithubApiService.create()
    return try {
        val allReleases = api.getAllReleases("evoryan", "AKBAR-MEDIA")
        val candidates = allReleases.filter { 
            it.draft != true && it.assets.any { asset -> 
                asset.browser_download_url.endsWith(".apk", ignoreCase = true) || 
                asset.name?.endsWith(".apk", ignoreCase = true) == true 
            }
        }
        if (candidates.isNotEmpty()) {
            candidates.maxWithOrNull { a, b ->
                compareAppVersions(a.tag_name, b.tag_name)
            } ?: candidates.first()
        } else {
            api.getLatestRelease("evoryan", "AKBAR-MEDIA")
        }
    } catch (_: Exception) {
        api.getLatestRelease("evoryan", "AKBAR-MEDIA")
    }
}

fun selectBestApkAsset(release: com.example.data.GithubRelease): com.example.data.GithubAsset? {
    val apkAssets = release.assets.filter {
        it.browser_download_url.endsWith(".apk", ignoreCase = true) ||
        it.name?.endsWith(".apk", ignoreCase = true) == true
    }
    if (apkAssets.isEmpty()) return release.assets.firstOrNull()
    if (apkAssets.size == 1) return apkAssets.first()
    
    return apkAssets.firstOrNull { 
        it.name?.contains("release", ignoreCase = true) == true || 
        it.browser_download_url.contains("release", ignoreCase = true) 
    } ?: apkAssets.first()
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
    fileName: String,
    expectedSize: Long? = null,
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
                    useCaches = false
                    defaultUseCaches = false
                    setRequestProperty("User-Agent", "AkbarMedia-Android-Client/1.0")
                    setRequestProperty("Accept", "*/*")
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                    connectTimeout = 30000
                    readTimeout = 45000
                }
                connection.connect()

                val code = connection.responseCode
                if ((code in 300..308) && redirectCount < 10) {
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

            val fileLength = if (connection.contentLengthLong > 0) connection.contentLengthLong else (expectedSize ?: -1L)
            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                ?: context.cacheDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            tempFile = File(downloadDir, "${fileName}.tmp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            val destinationFile = File(downloadDir, fileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            inputStream = connection.inputStream
            outputStream = java.io.FileOutputStream(tempFile)

            val data = ByteArray(16384)
            var total: Long = 0
            var count: Int

            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress((total.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f))
                }
                outputStream.write(data, 0, count)
            }
            outputStream.flush()
            outputStream.close()
            outputStream = null
            inputStream.close()
            inputStream = null
            connection.disconnect()

            if (!tempFile.exists() || tempFile.length() < 1000000L) {
                tempFile.delete()
                return@withContext null
            }

            // Verify package integrity using packageManager
            val pm = context.packageManager
            val archiveInfo = pm.getPackageArchiveInfo(tempFile.absolutePath, 0)
            if (archiveInfo == null) {
                android.util.Log.e("DownloadApk", "Downloaded file is not a valid APK package")
                tempFile.delete()
                return@withContext null
            }

            if (tempFile.renameTo(destinationFile)) {
                destinationFile
            } else {
                tempFile.copyTo(destinationFile, overwrite = true)
                tempFile.delete()
                destinationFile
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
    if (!apkFile.exists() || apkFile.length() < 1000000L) {
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
