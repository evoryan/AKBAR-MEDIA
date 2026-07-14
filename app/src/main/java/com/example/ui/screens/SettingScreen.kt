package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    onNavigateToGatewayPayment: () -> Unit,
    onNavigateToCompanySettings: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
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
                title = { Text("Setting", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
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
                    
                    if (currentUser?.role == UserRole.SUPER_ADMIN) { HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.Group, title = "Daftar Admin", subtitle = "Kelola akun admin/teknisi/collector", iconTint = textMain, onClick = onNavigateToDaftarAdmin) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TEMA
            Text("TEMA APLIKASI", color = primaryBg, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
            var showThemeDialog by remember { mutableStateOf(false) }
            var currentTheme by remember { mutableStateOf(com.example.ui.data.SettingsManager.appTheme) }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            ) {
                SettingItem(
                    icon = Icons.Default.Palette, 
                    title = "Tema Aplikasi", 
                    subtitle = currentTheme, 
                    iconTint = textMain, 
                    onClick = { showThemeDialog = true }
                )
            }
            
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Pilih Tema", color = textMain) },
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
                                    Text(themeName, color = textMain)
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
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
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
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color(0xFFAAAAAA), fontSize = 12.sp)
            }
        }
    }
}
