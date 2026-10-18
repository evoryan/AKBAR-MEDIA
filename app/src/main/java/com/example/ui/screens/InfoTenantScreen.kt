package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.ui.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoTenantScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF1F5F9)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF0F172A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0265DC)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFCBD5E1)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF475569)

    val currentUser by com.example.ui.data.UserSession.currentUser.collectAsState()
    val isSuperAdmin = currentUser?.role == com.example.ui.data.UserRole.SUPER_ADMIN
    val currentDb = currentUser?.db_name?.takeIf { it.isNotBlank() } ?: "tenant_default"

    var newInfoText by remember { mutableStateOf("") }
    val infoList by SettingsManager.tenantInfosFlow.collectAsState()
    val globalAnnouncements by SettingsManager.globalAnnouncementsFlow.collectAsState()
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(currentDb) {
        SettingsManager.reloadTenantInfosForActiveDatabase(currentDb)
        SettingsManager.syncSettingsFromRemote(context)
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (isSuperAdmin) "Kelola Info Tenant" else "Data Info Tenant", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Text("Database: $currentDb", color = textSecondary, fontSize = 11.sp)
                    }
                },
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Database Scope Status Tag
            Surface(
                color = cardBg,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSuperAdmin) Color(0xFF10B981) else primaryBg)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terisolasi pada Database: $currentDb",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textMain
                        )
                    }
                    Text(
                        text = if (isSuperAdmin) "Akses: Super Admin" else "Akses: Lihat Semua Admin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSuperAdmin) primaryBg else textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSuperAdmin) {
                // Form to Add Info (Only for Super Admin)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tambah Informasi Tenant Baru",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newInfoText,
                            onValueChange = {
                                newInfoText = it
                                showError = false
                            },
                            placeholder = { Text("Tulis pengumuman / info tenant untuk seluruh admin di database $currentDb...", color = textSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            )
                        )
                        if (showError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Teks informasi tidak boleh kosong", color = Color.Red, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val text = newInfoText.trim()
                                if (text.isNotEmpty()) {
                                    val updatedList = infoList.toMutableList().apply {
                                        add(0, text) // Add at the top
                                    }
                                    SettingsManager.setTenantInfosForDb(updatedList, currentDb)
                                    SettingsManager.lastNotifiedInfo = text
                                    newInfoText = ""
                                    val authorTitle = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Super Admin"
                                    com.example.ui.data.NotificationHelper.showNotification(
                                        context = context,
                                        title = "Informasi Baru dari $authorTitle",
                                        body = text
                                    )
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryBg, contentColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tambah Informasi ($currentDb)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = primaryBg, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Informasi & pengumuman resmi yang dibuat oleh akun Super Admin untuk seluruh admin dan tim dalam database $currentDb.",
                            color = textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Daftar Informasi Aktif (Database: $currentDb)",
                color = textMain,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (infoList.isEmpty() && globalAnnouncements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada informasi tenant dari Super Admin untuk database $currentDb.", color = textSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // PEMBERITAHUAN GLOBAL DARI MASTER AKBAR2026 (WARNA FONT KHUSUS)
                    items(globalAnnouncements) { broadcast ->
                        val isDarkTheme = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isDarkTheme) Color(0xFFF59E0B) else Color(0xFFD97706),
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("📢", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = broadcast.title.ifBlank { "PENGUMUMAN PUSAT (MASTER)" },
                                            color = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFB45309), // Distinct Amber / Gold font color
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Surface(
                                            color = if (isDarkTheme) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFFD97706).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = broadcast.priority,
                                                color = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = broadcast.message,
                                        color = if (isDarkTheme) Color(0xFFFCD34D) else Color(0xFF78350F), // Distinct font color!
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Berlaku untuk seluruh database • Pengirim: ${broadcast.author}",
                                        color = if (isDarkTheme) Color(0xFFFDE68A) else Color(0xFF92400E),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    items(infoList) { infoItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(primaryBg)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = infoItem,
                                        color = textMain,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Dibuat oleh Super Admin • Database: $currentDb",
                                        color = primaryBg.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                if (isSuperAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val updatedList = infoList.toMutableList().apply {
                                                remove(infoItem)
                                            }
                                            SettingsManager.setTenantInfosForDb(updatedList, currentDb)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
