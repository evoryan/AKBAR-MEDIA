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
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                Text("LAIN-LAIN", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                        HorizontalDivider(color = cardBorder)
                        SettingItem(icon = Icons.Default.Backup, title = "Backup & Restore", subtitle = "Database Pelanggan", iconTint = textMain, onClick = onNavigateToBackupRestore)
                        HorizontalDivider(color = cardBorder)
                        SettingItem(icon = Icons.Default.Receipt, title = "Pengaturan Invoice", subtitle = "Atur text header dan footer invoice (Thermal)", iconTint = textMain, onClick = onNavigateToInvoiceSettings)
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
        val latestVersion = updateInfo!!.tag_name.removePrefix("v")
        val isNewer = latestVersion > (currentVersion ?: "0")
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        
        AlertDialog(
            onDismissRequest = { if (!isDownloading) showUpdateDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (isNewer) "Update Tersedia" else "Sudah Versi Terbaru") },
            text = { 
                Column {
                    Text("Versi saat ini: $currentVersion")
                    Text("Versi terbaru: $latestVersion")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isDownloading) {
                        Text("Mengunduh update... ${(downloadProgress * 100).toInt()}%")
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = primaryBg,
                        )
                    } else {
                        if (isNewer) {
                            Text("Apakah Anda ingin mengunduh versi terbaru?")
                        } else {
                            Text("Apakah Anda ingin mengunduh ulang versi ini sebagai update perbaikan?")
                        }
                    }
                }
            },
            confirmButton = {
                val url = updateInfo!!.assets.firstOrNull()?.browser_download_url
                val downloadAction = {
                    if (url != null) {
                        isDownloading = true
                        coroutineScope.launch {
                            val file = downloadApk(context, url) { progress ->
                                downloadProgress = progress
                            }
                            isDownloading = false
                            if (file != null) {
                                showUpdateDialog = false
                                installApk(context, file)
                            } else {
                                Toast.makeText(context, "Gagal mengunduh update", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Link download tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isNewer) {
                        TextButton(
                            onClick = { downloadAction() },
                            enabled = !isDownloading
                        ) {
                            Text(if (isDownloading) "Mengunduh..." else "Download & Install", color = if (isDownloading) Color.Gray else primaryBg)
                        }
                    } else {
                        TextButton(
                            onClick = { downloadAction() },
                            enabled = !isDownloading
                        ) {
                            Text(if (isDownloading) "Mengunduh..." else "Update-Fix", color = if (isDownloading) Color.Gray else primaryBg)
                        }
                        if (!isDownloading) {
                            TextButton(onClick = { showUpdateDialog = false }) {
                                Text("Tutup", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                            }
                        }
                    }
                }
            },
            dismissButton = {
                if (isNewer && !isDownloading) {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Batal", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                    }
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


suspend fun downloadApk(
    context: android.content.Context,
    url: String,
    onProgress: (Float) -> Unit
): File? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()
            
            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                return@withContext null
            }
            
            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && !downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val apkFile = File(downloadDir, "update.apk")
            
            val input = connection.inputStream
            val output = java.io.FileOutputStream(apkFile)
            
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress((total.toFloat() / fileLength.toFloat()))
                }
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
            
            apkFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun installApk(context: android.content.Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    val apkUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
    )
    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal menginstall update: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
