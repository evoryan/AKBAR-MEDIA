package com.example.ui.screens
import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.border

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(
    onNavigateToCustomers: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToMikrotik: () -> Unit,
    onNavigateToPackages: () -> Unit,
    onNavigateToArea: () -> Unit,
    onNavigateToAcs: () -> Unit,
    onNavigateToBotWa: () -> Unit,
    onNavigateToPembukuan: () -> Unit,
    onNavigateToStockBarang: () -> Unit,
    onNavigateToSetting: () -> Unit,
    onNavigateToJaringan: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by UserSession.currentUser.collectAsState()

    
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLocalDate = java.time.LocalDate.now()
    val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val currentMonthName = monthNames.getOrElse(currentLocalDate.monthValue - 1) { "Agustus" }
    var selectedMonth by remember { mutableStateOf(currentMonthName) }
    var selectedYear by remember { mutableStateOf(currentLocalDate.year.toString()) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (!UserSession.hasCheckedForUpdate) {
                UserSession.hasCheckedForUpdate = true
                try {
                    val currentVersion = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName ?: "1.0.0"
                    val api = com.example.data.GithubApiService.create()
                    val release = api.getLatestRelease("evoryan", "AKBAR-MEDIA")
                    val latestVersion = release.tag_name.removePrefix("v")
                    val currVerNum = currentVersion.removePrefix("v")
                    if (latestVersion != currVerNum) {
                        android.widget.Toast.makeText(mContext, "Update tersedia: ${release.name}", android.widget.Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    // Ignore silent fail
                }
            }
        }

        viewModel.fetchDashboardSummary()
    }

    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = listOf("2023", "2024", "2025", "2026", "2027")

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    val gridSuccessBg = Color(0xFF00FF4D).copy(alpha = 0.1f)
    val gridSuccessBorder = Color(0xFF00FF4D).copy(alpha = 0.3f)
    val iconSuccess = Color(0xFF00FF4D)
    val gridErrorBg = Color(0xFFFF003C).copy(alpha = 0.1f)
    val gridErrorBorder = Color(0xFFFF003C).copy(alpha = 0.3f)
    val iconError = Color(0xFFFF003C)
    val textErrorPrimary = Color(0xFFFF003C)
    val textErrorSecondary = Color(0xFFFF003C).copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgMain)
            .statusBarsPadding()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val mContext = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(com.example.ui.data.SettingsManager.companyName, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        viewModel.fetchDashboardSummary()
                        Toast.makeText(mContext, "Refreshing data...", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Halo, ${currentUser?.name ?: "Admin"}", fontSize = 16.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, cardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), modifier = Modifier.size(32.dp))
            }
        }
        
        // Stack Informasi (Rectangle Shape)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, primaryBg.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = primaryBg, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Informasi Sistem", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("• ${com.example.ui.data.SettingsManager.dashboardInfo1}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                Text("• ${com.example.ui.data.SettingsManager.dashboardInfo2}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
            }
        }

        
        // Filter Bulan & Tahun
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Bulan
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedMonth = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedMonth, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    }
                }
                DropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false },
                    modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)) },
                            onClick = {
                                selectedMonth = month
                                expandedMonth = false
                            }
                        )
                    }
                }
            }
            
            // Dropdown Tahun
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedYear = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedYear, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    }
                }
                DropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false },
                    modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)) },
                            onClick = {
                                selectedYear = year
                                expandedYear = false
                            }
                        )
                    }
                }
            }
        }
        
        // Total Pendapatan (Swipeable)
        val pagerState = rememberPagerState(pageCount = { 2 })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                HorizontalPager(state = pagerState) { page ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryBg.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = primaryBg, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (page == 0) "Total Pendapatan ($selectedMonth $selectedYear)" else "Total Pendapatan Global",
                                fontWeight = FontWeight.Medium, fontSize = 14.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
                            )
                        }
                        
                        when (val state = uiState) {
                            is DashboardState.Loading -> Text("Rp ...", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                            is DashboardState.Success -> {
                                val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"))
                                format.maximumFractionDigits = 0
                                val amount = if (page == 0) state.data.monthlyRevenue else state.data.totalGlobalRevenue
                                Text(format.format(amount), fontWeight = FontWeight.Bold, fontSize = 32.sp, color = primaryBg)
                            }
                            is DashboardState.Error -> Text("-", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { iteration ->
                        val color = if (pagerState.currentPage == iteration) primaryBg else cardBorder
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }
        }

        // Summary Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Active Customers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gridSuccessBg)
                    .border(1.dp, gridSuccessBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = iconSuccess, modifier = Modifier.size(20.dp))
                    Column {
                        when (val state = uiState) {
                            is DashboardState.Loading -> Text("...", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                            is DashboardState.Success -> Text("${state.data.totalCustomers}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                            is DashboardState.Error -> Text("-", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                        }
                        Text("Aktif", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666))
                    }
                }
            }

            // Overdue Payments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gridErrorBg)
                    .border(1.dp, gridErrorBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = iconError, modifier = Modifier.size(20.dp))
                    Column {
                        when (val state = uiState) {
                            is DashboardState.Loading -> Text("...", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textErrorPrimary)
                            is DashboardState.Success -> Text("${state.data.unpaidCustomers}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textErrorPrimary)
                            is DashboardState.Error -> Text("-", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textErrorPrimary)
                        }
                        Text("Belum Bayar", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = textErrorSecondary)
                    }
                }
            }
        }
        
        // Shortcut Menu Cepat
        Text("Akses Cepat", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShortcutItem(Icons.Default.PersonAdd, "Tambah\nPelanggan", primaryBg, modifier = Modifier.weight(1f), onClick = onNavigateToCustomers)
            ShortcutItem(Icons.Default.Payment, "Bayar\nTagihan", gridSuccessBg, modifier = Modifier.weight(1f), onClick = onNavigateToBilling)
            ShortcutItem(Icons.Default.BugReport, "Lapor\nGangguan", gridErrorBg, modifier = Modifier.weight(1f), onClick = {})
        }

        // Menu Utama Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Menu Utama", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), modifier = Modifier.padding(bottom = 16.dp))
                
                val menuItems = mutableListOf<@Composable RowScope.() -> Unit>()
                menuItems.add { MenuItem(icon = Icons.Default.Group, title = "Pelanggan", tint = primaryBg, onClick = onNavigateToCustomers) }
                menuItems.add { MenuItem(icon = Icons.Default.Receipt, title = "Tagihan", tint = textErrorPrimary, onClick = onNavigateToBilling) }
                menuItems.add { MenuItem(icon = Icons.Default.Inventory, title = "Paket", tint = primaryBg, onClick = onNavigateToPackages) }
                menuItems.add { MenuItem(icon = Icons.Default.Map, title = "Area", tint = primaryBg, onClick = onNavigateToArea) }
                menuItems.add { MenuItem(icon = Icons.Default.Router, title = "Mikrotik", tint = primaryBg, onClick = onNavigateToMikrotik) }
                menuItems.add { MenuItem(icon = Icons.Default.Dns, title = "ACS", tint = primaryBg, onClick = onNavigateToAcs) }
                if (currentUser?.role == UserRole.SUPER_ADMIN) {
                    menuItems.add { MenuItem(icon = Icons.Default.Chat, title = "Bot WA", tint = Color(0xFF00FF4D), onClick = onNavigateToBotWa) }
                }
                menuItems.add { MenuItem(icon = Icons.Default.AccountTree, title = "Jaringan", tint = primaryBg, onClick = onNavigateToJaringan) }
                menuItems.add { MenuItem(icon = Icons.Default.Book, title = "Pembukuan", tint = primaryBg, onClick = onNavigateToPembukuan) }
                menuItems.add { MenuItem(icon = Icons.Default.Storage, title = "Stock Barang", tint = primaryBg, onClick = onNavigateToStockBarang) }
                menuItems.add { MenuItem(icon = Icons.Default.Settings, title = "Setting", tint = primaryBg, onClick = onNavigateToSetting) }

                menuItems.chunked(4).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        rowItems.forEach { it() }
                        repeat(4 - rowItems.size) { Box(modifier = Modifier.weight(1f)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // PPPoE Offline Users
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("PPPoE Offline", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    Text("Lihat Semua", fontSize = 12.sp, color = primaryBg, modifier = Modifier.clickable { onNavigateToMikrotik() })
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (uiState is DashboardState.Success) {
                    val offlineUsers = (uiState as DashboardState.Success).offlinePppoe
                    if (offlineUsers.isEmpty()) {
                        Text("Tidak ada PPPoE offline", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 14.sp)
                    } else {
                        offlineUsers.take(5).forEach { user ->
                            OfflineUserItem(user.name, user.lastLogoff)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Extra padding for floating nav bar
    }
}

@Composable
fun RowScope.MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, tint: Color, onClick: () -> Unit) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
    val iconBgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF0F0F0)
    val iconBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBgColor)
                .border(1.dp, iconBorderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 10.sp, color = if (isDark) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ShortcutItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if(androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF))
            .border(1.dp, if(androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun OfflineUserItem(username: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF003C).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color(0xFFFF003C), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(username, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(time, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
    }
}
