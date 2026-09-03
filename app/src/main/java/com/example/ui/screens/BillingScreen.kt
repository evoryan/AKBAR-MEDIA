package com.example.ui.screens

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning

import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.ui.data.remote.LoginRequest
import com.example.ui.data.UserRole
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.DeleteBillingRequest
import kotlinx.coroutines.launch
import com.example.ui.data.UserSession

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun parseDateToMillis(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    val formats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy",
        "yyyy/MM/dd"
    )
    for (fmt in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
            sdf.isLenient = true
            val d = sdf.parse(dateStr.trim())
            if (d != null) return d.time
        } catch (_: Exception) {}
    }
    return null
}

fun getMonthDateRange(monthIdx: Int, yearInt: Int): Pair<Long, Long> {
    val startCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, yearInt)
        set(java.util.Calendar.MONTH, monthIdx)
        set(java.util.Calendar.DAY_OF_MONTH, 1)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val endCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, yearInt)
        set(java.util.Calendar.MONTH, monthIdx)
        val maxDay = getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        set(java.util.Calendar.DAY_OF_MONTH, maxDay)
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
        set(java.util.Calendar.MILLISECOND, 999)
    }
    return Pair(startCal.timeInMillis, endCal.timeInMillis)
}

fun isTagihanInMonthRecap(
    t: com.example.ui.data.local.TagihanEntity,
    month: String,
    year: String,
    monthsList: List<String> = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
): Boolean {
    val monthIdx = monthsList.indexOfFirst { it.equals(month, ignoreCase = true) }
    if (monthIdx < 0) return false
    val yearInt = year.trim().toIntOrNull() ?: return false

    val (startMillis, endMillis) = getMonthDateRange(monthIdx, yearInt)

    // 1. Cek dari tanggal created_at apakah dimulai dari awal bulan/tanggal 1 sampai akhir bulan
    val createdMillis = parseDateToMillis(t.created_at)
    if (createdMillis != null && createdMillis in startMillis..endMillis) {
        return true
    }

    // 2. Cek kesesuaian tahun dan bulan pada field tagihan
    val yearStr = yearInt.toString()
    val yearMatches = t.tahun == yearInt ||
            (t.tahun == 0 && (t.bulan.contains(yearStr) || (t.created_at != null && t.created_at.startsWith(yearStr))))

    val monthNumberStr = String.format("%02d", monthIdx + 1)
    val monthNumberSingle = (monthIdx + 1).toString()
    val engMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val engMonthName = if (monthIdx in engMonths.indices) engMonths[monthIdx] else ""

    val monthMatches = t.bulan.equals(month, ignoreCase = true) ||
            t.bulan.contains(month, ignoreCase = true) ||
            (engMonthName.isNotEmpty() && t.bulan.contains(engMonthName, ignoreCase = true)) ||
            (t.bulan.trim() == monthNumberStr || t.bulan.contains("-$monthNumberStr") || t.bulan.contains("/$monthNumberStr")) ||
            (t.bulan.trim() == monthNumberSingle)

    return yearMatches && monthMatches
}

fun isCustomerPaidForMonth(
    customer: Customer,
    month: String,
    year: String,
    tagihanList: List<com.example.ui.data.local.TagihanEntity>,
    monthsList: List<String> = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
): Boolean {
    val custId = customer.id.toIntOrNull() ?: return false

    val paidRecord = tagihanList.find { t ->
        if (t.customer_id != custId) return@find false
        val isStatusPaid = t.status.contains("LUNAS", ignoreCase = true) || t.status.contains("SUDAH", ignoreCase = true)
        if (!isStatusPaid) return@find false

        isTagihanInMonthRecap(t, month, year, monthsList)
    }

    return paidRecord != null
}

fun parseCustomerRegistrationDate(dateStr: String?): java.util.Calendar? {
    if (dateStr.isNullOrBlank()) return null
    val formats = listOf(
        "dd/MM/yyyy",
        "d/M/yyyy",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "d-M-yyyy",
        "yyyy/MM/dd",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (fmt in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
            sdf.isLenient = false
            val d = sdf.parse(dateStr.trim())
            if (d != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = d
                return cal
            }
        } catch (e: Exception) {
            // try next format
        }
    }
    return null
}

