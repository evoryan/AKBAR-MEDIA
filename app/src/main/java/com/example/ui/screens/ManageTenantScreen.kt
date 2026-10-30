package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.remote.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTenantScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val bgMain = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
    val textMain = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val primaryColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0265DC)
    val successColor = if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
    val errorColor = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    val warningColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val purpleColor = if (isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)

    val currentUser by UserSession.currentUser.collectAsState()
    val isAuthorized = currentUser?.username == "akbar2026"

    // If not authorized as akbar2026, show restricted access screen
    if (!isAuthorized) {
        Scaffold(
            containerColor = bgMain,
            topBar = {
                TopAppBar(
                    title = { Text("Akses Terbatas", color = textMain, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(errorColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = errorColor, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Akses Khusus Admin Master", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Halaman ini khusus untuk manajemen sistem tenant database (manage_tenant.sh) dan hanya dapat diakses oleh akun admin master dengan username 'akbar2026'.",
                            color = textSecondary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kembali ke Pengaturan")
                        }
                    }
                }
            }
        }
        return
    }

    // State
    var selectedTab by remember { mutableIntStateOf(0) }
    var tenantsList by remember { mutableStateOf<List<TenantItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("SEMUA") } // SEMUA, ACTIVE, DISABLED, DEMO

    // Dialog states
    var showStatsDialog by remember { mutableStateOf<TenantItem?>(null) }
    var statsData by remember { mutableStateOf<TenantStatsResponse?>(null) }
    var isStatsLoading by remember { mutableStateOf(false) }

    var showResetPasswordDialog by remember { mutableStateOf<TenantItem?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isResetSubmitting by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf<TenantItem?>(null) }
    var deleteConfirmationInput by remember { mutableStateOf("") }
    var isDeleteSubmitting by remember { mutableStateOf(false) }

    var showStatusConfirmDialog by remember { mutableStateOf<Pair<TenantItem, String>?>(null) } // pair of tenant and target action ("disable", "enable", "toggle-demo")
    var isStatusSubmitting by remember { mutableStateOf(false) }

    var operationResultMsg by remember { mutableStateOf<String?>(null) }

    // Form Tambah Tenant Baru
    var newDbName by remember { mutableStateOf("") }
    var newTenantName by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }
    var newIsDemo by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var isAddSubmitting by remember { mutableStateOf(false) }

    // Maintenance / DB Schema & Backup
    var updateTargetTenant by remember { mutableStateOf("all") }
    var isUpdatingDb by remember { mutableStateOf(false) }
    var updateDbLogs by remember { mutableStateOf<String?>(null) }

    var backupDbTarget by remember { mutableStateOf("") }
    var isBackingUp by remember { mutableStateOf(false) }

    var restoreDbTarget by remember { mutableStateOf("") }
    var restoreSqlPath by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    // Pemberitahuan Global / Broadcast ke Seluruh User & DB
    var broadcastTitle by remember { mutableStateOf("PENGUMUMAN PUSAT (MASTER)") }
    var broadcastMessage by remember { mutableStateOf("") }
    var broadcastPriority by remember { mutableStateOf("PENTING") }
    var isBroadcastSubmitting by remember { mutableStateOf(false) }
    var broadcastsList by remember { mutableStateOf<List<GlobalAnnouncement>>(com.example.ui.data.SettingsManager.globalAnnouncements) }
    var isBroadcastLoading by remember { mutableStateOf(false) }
    var broadcastToDelete by remember { mutableStateOf<GlobalAnnouncement?>(null) }
    var isDeletingBroadcast by remember { mutableStateOf(false) }

    fun loadBroadcasts() {
        coroutineScope.launch {
            isBroadcastLoading = true
            try {
                // 1. Coba endpoint khusus broadcast
                val list = try {
                    withContext(Dispatchers.IO) {
                        ApiClient.apiService.getGlobalBroadcasts()
                    }
                } catch (_: Exception) {
                    // 2. Fallback: ambil dari app_settings MySQL yang sudah aktif di server
                    try {
                        val settings = withContext(Dispatchers.IO) {
                            ApiClient.apiService.getAppSettings()
                        }
                        val jsonStr = settings["global_master_announcements"] ?: "[]"
                        Json.decodeFromString<List<GlobalAnnouncement>>(jsonStr)
                    } catch (_: Exception) {
                        com.example.ui.data.SettingsManager.globalAnnouncements
                    }
                }
                broadcastsList = list
                com.example.ui.data.SettingsManager.globalAnnouncements = list
            } catch (e: Exception) {
                broadcastsList = com.example.ui.data.SettingsManager.globalAnnouncements
            } finally {
                isBroadcastLoading = false
            }
        }
    }

    // Function to load tenants from server
    fun loadTenants() {
        coroutineScope.launch {
            isLoading = true
            try {
                val list = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTenantsList()
                }
                tenantsList = list
                if (backupDbTarget.isEmpty() && list.isNotEmpty()) {
                    backupDbTarget = list.first().db_name
                    restoreDbTarget = list.first().db_name
                }
            } catch (e: Exception) {
                // If API is unreachable, provide informative toast
                Toast.makeText(context, "Koneksi ke API: ${e.localizedMessage ?: "Gagal memuat"}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTenants()
        loadBroadcasts()
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Kelola Tenant",
                                color = textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = primaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Master Panel",
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "manage_tenant.sh CLI Control • akbar2026",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        loadTenants()
                        loadBroadcasts()
                    }, enabled = !isLoading && !isBroadcastLoading) {
                        if (isLoading || isBroadcastLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = primaryColor)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = textMain)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs navigation (Daftar, Tambah Baru, Pemberitahuan, Skema DB, Backup/Restore)
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = cardBg,
                contentColor = primaryColor,
                divider = { HorizontalDivider(color = cardBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daftar (${tenantsList.size})", fontSize = 13.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tambah Baru", fontSize = 13.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        loadBroadcasts()
                    },
                    text = { Text("Pemberitahuan", fontSize = 13.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Skema DB", fontSize = 13.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Backup/Restore", fontSize = 13.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // TAB 0: DAFTAR TENANT
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Summary Cards Row
                        val totalTenants = tenantsList.size
                        val activeTenants = tenantsList.count { it.status == "ACTIVE" }
                        val demoTenants = tenantsList.count { it.is_demo == 1 }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                modifier = Modifier.weight(1f),
                                label = "Total Tenant",
                                value = "$totalTenants",
                                color = primaryColor,
                                bg = cardBg,
                                border = cardBorder
                            )
                            StatBox(
                                modifier = Modifier.weight(1f),
                                label = "Aktif",
                                value = "$activeTenants",
                                color = successColor,
                                bg = cardBg,
                                border = cardBorder
                            )
                            StatBox(
                                modifier = Modifier.weight(1f),
                                label = "Demo Mode",
                                value = "$demoTenants",
                                color = purpleColor,
                                bg = cardBg,
                                border = cardBorder
                            )
                        }

                        // Search and Filter Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari tenant, username, DB...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = cardBorder
                                )
                            )
                        }

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("SEMUA", "ACTIVE", "DISABLED", "DEMO").forEach { filterKey ->
                                FilterChip(
                                    selected = filterStatus == filterKey,
                                    onClick = { filterStatus = filterKey },
                                    label = { Text(filterKey, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = cardBg,
                                        labelColor = textSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = filterStatus == filterKey,
                                        borderColor = cardBorder,
                                        selectedBorderColor = primaryColor
                                    )
                                )
                            }
                        }

                        // Filtered List
                        val filteredList = tenantsList.filter { item ->
                            val matchQuery = searchQuery.isBlank() ||
                                item.name.contains(searchQuery, ignoreCase = true) ||
                                item.username.contains(searchQuery, ignoreCase = true) ||
                                item.db_name.contains(searchQuery, ignoreCase = true)

                            val matchFilter = when (filterStatus) {
                                "ACTIVE" -> item.status == "ACTIVE"
                                "DISABLED" -> item.status == "DISABLED"
                                "DEMO" -> item.is_demo == 1
                                else -> true
                            }
                            matchQuery && matchFilter
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = textSecondary, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (tenantsList.isEmpty()) "Belum ada tenant terdaftar atau server offline" else "Tidak ada tenant yang sesuai filter",
                                        color = textSecondary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { loadTenants() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Muat Ulang Data")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(filteredList, key = { it.id }) { tenant ->
                                    TenantCard(
                                        tenant = tenant,
                                        cardBg = cardBg,
                                        cardBorder = cardBorder,
                                        textMain = textMain,
                                        textSecondary = textSecondary,
                                        primaryColor = primaryColor,
                                        successColor = successColor,
                                        errorColor = errorColor,
                                        warningColor = warningColor,
                                        purpleColor = purpleColor,
                                        onStatsClick = {
                                            showStatsDialog = tenant
                                            isStatsLoading = true
                                            coroutineScope.launch {
                                                try {
                                                    val res = withContext(Dispatchers.IO) {
                                                        ApiClient.apiService.getTenantStats(tenant.username)
                                                    }
                                                    statsData = res
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Gagal memuat statistik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isStatsLoading = false
                                                }
                                            }
                                        },
                                        onResetPasswordClick = {
                                            showResetPasswordDialog = tenant
                                            newPasswordInput = ""
                                            confirmPasswordInput = ""
                                        },
                                        onToggleStatusClick = {
                                            val action = if (tenant.status == "ACTIVE") "disable" else "enable"
                                            showStatusConfirmDialog = Pair(tenant, action)
                                        },
                                        onToggleDemoClick = {
                                            showStatusConfirmDialog = Pair(tenant, "toggle-demo")
                                        },
                                        onDeleteClick = {
                                            showDeleteDialog = tenant
                                            deleteConfirmationInput = ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: TAMBAH TENANT BARU (manage_tenant.sh Menu 2)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AddBusiness, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Pendaftaran Tenant Baru", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Otomatis membuat DB MySQL, skema init.sql & Superadmin", color = textSecondary, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 1. Nama Database
                                Text("1. Nama Database Tenant *", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = newDbName,
                                    onValueChange = { newDbName = it.trim().replace(" ", "_") },
                                    placeholder = { Text("contoh: akbar_media_client2", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Text("Hanya huruf, angka, dan underscore (_). Database terisolasi penuh.", color = textSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. Nama Lengkap Admin / Tenant
                                Text("2. Nama Lengkap ISP / Tenant *", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = newTenantName,
                                    onValueChange = { newTenantName = it },
                                    placeholder = { Text("contoh: ISP Maju Bersama", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 3. Username Login Superadmin
                                Text("3. Username Login Superadmin *", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = newUsername,
                                    onValueChange = { newUsername = it.trim().replace(" ", "") },
                                    placeholder = { Text("contoh: admin_maju", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 4. Password Login Superadmin
                                Text("4. Password Login Superadmin *", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    placeholder = { Text("Password Superadmin", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                            Icon(if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Konfirmasi Password
                                Text("Konfirmasi Password *", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = newPasswordConfirm,
                                    onValueChange = { newPasswordConfirm = it },
                                    placeholder = { Text("Ulangi Password", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    isError = newPasswordConfirm.isNotEmpty() && newPassword != newPasswordConfirm
                                )
                                if (newPasswordConfirm.isNotEmpty() && newPassword != newPasswordConfirm) {
                                    Text("Password konfirmasi tidak cocok!", color = errorColor, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 5. Mode Demo Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (newIsDemo) purpleColor.copy(alpha = 0.12f) else bgMain)
                                        .border(1.dp, if (newIsDemo) purpleColor else cardBorder, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            if (newIsDemo) "Mode DEMO (Aktif)" else "Mode PRODUKSI",
                                            color = if (newIsDemo) purpleColor else textMain,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            if (newIsDemo) "Tenant ditandai sebagai versi uji coba / demo" else "Tenant beroperasi untuk operasional resmi klien",
                                            color = textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Switch(
                                        checked = newIsDemo,
                                        onCheckedChange = { newIsDemo = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = purpleColor,
                                            checkedTrackColor = purpleColor.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Submit Button
                                Button(
                                    onClick = {
                                        if (newDbName.isBlank() || newTenantName.isBlank() || newUsername.isBlank() || newPassword.isBlank()) {
                                            Toast.makeText(context, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (newPassword != newPasswordConfirm) {
                                            Toast.makeText(context, "Password konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            isAddSubmitting = true
                                            try {
                                                val req = CreateTenantRequest(
                                                    db_name = newDbName,
                                                    name = newTenantName,
                                                    username = newUsername,
                                                    password = newPassword,
                                                    is_demo = newIsDemo
                                                )
                                                val res = withContext(Dispatchers.IO) {
                                                    ApiClient.apiService.createTenant(req)
                                                }
                                                operationResultMsg = res.message
                                                // Reset form
                                                newDbName = ""
                                                newTenantName = ""
                                                newUsername = ""
                                                newPassword = ""
                                                newPasswordConfirm = ""
                                                newIsDemo = false
                                                loadTenants()
                                            } catch (e: Exception) {
                                                operationResultMsg = "Error: ${e.localizedMessage ?: "Gagal membuat tenant"}"
                                            } finally {
                                                isAddSubmitting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    enabled = !isAddSubmitting
                                ) {
                                    if (isAddSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sedang Membuat Database & Akun...")
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Buat Tenant Baru", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: FORM PEMBERITAHUAN KEPADA SELURUH USER & DATABASE (MASTER BROADCAST)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF78350F).copy(alpha = 0.25f) else Color(0xFFFEF3C7)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706))
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isDark) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFFFDE68A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📢", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Siaran Pemberitahuan ke Seluruh User & DB",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Pemberitahuan yang dibuat di sini akan disinkronkan secara global ke SEMUA database tenant dan langsung ditampilkan pada Card Info Tenant di dashboard serta layar Info Tenant tiap user dengan warna font emas/amber khusus yang mencolok.",
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFFFDE68A) else Color(0xFF78350F),
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        // Form Tambah Pemberitahuan Baru
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "Buat Pemberitahuan Baru",
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Judul
                                OutlinedTextField(
                                    value = broadcastTitle,
                                    onValueChange = { broadcastTitle = it },
                                    label = { Text("Judul Pengumuman", fontSize = 13.sp) },
                                    placeholder = { Text("Misal: PENGUMUMAN PUSAT, PERAWATAN SISTEM, dll.", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = cardBorder,
                                        focusedTextColor = textMain,
                                        unfocusedTextColor = textMain
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Tingkat Prioritas
                                Text(
                                    "Tingkat Prioritas:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val priorities = listOf("PENTING", "INFO", "DARURAT", "UMUM")
                                    priorities.forEach { p ->
                                        val isSelected = broadcastPriority == p
                                        val badgeColor = when (p) {
                                            "DARURAT" -> errorColor
                                            "PENTING" -> warningColor
                                            "INFO" -> primaryColor
                                            else -> successColor
                                        }
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { broadcastPriority = p },
                                            color = if (isSelected) badgeColor.copy(alpha = 0.2f) else cardBg,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) badgeColor else cardBorder
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = p,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) badgeColor else textSecondary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Pesan
                                OutlinedTextField(
                                    value = broadcastMessage,
                                    onValueChange = { broadcastMessage = it },
                                    label = { Text("Isi Pesan Pemberitahuan (Wajib)", fontSize = 13.sp) },
                                    placeholder = { Text("Tulis pesan yang akan disiarkan ke seluruh admin & database tenant di sini...", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 6,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = cardBorder,
                                        focusedTextColor = textMain,
                                        unfocusedTextColor = textMain
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pratinjau Tampilan di Card Info Tenant
                                Text(
                                    "Pratinjau Tampilan di Card Info Tenant:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("📢 ", fontSize = 13.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "[PUSAT] ${broadcastMessage.ifBlank { "Teks isi pesan pemberitahuan akan tampil di sini..." }}",
                                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${broadcastTitle.ifBlank { "PENGUMUMAN PUSAT" }} ($broadcastPriority) • Pengirim: akbar2026",
                                                color = if (isDark) Color(0xFFFCD34D) else Color(0xFF92400E),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Tombol Kirim
                                Button(
                                    onClick = {
                                        if (broadcastMessage.isBlank()) {
                                            Toast.makeText(context, "Isi pesan tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        coroutineScope.launch {
                                            isBroadcastSubmitting = true
                                            try {
                                                val cleanTitle = broadcastTitle.trim().ifBlank { "PENGUMUMAN PUSAT (MASTER)" }
                                                val cleanMsg = broadcastMessage.trim()
                                                val simpleDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                                                val newAnnouncement = GlobalAnnouncement(
                                                    id = System.currentTimeMillis().toString(),
                                                    title = cleanTitle,
                                                    message = cleanMsg,
                                                    priority = broadcastPriority,
                                                    author = "akbar2026",
                                                    created_at = simpleDate
                                                )

                                                var success = false
                                                var respMessage = "Pemberitahuan berhasil dikirimkan ke seluruh database dan pengguna aplikasi!"

                                                // 1. Coba endpoint khusus broadcast
                                                try {
                                                    val req = BroadcastRequest(
                                                        title = cleanTitle,
                                                        message = cleanMsg,
                                                        priority = broadcastPriority
                                                    )
                                                    val resp = withContext(Dispatchers.IO) {
                                                        ApiClient.apiService.sendGlobalBroadcast(req)
                                                    }
                                                    respMessage = resp.message ?: respMessage
                                                    success = true
                                                } catch (_: Exception) {
                                                    // 2. Fallback otomatis jika 404: Simpan langsung ke app_settings global_master_announcements
                                                    try {
                                                        val currentList = broadcastsList.toMutableList()
                                                        currentList.removeAll { it.id == newAnnouncement.id }
                                                        currentList.add(0, newAnnouncement)
                                                        val jsonStr = Json.encodeToString(currentList)
                                                        withContext(Dispatchers.IO) {
                                                            ApiClient.apiService.saveAppSetting("global_master_announcements", mapOf("value" to jsonStr))
                                                        }
                                                        broadcastsList = currentList
                                                        com.example.ui.data.SettingsManager.globalAnnouncements = currentList
                                                        success = true
                                                    } catch (dbErr: Exception) {
                                                        throw dbErr
                                                    }
                                                }

                                                if (success) {
                                                    operationResultMsg = "✅ BERHASIL DISIARKAN!\n\n$respMessage"
                                                    broadcastMessage = ""
                                                    loadBroadcasts()
                                                    com.example.ui.data.SettingsManager.syncSettingsFromRemote(context)
                                                }
                                            } catch (e: Exception) {
                                                operationResultMsg = "❌ GAGAL MENGIRIM PEMBERITAHUAN:\n${e.localizedMessage ?: e.message}"
                                            } finally {
                                                isBroadcastSubmitting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)),
                                    enabled = !isBroadcastSubmitting && broadcastMessage.isNotBlank()
                                ) {
                                    if (isBroadcastSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Menyiarkan ke Seluruh Database...", color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Kirim Pemberitahuan ke Seluruh DB & User", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Daftar Pemberitahuan Aktif
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Daftar Pemberitahuan Aktif (${broadcastsList.size})",
                                color = textMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            IconButton(onClick = { loadBroadcasts() }, enabled = !isBroadcastLoading) {
                                if (isBroadcastLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = primaryColor, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = textSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (broadcastsList.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = textSecondary, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Belum ada pemberitahuan siaran aktif", color = textSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            broadcastsList.forEach { b ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (isDark) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFFD97706).copy(alpha = 0.4f)
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("📢", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = b.title.ifBlank { "PENGUMUMAN PUSAT" },
                                                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                                Surface(
                                                    color = when (b.priority) {
                                                        "DARURAT" -> errorColor.copy(alpha = 0.15f)
                                                        "PENTING" -> warningColor.copy(alpha = 0.15f)
                                                        "INFO" -> primaryColor.copy(alpha = 0.15f)
                                                        else -> successColor.copy(alpha = 0.15f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = b.priority,
                                                        color = when (b.priority) {
                                                            "DARURAT" -> errorColor
                                                            "PENTING" -> warningColor
                                                            "INFO" -> primaryColor
                                                            else -> successColor
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = b.message,
                                                color = if (isDark) Color(0xFFFCD34D) else Color(0xFF78350F),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Pengirim: ${b.author}${if (!b.created_at.isNullOrBlank()) " • ${b.created_at}" else ""}",
                                                    color = textSecondary,
                                                    fontSize = 10.sp
                                                )
                                                IconButton(
                                                    onClick = { broadcastToDelete = b },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Hapus",
                                                        tint = errorColor,
                                                        modifier = Modifier.size(16.dp)
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

                3 -> {
                    // TAB 3: UPDATE DATABASE / SCHEMA INIT.SQL TERBARU (manage_tenant.sh Menu 10)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.SyncProblem, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Update Database Sesuai init.sql", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Sinkronkan struktur tabel Room DB & MySQL", color = textSecondary, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    color = primaryColor.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(primaryColor.copy(alpha = 0.2f)))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Fitur sinkronisasi skema:", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "• Menambahkan tabel: odc_list, odp_list, rasio, status_router_terakhir, gangguan\n" +
                                            "• Menyiapkan VIEW pelanggan & tagihan untuk Room DB\n" +
                                            "• Menambahkan kolom: additional_cost1/2, pppoe_secret, odp_id, dsb.\n" +
                                            "• Data pelanggan, tagihan, dan transaksi lama tetap aman.",
                                            color = textSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Pilih Target Update:", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { updateTargetTenant = "all" },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (updateTargetTenant == "all") primaryColor.copy(alpha = 0.15f) else Color.Transparent
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(if (updateTargetTenant == "all") primaryColor else cardBorder)
                                        )
                                    ) {
                                        Text("Semua Tenant", color = if (updateTargetTenant == "all") primaryColor else textSecondary, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (tenantsList.isNotEmpty()) {
                                                updateTargetTenant = tenantsList.first().db_name
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (updateTargetTenant != "all") primaryColor.copy(alpha = 0.15f) else Color.Transparent
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(if (updateTargetTenant != "all") primaryColor else cardBorder)
                                        )
                                    ) {
                                        Text("Satu Tenant", color = if (updateTargetTenant != "all") primaryColor else textSecondary, fontSize = 12.sp)
                                    }
                                }

                                if (updateTargetTenant != "all") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Pilih Database Tenant:", color = textSecondary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = updateTargetTenant,
                                        onValueChange = { updateTargetTenant = it.trim() },
                                        placeholder = { Text("Nama database atau username tenant", fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isUpdatingDb = true
                                            updateDbLogs = null
                                            try {
                                                val res = withContext(Dispatchers.IO) {
                                                    ApiClient.apiService.updateTenantDatabases(UpdateDbRequest(target = updateTargetTenant))
                                                }
                                                operationResultMsg = res.message
                                                updateDbLogs = res.details ?: res.message
                                                loadTenants()
                                            } catch (e: Exception) {
                                                operationResultMsg = "Error: ${e.localizedMessage ?: "Gagal update skema"}"
                                            } finally {
                                                isUpdatingDb = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    enabled = !isUpdatingDb
                                ) {
                                    if (isUpdatingDb) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Menyelaraskan Skema...")
                                    } else {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (updateTargetTenant == "all") "Update SEMUA Tenant Sekarang" else "Update Database: $updateTargetTenant", fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (updateDbLogs != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Log Eksekusi:", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDark) Color(0xFF0A0F1D) else Color(0xFFE2E8F0))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            updateDbLogs ?: "",
                                            color = if (isDark) Color(0xFF67E8F9) else Color(0xFF0369A1),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: BACKUP & RESTORE DATABASE TENANT (manage_tenant.sh Menu 8 & 9)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section Backup
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(successColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = successColor, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Backup Database Tenant", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Export SQL dump database tenant ke VPS", color = textSecondary, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text("Nama Database Tenant Target:", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = backupDbTarget,
                                    onValueChange = { backupDbTarget = it.trim() },
                                    placeholder = { Text("contoh: akbar_media_client2", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (backupDbTarget.isBlank()) {
                                            Toast.makeText(context, "Nama database wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        coroutineScope.launch {
                                            isBackingUp = true
                                            try {
                                                val res = withContext(Dispatchers.IO) {
                                                    ApiClient.apiService.backupTenantDatabase(BackupTenantRequest(db_name = backupDbTarget))
                                                }
                                                operationResultMsg = "${res.message}\n${res.details ?: ""}"
                                            } catch (e: Exception) {
                                                operationResultMsg = "Gagal backup: ${e.localizedMessage ?: "Error"}"
                                            } finally {
                                                isBackingUp = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = successColor),
                                    enabled = !isBackingUp
                                ) {
                                    if (isBackingUp) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sedang Membuat Backup SQL...")
                                    } else {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Eksekusi Backup SQL", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Section Restore
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(warningColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = null, tint = warningColor, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Restore Database Tenant", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Import dump SQL ke database tenant", color = textSecondary, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    color = errorColor.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(errorColor.copy(alpha = 0.2f)))
                                ) {
                                    Text(
                                        "⚠️ PERINGATAN: Data pada database tujuan akan ditimpa dengan data dari file SQL yang dipilih. Pastikan Anda telah memiliki salinan cadangan.",
                                        color = errorColor,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Nama Database Tujuan Restore:", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = restoreDbTarget,
                                    onValueChange = { restoreDbTarget = it.trim() },
                                    placeholder = { Text("contoh: ion_network", fontSize = 13.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Path File SQL Dump (.sql):", color = textMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = restoreSqlPath,
                                    onValueChange = { restoreSqlPath = it.trim() },
                                    placeholder = { Text("contoh: backups/ion_network_backup.sql", fontSize = 13.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (restoreDbTarget.isBlank() || restoreSqlPath.isBlank()) {
                                            Toast.makeText(context, "Database tujuan dan path SQL wajib diisi!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        coroutineScope.launch {
                                            isRestoring = true
                                            try {
                                                val res = withContext(Dispatchers.IO) {
                                                    ApiClient.apiService.restoreTenantDatabase(
                                                        RestoreTenantRequest(db_name = restoreDbTarget, sql_path = restoreSqlPath)
                                                    )
                                                }
                                                operationResultMsg = res.message
                                                loadTenants()
                                            } catch (e: Exception) {
                                                operationResultMsg = "Gagal restore: ${e.localizedMessage ?: "Error"}"
                                            } finally {
                                                isRestoring = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = warningColor),
                                    enabled = !isRestoring
                                ) {
                                    if (isRestoring) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sedang Merestore Database...")
                                    } else {
                                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Proses Restore Database", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: Detail & Statistik Tenant (manage_tenant.sh Menu 7)
    if (showStatsDialog != null) {
        val t = showStatsDialog!!
        var showPasswordInStats by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showStatsDialog = null
                statsData = null
            },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Statistik & Detail Tenant", color = textMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                if (isStatsLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow("Nama Tenant", t.name, textMain, textSecondary)
                        DetailRow("Username", t.username, textMain, textSecondary)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Password Superadmin", color = textSecondary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (showPasswordInStats) (t.password?.takeIf { it.isNotBlank() } ?: "(kosong)") else "••••••••",
                                    color = textMain,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { showPasswordInStats = !showPasswordInStats },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showPasswordInStats) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Lihat Password",
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        DetailRow("Database", t.db_name, textMain, textSecondary)
                        DetailRow("Status Akun", t.status, if (t.status == "ACTIVE") successColor else errorColor, textSecondary)
                        DetailRow("Mode", if (t.is_demo == 1) "DEMO (Uji Coba)" else "PRODUKSI", if (t.is_demo == 1) purpleColor else primaryColor, textSecondary)
                        DetailRow("Database", t.db_exists ?: "✅ Ada", textMain, textSecondary)

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 4.dp))

                        val stats = statsData
                        if (stats != null) {
                            Text("Statistik Data Database:", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            DetailRow("👥 Total Pelanggan", "${stats.total_customers}", textMain, textSecondary)
                            DetailRow("🌐 Total Area / Router", "${stats.total_areas}", textMain, textSecondary)
                            DetailRow("💰 Total Transaksi", "${stats.total_transactions}", textMain, textSecondary)
                            DetailRow("📄 Total Tagihan", "${stats.total_invoices}", textMain, textSecondary)
                            DetailRow("👤 Total User Akun", "${stats.total_users}", textMain, textSecondary)
                        } else {
                            Text("Total Pelanggan Terdaftar: ${t.total_customers ?: "0"}", color = textSecondary, fontSize = 13.sp)
                            Text("Total Akun User App: ${t.total_users ?: "0"}", color = textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showStatsDialog = null
                    statsData = null
                }) {
                    Text("Tutup", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // DIALOG: Reset Password Superadmin (manage_tenant.sh Menu 3)
    if (showResetPasswordDialog != null) {
        val t = showResetPasswordDialog!!
        var showPass by remember { mutableStateOf(false) }
        var showCurrentPass by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isResetSubmitting) showResetPasswordDialog = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = warningColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubah / Reset Password Superadmin", color = textMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ubah password superadmin untuk tenant '${t.name}' (username: ${t.username}).", color = textSecondary, fontSize = 13.sp)

                    Surface(
                        color = primaryColor.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Password Saat Ini:", color = textSecondary, fontSize = 11.sp)
                                Text(
                                    text = if (showCurrentPass) (t.password?.takeIf { it.isNotBlank() } ?: "(kosong)") else "••••••••",
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(
                                onClick = { showCurrentPass = !showCurrentPass },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (showCurrentPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Tampilkan Password Saat Ini",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        placeholder = { Text("Password Baru", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        placeholder = { Text("Konfirmasi Password Baru", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        isError = confirmPasswordInput.isNotEmpty() && newPasswordInput != confirmPasswordInput
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput.isBlank()) {
                            Toast.makeText(context, "Password baru tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPasswordInput != confirmPasswordInput) {
                            Toast.makeText(context, "Password konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            isResetSubmitting = true
                            try {
                                val res = withContext(Dispatchers.IO) {
                                    ApiClient.apiService.resetTenantPassword(
                                        ResetPasswordTenantRequest(username = t.username, new_password = newPasswordInput)
                                    )
                                }
                                operationResultMsg = res.message
                                showResetPasswordDialog = null
                                loadTenants()
                            } catch (e: Exception) {
                                operationResultMsg = "Gagal reset password: ${e.localizedMessage ?: "Error"}"
                            } finally {
                                isResetSubmitting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !isResetSubmitting
                ) {
                    if (isResetSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Simpan Password")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = null }, enabled = !isResetSubmitting) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    // DIALOG: Toggle Status Confirm (Disable / Enable / Toggle Demo - manage_tenant.sh Menu 4, 5, 6)
    if (showStatusConfirmDialog != null) {
        val (t, action) = showStatusConfirmDialog!!
        val actionTitle = when (action) {
            "disable" -> "Nonaktifkan Tenant"
            "enable" -> "Aktifkan Tenant"
            else -> "Ubah Mode Demo"
        }
        val actionDesc = when (action) {
            "disable" -> "Apakah Anda yakin ingin menonaktifkan tenant '${t.name}'? Akses login superadmin dan admin di database ini akan diblokir."
            "enable" -> "Buka kembali akses login untuk tenant '${t.name}' (username: ${t.username})?"
            else -> "Ubah status mode demo tenant '${t.name}' menjadi ${if (t.is_demo == 1) "PRODUKSI (Bukan Demo)" else "DEMO (Uji Coba)"}?"
        }

        AlertDialog(
            onDismissRequest = { if (!isStatusSubmitting) showStatusConfirmDialog = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(actionTitle, color = textMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(actionDesc, color = textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isStatusSubmitting = true
                            try {
                                val res = withContext(Dispatchers.IO) {
                                    when (action) {
                                        "disable" -> ApiClient.apiService.disableTenant(TenantActionRequest(username = t.username))
                                        "enable" -> ApiClient.apiService.enableTenant(TenantActionRequest(username = t.username))
                                        else -> ApiClient.apiService.toggleDemoTenant(TenantActionRequest(username = t.username))
                                    }
                                }
                                operationResultMsg = res.message
                                showStatusConfirmDialog = null
                                loadTenants()
                            } catch (e: Exception) {
                                operationResultMsg = "Gagal memproses aksi: ${e.localizedMessage ?: "Error"}"
                            } finally {
                                isStatusSubmitting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (action == "disable") errorColor else if (action == "enable") successColor else purpleColor
                    ),
                    enabled = !isStatusSubmitting
                ) {
                    if (isStatusSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Lanjutkan")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusConfirmDialog = null }, enabled = !isStatusSubmitting) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    // DIALOG: Hapus Tenant Permanen (manage_tenant.sh Menu 11)
    if (showDeleteDialog != null) {
        val t = showDeleteDialog!!

        AlertDialog(
            onDismissRequest = { if (!isDeleteSubmitting) showDeleteDialog = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Tenant Permanen", color = errorColor, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Tindakan ini akan menghapus database '${t.db_name}' beserta seluruh data pelanggan, tagihan, transaksi, dan akun superadmin '${t.username}' secara permanen!",
                        color = textSecondary,
                        fontSize = 13.sp
                    )

                    Surface(
                        color = errorColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Untuk melanjutkan, ketik kata 'HAPUS' (huruf besar) di bawah:",
                            color = errorColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = deleteConfirmationInput,
                        onValueChange = { deleteConfirmationInput = it.trim() },
                        placeholder = { Text("Ketik 'HAPUS'", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmationInput != "HAPUS") {
                            Toast.makeText(context, "Kata kunci konfirmasi tidak cocok!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            isDeleteSubmitting = true
                            try {
                                val res = withContext(Dispatchers.IO) {
                                    ApiClient.apiService.deleteTenant(
                                        DeleteTenantRequest(username = t.username, confirmation = deleteConfirmationInput)
                                    )
                                }
                                operationResultMsg = res.message
                                showDeleteDialog = null
                                loadTenants()
                            } catch (e: Exception) {
                                operationResultMsg = "Gagal menghapus tenant: ${e.localizedMessage ?: "Error"}"
                            } finally {
                                isDeleteSubmitting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    enabled = !isDeleteSubmitting && deleteConfirmationInput == "HAPUS"
                ) {
                    if (isDeleteSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("HAPUS PERMANEN", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }, enabled = !isDeleteSubmitting) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    // DIALOG: Konfirmasi Hapus Pemberitahuan Global
    if (broadcastToDelete != null) {
        val b = broadcastToDelete!!
        AlertDialog(
            onDismissRequest = { if (!isDeletingBroadcast) broadcastToDelete = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = errorColor, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Hapus Pemberitahuan?", color = textMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    Text(
                        "Pemberitahuan ini akan dihapus dari SELURUH DATABASE tenant dan tidak lagi muncul di Card Info Tenant tiap user:",
                        color = textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = b.message,
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isDeletingBroadcast = true
                            try {
                                var respMessage = "Pemberitahuan dihapus dari seluruh database."
                                try {
                                    val res = withContext(Dispatchers.IO) {
                                        ApiClient.apiService.deleteGlobalBroadcast(b.id)
                                    }
                                    respMessage = res.message ?: respMessage
                                } catch (_: Exception) {
                                    // Fallback: hapus dari list di app_settings MySQL
                                    val updatedList = broadcastsList.filter { it.id != b.id }
                                    val jsonStr = Json.encodeToString(updatedList)
                                    withContext(Dispatchers.IO) {
                                        ApiClient.apiService.saveAppSetting("global_master_announcements", mapOf("value" to jsonStr))
                                    }
                                    broadcastsList = updatedList
                                    com.example.ui.data.SettingsManager.globalAnnouncements = updatedList
                                }
                                operationResultMsg = "✅ BERHASIL DIHAPUS:\n\n$respMessage"
                                broadcastToDelete = null
                                loadBroadcasts()
                                com.example.ui.data.SettingsManager.syncSettingsFromRemote(context)
                            } catch (e: Exception) {
                                operationResultMsg = "❌ GAGAL MENGHAPUS:\n${e.localizedMessage ?: e.message}"
                            } finally {
                                isDeletingBroadcast = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    enabled = !isDeletingBroadcast
                ) {
                    if (isDeletingBroadcast) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Menghapus...")
                    } else {
                        Text("HAPUS DARI SEMUA DB", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { broadcastToDelete = null }, enabled = !isDeletingBroadcast) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }

    // DIALOG: Hasil Operasi Feedback
    if (operationResultMsg != null) {
        AlertDialog(
            onDismissRequest = { operationResultMsg = null },
            containerColor = cardBg,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (operationResultMsg?.startsWith("Error") == true || operationResultMsg?.startsWith("Gagal") == true)
                            Icons.Default.ErrorOutline else Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = if (operationResultMsg?.startsWith("Error") == true || operationResultMsg?.startsWith("Gagal") == true)
                            errorColor else successColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Informasi Operasi", color = textMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Text(operationResultMsg ?: "", color = textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { operationResultMsg = null },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    bg: Color,
    border: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(border))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valColor: Color,
    labelColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = labelColor, fontSize = 13.sp)
        Text(value, color = valColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun TenantCard(
    tenant: TenantItem,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    primaryColor: Color,
    successColor: Color,
    errorColor: Color,
    warningColor: Color,
    purpleColor: Color,
    onStatsClick: () -> Unit,
    onResetPasswordClick: () -> Unit,
    onToggleStatusClick: () -> Unit,
    onToggleDemoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Tenant Name and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tenant.name.ifBlank { "Tenant #${tenant.id}" },
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "User: ${tenant.username}",
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                        Text("•", color = textSecondary, fontSize = 12.sp)
                        Text("Pass: ", color = textSecondary, fontSize = 12.sp)
                        Text(
                            text = if (showPassword) (tenant.password?.takeIf { it.isNotBlank() } ?: "(kosong)") else "••••••••",
                            color = textMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Lihat Password",
                            tint = primaryColor,
                            modifier = Modifier.size(15.dp).clickable { showPassword = !showPassword }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Demo Badge
                    if (tenant.is_demo == 1) {
                        Surface(
                            color = purpleColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "DEMO",
                                color = purpleColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Status Badge
                    val isAct = tenant.status == "ACTIVE"
                    Surface(
                        color = if (isAct) successColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isAct) "AKTIF" else "NONAKTIF",
                            color = if (isAct) successColor else errorColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Database info and Counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(textSecondary.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tenant.db_name, color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👥 ${tenant.total_customers ?: "0"}", color = textSecondary, fontSize = 11.sp)
                    Text("👤 ${tenant.total_users ?: "0"}", color = textSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Stats button
                OutlinedButton(
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp), tint = primaryColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detail", fontSize = 11.sp, color = textMain)
                }

                // 2. Reset Password button
                OutlinedButton(
                    onClick = onResetPasswordClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(cardBorder))
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp), tint = warningColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Password", fontSize = 11.sp, color = textMain)
                }

                // 3. Toggle Status (Disable / Enable)
                OutlinedButton(
                    onClick = onToggleStatusClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (tenant.status == "ACTIVE") errorColor.copy(alpha = 0.5f) else successColor.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        if (tenant.status == "ACTIVE") Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (tenant.status == "ACTIVE") errorColor else successColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (tenant.status == "ACTIVE") "Suspend" else "Aktifkan", fontSize = 11.sp, color = textMain)
                }

                // 4. More Options Popup Menu (Demo toggle, Delete)
                var showMoreMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu Lainnya", tint = textSecondary, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(if (tenant.is_demo == 1) "Matikan Mode Demo" else "Jadikan Mode Demo", fontSize = 12.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, tint = purpleColor, modifier = Modifier.size(16.dp))
                            },
                            onClick = {
                                showMoreMenu = false
                                onToggleDemoClick()
                            }
                        )
                        if (tenant.username != "akbar2026") {
                            DropdownMenuItem(
                                text = {
                                    Text("Hapus Tenant & DB", color = errorColor, fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = errorColor, modifier = Modifier.size(16.dp))
                                },
                                onClick = {
                                    showMoreMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
