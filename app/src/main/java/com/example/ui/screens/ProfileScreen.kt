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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserRole
import com.example.ui.data.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)

    val currentUser by UserSession.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Profil Pengguna", color = textMain, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header with Avatar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(primaryBg.copy(alpha = 0.15f))
                            .border(3.dp, primaryBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.name ?: "A").take(1).uppercase(),
                            color = primaryBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentUser?.name ?: "Pengguna",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    val roleLabel = currentUser?.role?.name ?: "ADMIN"
                    val roleColor = when (currentUser?.role) {
                        UserRole.SUPER_ADMIN -> Color(0xFFFF3B30)
                        UserRole.ADMIN -> Color(0xFF007AFF)
                        UserRole.TEKNISI -> Color(0xFFFF9500)
                        UserRole.COLLECTOR -> Color(0xFFAF52DE)
                        else -> primaryBg
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(roleColor.copy(alpha = 0.12f))
                            .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            color = roleColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Profile Details Section
            Text(
                text = "Detail Informasi",
                color = textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ProfileItemRow(
                        icon = Icons.Default.Person,
                        label = "Username",
                        value = currentUser?.username ?: "-",
                        tint = textSecondary,
                        textMain = textMain,
                        textSecondary = textSecondary
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = cardBorder)
                    ProfileItemRow(
                        icon = Icons.Default.Lock,
                        label = "Hak Akses (Role)",
                        value = currentUser?.role?.name ?: "ADMIN",
                        tint = textSecondary,
                        textMain = textMain,
                        textSecondary = textSecondary
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = cardBorder)
                    ProfileItemRow(
                        icon = Icons.Default.Info,
                        label = "ID Pengguna",
                        value = currentUser?.id ?: "-",
                        tint = textSecondary,
                        textMain = textMain,
                        textSecondary = textSecondary
                    )
                    currentUser?.db_name?.let { db ->
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = cardBorder)
                        ProfileItemRow(
                            icon = Icons.Default.Storage,
                            label = "Database",
                            value = db,
                            tint = textSecondary,
                            textMain = textMain,
                            textSecondary = textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = onNavigateToEditProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBg,
                    contentColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profil Lengkap", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF3B30)
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = cardBg,
            title = { Text("Konfirmasi Logout", color = textMain, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?", color = textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Ya, Keluar", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = primaryBg)
                }
            }
        )
    }
}

@Composable
fun ProfileItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    textMain: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(text = label, fontSize = 12.sp, color = textSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 15.sp, color = textMain, fontWeight = FontWeight.SemiBold)
        }
    }
}