fun isRegisteredBeforeOrInMonth(
    customer: Customer,
    month: String,
    year: String,
    monthsList: List<String>
): Boolean {
    val monthIdx = monthsList.indexOfFirst { it.equals(month, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
    val yearInt = year.trim().toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    // Tanggal akhir bulan (rekap hanya sampai akhir bulan yang dipilih)
    val endOfMonthCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, yearInt)
        set(java.util.Calendar.MONTH, monthIdx)
        val maxDay = getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        set(java.util.Calendar.DAY_OF_MONTH, maxDay)
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
        set(java.util.Calendar.MILLISECOND, 999)
    }

    val regCal = parseCustomerRegistrationDate(customer.registerDate) ?: return true
    return !regCal.after(endOfMonthCal)
}

fun getCustomerUnpaidPastMonths(
    customer: Customer,
    tagihanList: List<com.example.ui.data.local.TagihanEntity>,
    monthsList: List<String>
): List<String> {
    val custId = customer.id.toIntOrNull() ?: return emptyList()
    
    val regCal = parseCustomerRegistrationDate(customer.registerDate)
    val hasRegDate = regCal != null
    val regYear = regCal?.get(java.util.Calendar.YEAR) ?: 0
    val regMonthIdx = regCal?.get(java.util.Calendar.MONTH) ?: 0

    val customerTagihan = tagihanList.filter { it.customer_id == custId }
    val unpaidMonths = mutableListOf<String>()

    val checkCal = java.util.Calendar.getInstance()
    val sdfMonthOnly = java.text.SimpleDateFormat("MMMM", java.util.Locale("id", "ID"))

    // Periksa bulan-bulan lampau yang sudah lewat sebelum bulan berjalan (i = 1 adalah 1 bulan sebelum bulan aktif saat ini)
    for (i in 1..24) {
        checkCal.time = java.util.Date()
        checkCal.add(java.util.Calendar.MONTH, -i)
        val pastYear = checkCal.get(java.util.Calendar.YEAR)
        val pastMonthIdx = checkCal.get(java.util.Calendar.MONTH)

        // Jangan periksa bulan sebelum tanggal registrasi pelanggan
        if (hasRegDate) {
            if (pastYear < regYear || (pastYear == regYear && pastMonthIdx < regMonthIdx)) {
                break
            }
        } else if (i > 12) {
            break
        }

        val monthName = monthsList.getOrElse(pastMonthIdx) { sdfMonthOnly.format(checkCal.time) }
        val monthNumberStr = String.format("%02d", pastMonthIdx + 1)
        val monthNumberSingle = (pastMonthIdx + 1).toString()
        val monthYearName = "$monthName $pastYear"

        // Cek apakah ada input pembayaran (status lunas) untuk bulan lampau tersebut
        val isPaid = customerTagihan.any { t ->
            t.status.contains("LUNAS", ignoreCase = true) &&
            (t.tahun == 0 || t.tahun == pastYear || t.bulan.contains(pastYear.toString())) &&
            (
                t.bulan.contains(monthName, ignoreCase = true) ||
                t.bulan.equals(monthName, ignoreCase = true) ||
                t.bulan.trim() == monthNumberStr ||
                t.bulan.trim() == monthNumberSingle
            )
        }

        if (!isPaid) {
            unpaidMonths.add(monthYearName)
        }
    }

    return unpaidMonths
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(initialTab: Int = 0, onBack: () -> Unit, onNavigateToPayment: (String) -> Unit, onNavigateToSuccess: (String, String, String) -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val neonPink = Color(0xFFFF00FF)
    val warningOrange = Color(0xFFFF9900)
    val errorRed = Color(0xFFFF003C)
    val successGreen = Color(0xFF00FF00)

    val months = remember {
        listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    }
    val years = remember {
        listOf("2024", "2025", "2026", "2027", "2028")
    }

    val currentCal = remember { java.util.Calendar.getInstance() }
    val currentIndoMonth = remember {
        val mIdx = currentCal.get(java.util.Calendar.MONTH)
        months.getOrElse(mIdx) { "Januari" }
    }
    val currentIndoYear = remember {
        currentCal.get(java.util.Calendar.YEAR).toString()
    }

    var selectedMonth by remember { mutableStateOf(currentIndoMonth) }
    var selectedYear by remember { mutableStateOf(currentIndoYear) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.example.ui.data.local.AppDatabase.getDatabase(context) }
    val localPelangganList by db.pelangganDao().getAllPelanggan().collectAsState(initial = emptyList())
    val localTagihanList by db.tagihanDao().getAllTagihan().collectAsState(initial = emptyList())

    val customers = remember(localPelangganList) {
        localPelangganList.map { entity ->
            Customer(
                id = entity.id.toString(),
                name = entity.name,
                phone = entity.phone,
                area = entity.area,
                address = entity.address,
                username = entity.username,
                billingDate = entity.billingDate,
                status = entity.status,
                price = entity.price,
                discount = entity.discount,
                registerDate = entity.register_date,
                isolateDate = entity.isolate_date,
                packageName = entity.package_name,
                additionalCost1 = entity.additionalCost1,
                additionalCost2 = entity.additionalCost2,
                pppoeSecret = entity.pppoe_secret,
                odpId = entity.odp_id?.toString(),
                odpPort = entity.odp_port
            )
        }.filter { it.status != "TERHAPUS" && com.example.ui.data.UserSession.isAreaNameAllowed(it.area) }
    }

    var showCancelDialog by remember { mutableStateOf(false) }
    var customerToCancel by remember { mutableStateOf<Customer?>(null) }
    var cancelPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    fun fetchCustomers() {
        coroutineScope.launch {
            try {
                com.example.ui.data.UserSession.getOrFetchAreas()
                val syncResponse = ApiClient.apiService.syncData()
                val mapped = syncResponse.customers.map { item ->
                    com.example.ui.data.local.PelangganEntity(
                        id = item.id.toIntOrNull() ?: 0,
                        name = item.name ?: "",
                        phone = item.phone ?: "",
                        area = item.area ?: "",
                        address = item.address,
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
                        additionalCost1 = item.additionalCost1,
                        additionalCost2 = item.additionalCost2
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
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        fetchCustomers()
    }

    val areas = listOf("Semua") + customers.map { it.area }.distinct().sorted()
    var selectedArea by remember { mutableStateOf("Semua") }
    
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }
    
    val localFocusManager = LocalFocusManager.current

    val filteredByArea = if (selectedArea == "Semua") customers else customers.filter { it.area == selectedArea }
    val filteredBySearch = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery, ignoreCase = true) }
    
    val unpaidCustomers = filteredBySearch.filter { customer ->
        val isPaid = isCustomerPaidForMonth(customer, selectedMonth, selectedYear, localTagihanList, months)
        val hasObligation = isRegisteredBeforeOrInMonth(customer, selectedMonth, selectedYear, months)
        !isPaid && hasObligation
    }
    val paidCustomers = filteredBySearch.filter { customer ->
        isCustomerPaidForMonth(customer, selectedMonth, selectedYear, localTagihanList, months)
    }
    
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val unpaidSum = unpaidCustomers.sumOf { customer ->
        val custId = customer.id.toIntOrNull()
        val tagihanRecord = if (custId != null) {
            localTagihanList.firstOrNull { t ->
                t.customer_id == custId && isTagihanInMonthRecap(t, selectedMonth, selectedYear, months)
            }
        } else null

        val tagihanAmount = tagihanRecord?.amount?.toLong()
        if (tagihanAmount != null && tagihanAmount > 0L) {
            tagihanAmount
        } else {
            customer.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
        }
    }
    val paidSum = paidCustomers.sumOf { customer ->
        val custId = customer.id.toIntOrNull()
        val tagihanRecord = if (custId != null) {
            localTagihanList.firstOrNull { t ->
                t.customer_id == custId &&
                (t.status.contains("LUNAS", ignoreCase = true) || t.status.contains("SUDAH", ignoreCase = true)) &&
                isTagihanInMonthRecap(t, selectedMonth, selectedYear, months)
            }
        } else null

        val tagihanAmount = tagihanRecord?.amount?.toLong()
        if (tagihanAmount != null && tagihanAmount > 0L) {
            tagihanAmount
        } else {
            customer.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
        }
    }
    val totalUnpaid = "Rp. ${formatter.format(unpaidSum)}"
    val totalPaid = "Rp. ${formatter.format(paidSum)}"

    if (showCancelDialog && customerToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false
                cancelPassword = ""
            },
            title = { Text("Batalkan Pembayaran", color = textMain) },
            text = { 
                Column {
                    Text("Masukkan password superadmin untuk membatalkan pembayaran ${customerToCancel?.name}:", color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelPassword,
                        onValueChange = { cancelPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val currentUser = com.example.ui.data.UserSession.currentUser.value
                            if (currentUser != null) {
                                val loginRes = com.example.ui.data.remote.ApiClient.apiService.login(
                                    com.example.ui.data.remote.LoginRequest(currentUser.username, cancelPassword)
                                )
                                if (loginRes.role.name == "SUPER_ADMIN" || loginRes.role.name == "ADMIN") {
                                    ApiClient.apiService.deleteBilling(DeleteBillingRequest(customerToCancel!!.id))
                                    val custId = customerToCancel!!.id.toIntOrNull()
                                    if (custId != null) {
                                        db.pelangganDao().updateStatus(custId, "BELUM BAYAR")
                                    }
                                    Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                                    showCancelDialog = false
                                    cancelPassword = ""
                                    customerToCancel = null
                                    fetchCustomers()
                                } else {
                                    Toast.makeText(context, "Akses ditolak. Butuh Super Admin", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Batalkan", color = errorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    cancelPassword = ""
                }) {
                    Text("Tutup", color = textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Tagihan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgMain)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { localFocusManager.clearFocus() })
                }
        ) {
            // Dropdown Bulan & Tahun di Posisi Paling Atas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dropdown Bulan
                Box(modifier = Modifier.weight(1.4f)) {
                    OutlinedButton(
                        onClick = { expandedMonth = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMonth,
                                color = textMain,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = neonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false },
                        modifier = Modifier.background(cardBg).heightIn(max = 280.dp)
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        month, 
                                        color = if (month == selectedMonth) neonCyan else textMain,
                                        fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    ) 
                                },
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
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedYear,
                                color = textMain,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = neonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false },
                        modifier = Modifier.background(cardBg).heightIn(max = 240.dp)
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        year, 
                                        color = if (year == selectedYear) neonCyan else textMain,
                                        fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    ) 
                                },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RectangleShape)
                    .background(cardBg)
                    .border(1.dp, successGreen, RectangleShape)
            ) {
                val tabs = listOf("BELUM BAYAR", "SUDAH BAYAR")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RectangleShape)
                            .background(if (isSelected) successGreen else Color.Transparent)
                            .clickable { selectedTabIndex = index }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isSelected) Color.Black else textSecondary
                        )
                    }
                }
            }

            // Filter and Search Row
            var expanded by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedArea,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Area", color = textMain.copy(alpha = 0.7f), fontSize = 14.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                                focusedTextColor = textMain, unfocusedTextColor = textMain,
                                focusedTrailingIconColor = neonCyan, unfocusedTrailingIconColor = textMain.copy(alpha = 0.5f),
                                focusedLabelColor = neonCyan, unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                            )
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = cardBg) {
                            areas.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area, color = if (selectedArea == area) neonCyan else textMain, fontSize = 14.sp) },
                                    onClick = { selectedArea = area; expanded = false }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari", color = textMain.copy(alpha = 0.7f), fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan, unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                        focusedTextColor = textMain, unfocusedTextColor = textMain,
                        focusedLabelColor = neonCyan, unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // BELUM BAYAR Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorRed.copy(alpha = 0.2f))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Total Tagihan Belum Dibayar ($selectedMonth $selectedYear)", color = textSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(totalUnpaid, color = errorRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                    
                    Text(
                        "Daftar Pelanggan Belum Bayar (${unpaidCustomers.size})", 
                        color = textMain, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(unpaidCustomers) { customer ->
                            BillingCustomerItem(
                                customer = customer, 
                                isPaid = false,
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = {},
                                onIsolirClick = {
                                    coroutineScope.launch {
                                        try {
                                            com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customer.id)
                                            android.widget.Toast.makeText(context, "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Gagal mengisolir pelanggan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onWaClick = {
                                    val formattedPhone = formatPhoneForWhatsapp(customer.phone)
                                    val message = """
                                        Halo *${customer.name}*,

                                        Berikut adalah rincian tagihan internet Anda periode *$selectedMonth $selectedYear*:
                                        - Nama: ${customer.name}
                                        - No HP: ${customer.phone}
                                        - Area: ${customer.area}
                                        - Paket: ${customer.packageName ?: "-"}
                                        - Total Tagihan: ${customer.price}
                                        - Status: *BELUM BAYAR*

                                        Mohon segera melakukan pembayaran. Terima kasih.
                                    """.trimIndent()
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        val url = "https://api.whatsapp.com/send?phone=$formattedPhone&text=${android.net.Uri.encode(message)}"
                                        data = android.net.Uri.parse(url)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal membuka WhatsApp: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                } else {
                    // SUDAH BAYAR Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(successGreen.copy(alpha = 0.2f))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Total Tagihan Sudah Dibayar ($selectedMonth $selectedYear)", color = textSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(totalPaid, color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                    
                    Text(
                        "Daftar Pelanggan Sudah Bayar (${paidCustomers.size})", 
                        color = textMain, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(paidCustomers) { customer ->
                            BillingCustomerItem(
                                customer = customer, 
                                isPaid = true,
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = {},
                                onDetailClick = {
                                    val custId = customer.id.toIntOrNull()
                                    val tagihanRecord = if (custId != null) {
                                        localTagihanList.firstOrNull { t ->
                                            t.customer_id == custId &&
                                            (t.status.contains("LUNAS", ignoreCase = true) || t.status.contains("SUDAH", ignoreCase = true)) &&
                                            isTagihanInMonthRecap(t, selectedMonth, selectedYear, months)
                                        }
                                    } else null
                                    val amount = tagihanRecord?.amount?.toLong()?.toString()
                                        ?: customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "$selectedMonth $selectedYear")
                                },
                                onLongPress = {
                                    customerToCancel = customer
                                    showCancelDialog = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillingCustomerItem(
    customer: Customer,
    isPaid: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    neonPink: Color,
    onPayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onIsolirClick: () -> Unit = {},
    onWaClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content: Info and Price
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(customer.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(customer.phone, color = textSecondary, fontSize = 11.sp)
                Text("Area: ${customer.area}", color = textSecondary, fontSize = 11.sp)
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            // Right content: Status, WA (if unpaid), and Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isPaid) Color(0xFFFF003C).copy(alpha = 0.2f) else neonCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPaid) "LUNAS CASH" else "BELUM BAYAR",
                        color = if (!isPaid) Color(0xFFFF003C) else neonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // WhatsApp Button (Only for unpaid bills)
                if (!isPaid) {
                    Button(
                        onClick = onWaClick,
                        modifier = Modifier.width(110.dp).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Kirim WA",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("KIRIM WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom actions row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPaid) {
                        IconButton(onClick = onIsolirClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = "Isolir", tint = Color(0xFFD4AF37))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    } else {
                        Button(
                            onClick = onLongPress,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFFF003C)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF003C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BATAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            if (!isPaid) {
                                onPayClick()
                            } else {
                                onDetailClick()
                            }
                        },
                        modifier = Modifier.width(110.dp).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPaid) neonCyan else Color.Transparent,
                            contentColor = if (!isPaid) Color.Black else neonCyan
                        ),
                        border = if (isPaid) androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (!isPaid) "BAYAR" else "DETAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatPhoneForWhatsapp(phone: String): String {
    val clean = phone.replace(Regex("[^0-9]"), "")
    return if (clean.startsWith("0")) {
        "62" + clean.substring(1)
    } else if (clean.startsWith("62")) {
        clean
    } else {
        if (clean.length >= 9 && !clean.startsWith("62")) "62$clean" else clean
    }
}
