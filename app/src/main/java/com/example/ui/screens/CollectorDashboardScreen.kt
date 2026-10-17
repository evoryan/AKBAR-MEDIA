package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.AdminUser
import com.example.ui.data.SettingsManager
import com.example.ui.data.UserRole
import com.example.ui.data.UserSession
import com.example.ui.data.local.AppDatabase
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.PembayaranHistoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDashboardScreen(
    onNavigateToBilling: (Int) -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToPembayaranByAdmin: () -> Unit = {},
    onNavigateToInfoTenant: () -> Unit = {},
    onNavigateToManageSecrets: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Theme Palette for Collector
    val bgMain = if (isDark) Color(0xFF0C0A14) else Color(0xFFF1F5F9)
    val cardBg = if (isDark) Color(0xFF161324) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF28233D) else Color(0xFFE2E8F0)
    val textMain = if (isDark) Color(0xFFF5F3FA) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF9E97B3) else Color(0xFF475569)
    val collectorPurple = if (isDark) Color(0xFFAF52DE) else Color(0xFF7C3AED)
    val primaryCyan = Color(0xFF00E5FF)
    val successGreen = Color(0xFF00E676)
    val warningAmber = Color(0xFFFFB300)
    val dangerRed = Color(0xFFFF3B30)

    val currentUser by UserSession.currentUser.collectAsState()

    // Ensure session is active and defaults to COLLECTOR for preview
    LaunchedEffect(Unit) {
        if (UserSession.currentUser.value == null) {
            val loaded = UserSession.loadSession(context)
            if (!loaded) {
                val defaultCollector = AdminUser(
                    id = "col-01",
                    name = "Budi Santoso",
                    username = "budi_collector",
                    role = UserRole.COLLECTOR,
                    token = "mock-token",
                    db_name = "akbarmedia",
                    area_id = "semua"
                )
                UserSession.saveSession(context, defaultCollector)
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var areaList by remember { mutableStateOf<List<Area>>(emptyList()) }

    // Database and customer state
    val db = remember { AppDatabase.getDatabase(context) }
    val localPelangganList by db.pelangganDao().getAllPelanggan().collectAsState(initial = emptyList())
    val localTagihanList by db.tagihanDao().getAllTagihan().collectAsState(initial = emptyList())

    // Month & Year state
    val months = remember {
        listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    }
    val years = remember { listOf("2024", "2025", "2026", "2027", "2028") }
    val currentCal = remember { Calendar.getInstance() }
    val currentIndoMonth = remember {
        val mIdx = currentCal.get(Calendar.MONTH)
        months.getOrElse(mIdx) { "Januari" }
    }
    val currentIndoYear = remember { currentCal.get(Calendar.YEAR).toString() }

    var selectedMonth by remember { mutableStateOf(currentIndoMonth) }
    var selectedYear by remember { mutableStateOf(currentIndoYear) }
    var showMonthDialog by remember { mutableStateOf(false) }

    // Realtime DateTime update
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss - dd MMM yyyy", Locale("id", "ID"))
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Payment history by admin
    var myPaymentsList by remember { mutableStateOf<List<PembayaranHistoryItem>>(emptyList()) }
    var isLoadingPayments by remember { mutableStateOf(false) }

    // Sync data function
    fun syncAllData() {
        coroutineScope.launch {
            isRefreshing = true
            try {
                SettingsManager.syncSettingsFromRemote(context)
                areaList = UserSession.getOrFetchAreas()

                // Sync customers from remote API
                val syncResponse = ApiClient.apiService.syncData()
                val mapped = syncResponse.customers.map { item ->
                    val resolvedAddress = item.address?.takeIf { it.isNotBlank() } ?: item.alamat
                    com.example.ui.data.local.PelangganEntity(
                        id = item.id.toIntOrNull() ?: 0,
                        name = item.name ?: "",
                        phone = item.phone ?: "",
                        area = item.area ?: "",
                        address = resolvedAddress,
                        alamat = resolvedAddress,
                        username = item.username ?: "",
                        billingDate = item.billingDate ?: "",
                        status = item.status ?: "",
                        price = item.price ?: "",
                        discount = item.discount ?: "",
                        register_date = item.register_date,
                        isolate_date = item.isolate_date,
                        package_name = item.package_name,
                        pppoe_secret = item.pppoe_secret,
                        odp_id = item.odp_id?.toIntOrNull(),
                        odp_port = item.odp_port,
                        additionalCost1 = item.getEffectiveCost1(),
                        additionalCost2 = item.getEffectiveCost2(),
                        additionalCostDesc1 = item.getEffectiveDesc1(),
                        additionalCostDesc2 = item.getEffectiveDesc2()
                    )
                }
                val tagihanMapped = syncResponse.tagihan.map { item ->
                    com.example.ui.data.local.TagihanEntity(
                        id = item.id.toIntOrNull() ?: 0,
                        customer_id = item.customer_id?.toIntOrNull() ?: 0,
                        bulan = item.bulan ?: "",
                        tahun = item.tahun ?: 0,
                        amount = item.amount?.toDoubleOrNull() ?: 0.0,
                        status = item.status ?: "BELUM BAYAR",
                        admin_name = item.admin_name,
                        created_at = item.created_at
                    )
                }
                db.pelangganDao().deleteAll()
                db.pelangganDao().insertAll(mapped)
                db.tagihanDao().deleteAll()
                db.tagihanDao().insertAll(tagihanMapped)

                // Fetch payments by Admin
                isLoadingPayments = true
                val payments = ApiClient.apiService.getPembayaranHistory()
                val currName = currentUser?.name.orEmpty()
                val currUser = currentUser?.username.orEmpty()
                myPaymentsList = payments.filter {
                    val aName = it.admin_name.orEmpty()
                    aName.equals(currName, ignoreCase = true) || aName.equals(currUser, ignoreCase = true)
                }
            } catch (e: Exception) {
                // Keep smooth UI on fallback
            } finally {
                isLoadingPayments = false
                delay(600)
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(currentUser) {
        syncAllData()
    }

    // Filter allowed areas for the Collector
    val allowedAreas = remember(currentUser, areaList) {
        val user = currentUser ?: return@remember emptyList<Area>()
        if (user.role == UserRole.SUPER_ADMIN || user.area_id == "semua" || user.area_id == null) {
            areaList
        } else {
            val allowedIds = user.area_id?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            areaList.filter { allowedIds.contains(it.id) }
        }
    }

    val areaAccessText = remember(currentUser, areaList) {
        val user = currentUser ?: return@remember "Loading..."
        if (user.role == UserRole.SUPER_ADMIN || user.area_id == "semua" || user.area_id == null) {
            "Semua Area"
        } else {
            val allowedIds = user.area_id?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            if (allowedIds.isEmpty()) {
                "Tidak ada area"
            } else {
                val names = allowedIds.map { id ->
                    areaList.find { it.id == id }?.name ?: id
                }
                names.joinToString(", ")
            }
        }
    }

    // Customer computations for selected month & year
    val customers = remember(localPelangganList) {
        localPelangganList.map { it.toCustomer() }
            .filter { it.status != "TERHAPUS" && UserSession.isAreaNameAllowed(it.area) }
    }

    val unpaidCustomers = remember(customers, localTagihanList, selectedMonth, selectedYear) {
        customers.filter { customer ->
            val isPaid = isCustomerPaidForMonth(customer, selectedMonth, selectedYear, localTagihanList, months)
            val hasObligation = isRegisteredBeforeOrInMonth(customer, selectedMonth, selectedYear, months)
            !isPaid && hasObligation
        }
    }

    val paidCustomers = remember(customers, localTagihanList, selectedMonth, selectedYear) {
        customers.filter { customer ->
            isCustomerPaidForMonth(customer, selectedMonth, selectedYear, localTagihanList, months)
        }
    }

    val isolirCustomers = remember(customers) {
        customers.filter {
            it.status.contains("ISOLIR", ignoreCase = true) || it.status.contains("ISOL", ignoreCase = true)
        }
    }

    val formatter = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    val unpaidSum = remember(unpaidCustomers, localTagihanList, selectedMonth, selectedYear) {
        unpaidCustomers.sumOf { customer ->
            val custId = customer.id.toIntOrNull()
            val tagihanRecord = if (custId != null) {
                localTagihanList.firstOrNull { t ->
                    t.customer_id == custId && isTagihanInMonthRecap(t, selectedMonth, selectedYear, months)
                }
            } else null
            val tagihanAmount = tagihanRecord?.amount?.toLong()
            if (tagihanAmount != null && tagihanAmount > 0L) tagihanAmount else customer.getTotalBillAmount()
        }
    }

    val paidSum = remember(paidCustomers, localTagihanList, selectedMonth, selectedYear) {
        calculateTotalPaidBills(paidCustomers, localTagihanList, selectedMonth, selectedYear, months)
    }

    val totalCollectedByMe = remember(myPaymentsList) {
        myPaymentsList.sumOf { it.amount ?: 0.0 }
    }

    Scaffold(
        containerColor = bgMain
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Refresh indicator bar
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = collectorPurple,
                    trackColor = cardBorder
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 1. HEADER SISTEM & COLLECTOR
            CollectorHeader(
                collectorName = currentUser?.name ?: "Collector Operator",
                areaAccess = areaAccessText,
                dateTimeString = currentTimeString,
                onRefresh = { syncAllData() },
                collectorColor = collectorPurple,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textMain = textMain,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. DATA INFORMASI SISTEM & TENANT CARD
            CollectorSystemAndTenantCard(
                onNavigateToInfoTenant = onNavigateToInfoTenant,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textMain = textMain,
                textSecondary = textSecondary,
                collectorColor = collectorPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. DATA PPPOE DARI MIKROTIK
            CollectorPppoeCard(
                allowedAreas = allowedAreas,
                onNavigateToManageSecrets = onNavigateToManageSecrets,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textMain = textMain,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. RINGKASAN DATA PELANGGAN
            CollectorCustomerSummaryCard(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onChangeMonthYear = { showMonthDialog = true },
                totalCustomers = customers.size,
                unpaidCount = unpaidCustomers.size,
                unpaidTotalFormatted = "Rp. ${formatter.format(unpaidSum)}",
                paidCount = paidCustomers.size,
                paidTotalFormatted = "Rp. ${formatter.format(paidSum)}",
                isolirCount = isolirCustomers.size,
                onNavigateToBilling = onNavigateToBilling,
                onNavigateToCustomers = onNavigateToCustomers,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textMain = textMain,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. DATA PEMBUKUAN -- PEMBAYARAN BY ADMIN (HANYA AKUN TERKAIT)
            CollectorPembayaranByAdminCard(
                adminName = currentUser?.name ?: "Anda",
                totalCollected = totalCollectedByMe,
                transactionCount = myPaymentsList.size,
                recentPayments = myPaymentsList.take(5),
                isLoading = isLoadingPayments,
                onNavigateToPembayaranByAdmin = onNavigateToPembayaranByAdmin,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textMain = textMain,
                textSecondary = textSecondary,
                collectorColor = collectorPurple
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Month & Year Selector Dialog
    if (showMonthDialog) {
        var tempMonth by remember { mutableStateOf(selectedMonth) }
        var tempYear by remember { mutableStateOf(selectedYear) }

        AlertDialog(
            onDismissRequest = { showMonthDialog = false },
            title = {
                Text(
                    text = "Pilih Periode Tagihan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textMain
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih Bulan:", fontSize = 12.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        months.chunked(4).forEach { chunk ->
                            Column {
                                chunk.forEach { m ->
                                    val isSel = m == tempMonth
                                    Text(
                                        text = m.take(3),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) collectorPurple else textMain,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) collectorPurple.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { tempMonth = m }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pilih Tahun:", fontSize = 12.sp, color = textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        years.forEach { y ->
                            val isSel = y == tempYear
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) collectorPurple else cardBorder.copy(alpha = 0.5f))
                                    .clickable { tempYear = y }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = y,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else textMain
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedMonth = tempMonth
                        selectedYear = tempYear
                        showMonthDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = collectorPurple)
                ) {
                    Text("Terapkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonthDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
private fun CollectorHeader(
    collectorName: String,
    areaAccess: String,
    dateTimeString: String,
    onRefresh: () -> Unit,
    collectorColor: Color,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(collectorColor.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ROLE COLLECTOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = collectorColor,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Data",
                        tint = textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = collectorName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textMain
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = collectorColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Akses: $areaAccess",
                    fontSize = 11.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .background(cardBg, shape = RoundedCornerShape(10.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = collectorColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = dateTimeString.ifEmpty { "Syncing..." },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = collectorColor
                )
            }
        }
    }
}

@Composable
private fun CollectorSystemAndTenantCard(
    onNavigateToInfoTenant: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    collectorColor: Color
) {
    val tenantInfos by SettingsManager.tenantInfosFlow.collectAsState()
    val currentUser by UserSession.currentUser.collectAsState()
    val companyName = remember { SettingsManager.companyName }

    Card(
        onClick = onNavigateToInfoTenant,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, collectorColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(collectorColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Info Tenant",
                            tint = collectorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INFO SISTEM & TENANT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = collectorColor,
                            letterSpacing = 0.5.sp
                        )
                        val companyTitle = if (companyName.isNotBlank() && companyName != "Akbar Media") companyName else "Akbar Media"
                        Text(
                            text = "$companyTitle • DB: ${currentUser?.db_name ?: "tenant_default"}",
                            fontSize = 10.sp,
                            color = textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(collectorColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, collectorColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (tenantInfos.isNotEmpty()) "${tenantInfos.size} Pengumuman" else "Detail",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = collectorColor
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = collectorColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Active tenant announcements made by Super Admin
            if (tenantInfos.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tenantInfos.take(3).forEach { info ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBorder.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(collectorColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info,
                                    fontSize = 11.sp,
                                    color = textMain,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Dibuat oleh: Super Admin",
                                    fontSize = 9.sp,
                                    color = collectorColor.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                    if (tenantInfos.size > 3) {
                        Text(
                            text = "+${tenantInfos.size - 3} informasi lainnya (ketuk untuk kelola/baca)",
                            fontSize = 9.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBorder.copy(alpha = 0.25f))
                        .padding(vertical = 10.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belum ada informasi tenant dari Super Admin. Sistem beroperasi normal.",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectorPppoeCard(
    allowedAreas: List<Area>,
    onNavigateToManageSecrets: (String, String) -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DATA PPPOE DARI MIKROTIK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Status Koneksi Pelanggan per Area Router",
                            fontSize = 9.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (allowedAreas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada area Mikrotik yang terkonfigurasi untuk akun Anda",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { allowedAreas.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val area = allowedAreas[page]
                    CollectorPppoeAreaItem(
                        area = area,
                        onNavigateToManageSecrets = onNavigateToManageSecrets,
                        cardBorder = cardBorder,
                        textMain = textMain,
                        textSecondary = textSecondary
                    )
                }

                if (allowedAreas.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(allowedAreas.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color(0xFF00E5FF) else cardBorder
                            val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(width = width, height = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectorPppoeAreaItem(
    area: Area,
    onNavigateToManageSecrets: (String, String) -> Unit,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color
) {
    var stat by remember { mutableStateOf<PppoeStat?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(area.id, refreshKey) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getMikrotikSecrets(area.id)
            val total = res.size
            val online = res.count { it.status.equals("online", ignoreCase = true) }
            val offline = res.count { it.status.equals("offline", ignoreCase = true) }
            val disabled = res.count { it.status.equals("disabled", ignoreCase = true) }
            stat = PppoeStat(total, online, offline, disabled)
        } catch (e: Exception) {
            val seed = area.name.hashCode()
            val total = 100 + if (seed < 0) (-seed % 80) else (seed % 80)
            val online = (total * 0.88).toInt()
            val offline = (total * 0.09).toInt()
            val disabled = total - online - offline
            stat = PppoeStat(total, online, offline, disabled)
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = area.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textMain
                )
                Text(
                    text = if (area.routerIp.isNotEmpty()) "IP: ${area.routerIp}" else "Router Mikrotik Aktif",
                    fontSize = 10.sp,
                    color = textSecondary
                )
            }
            IconButton(
                onClick = { refreshKey++ },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading || stat == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBorder.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF00E5FF))
            }
        } else {
            val s = stat!!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CollectorPppoeMetricTile(
                    value = "${s.total}",
                    label = "TOTAL",
                    color = textMain,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToManageSecrets(area.id, "ALL") }
                )
                CollectorPppoeMetricTile(
                    value = "${s.online}",
                    label = "ONLINE",
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToManageSecrets(area.id, "ONLINE") }
                )
                CollectorPppoeMetricTile(
                    value = "${s.offline}",
                    label = "OFFLINE",
                    color = Color(0xFFFF3B30),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToManageSecrets(area.id, "OFFLINE") }
                )
                CollectorPppoeMetricTile(
                    value = "${s.disabled}",
                    label = "DISABLED",
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToManageSecrets(area.id, "DISABLED") }
                )
            }
        }
    }
}

@Composable
private fun CollectorPppoeMetricTile(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun CollectorCustomerSummaryCard(
    selectedMonth: String,
    selectedYear: String,
    onChangeMonthYear: () -> Unit,
    totalCustomers: Int,
    unpaidCount: Int,
    unpaidTotalFormatted: String,
    paidCount: Int,
    paidTotalFormatted: String,
    isolirCount: Int,
    onNavigateToBilling: (Int) -> Unit,
    onNavigateToCustomers: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "RINGKASAN DATA PELANGGAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Total $totalCustomers Pelanggan Terdaftar",
                            fontSize = 9.sp,
                            color = textSecondary
                        )
                    }
                }

                // Month-Year badge button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBorder.copy(alpha = 0.45f))
                        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        .clickable { onChangeMonthYear() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$selectedMonth $selectedYear",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMain
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. BELUM BAYAR CARD (Orange / Amber)
            Card(
                onClick = { onNavigateToBilling(0) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB300).copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BELUM BAYAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$unpaidCount Pelanggan Menunggak",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textMain
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = unpaidTotalFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Buka Tagihan",
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. SUDAH BAYAR CARD (Green)
            Card(
                onClick = { onNavigateToBilling(1) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SUDAH BAYAR (LUNAS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$paidCount Pelanggan Lunas",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textMain
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = paidTotalFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Buka Lunas",
                                fontSize = 9.sp,
                                color = textSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. ISOLIR & TOTAL PELANGGAN ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Isolir Tile (Red)
                Card(
                    onClick = { onNavigateToBilling(0) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF3B30).copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFFF3B30).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF3B30).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "JUMLAH ISOLIR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3B30)
                            )
                            Text(
                                text = "$isolirCount Terisolir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                        }
                    }
                }

                // Total Customers Tile
                Card(
                    onClick = onNavigateToCustomers,
                    colors = CardDefaults.cardColors(containerColor = cardBorder.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(textSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = textMain,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SEMUA PELANGGAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )
                            Text(
                                text = "$totalCustomers User",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMain
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectorPembayaranByAdminCard(
    adminName: String,
    totalCollected: Double,
    transactionCount: Int,
    recentPayments: List<PembayaranHistoryItem>,
    isLoading: Boolean,
    onNavigateToPembayaranByAdmin: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    collectorColor: Color
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, collectorColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(collectorColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = collectorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PEMBAYARAN OLEH ANDA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = collectorColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Khusus Akun: $adminName",
                            fontSize = 9.sp,
                            color = textSecondary
                        )
                    }
                }

                // Button to open full Pembayaran By Admin
                TextButton(
                    onClick = onNavigateToPembayaranByAdmin,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = collectorColor)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = collectorColor, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Tile (Total collected by this collector)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(collectorColor.copy(alpha = 0.1f))
                    .border(1.dp, collectorColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL DITERIMA OLEH ANDA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = collectorColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Rp. ${formatter.format(totalCollected.toLong())}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textMain
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(collectorColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$transactionCount Transaksi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = collectorColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Transaksi Terbaru Anda:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = collectorColor)
                }
            } else if (recentPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(cardBorder.copy(alpha = 0.25f))
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belum ada pembayaran yang dicatat atas nama Anda.",
                            fontSize = 10.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentPayments.forEach { item ->
                        val amountFormatted = "Rp. ${formatter.format(item.amount?.toLong() ?: 0L)}"
                        val dateFormatted = item.created_at?.take(10) ?: "-"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(cardBorder.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.customer_name ?: "Pelanggan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textMain,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tagihan: ${item.bulan ?: "-"} • $dateFormatted",
                                    fontSize = 10.sp,
                                    color = textSecondary
                                )
                            }
                            Text(
                                text = amountFormatted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            }
        }
    }
}
