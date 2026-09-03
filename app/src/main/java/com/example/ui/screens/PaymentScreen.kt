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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.ui.data.remote.ApiClient
import com.example.ui.screens.Customer
import com.example.ui.data.remote.PaymentRequest
import kotlinx.coroutines.launch
import com.example.ui.data.UserSession
import com.example.ui.data.local.AppDatabase
import com.example.ui.data.local.TagihanEntity
import com.example.ui.data.local.PelangganEntity

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale

fun getCustomerAllUnpaidMonths(
    customer: Customer,
    tagihanList: List<TagihanEntity>,
    monthsList: List<String>
): List<String> {
    val custId = customer.id.toIntOrNull() ?: return emptyList()
    val regCal = parseCustomerRegistrationDate(customer.registerDate)
    val hasRegDate = regCal != null
    val regYear = regCal?.get(java.util.Calendar.YEAR) ?: 0
    val regMonthIdx = regCal?.get(java.util.Calendar.MONTH) ?: 0

    val nowCal = java.util.Calendar.getInstance()
    val currentYear = nowCal.get(java.util.Calendar.YEAR)
    val currentMonthIdx = nowCal.get(java.util.Calendar.MONTH)

    val candidateMonths = mutableListOf<Pair<Int, Int>>() // Pair(monthIdx, year)
    val checkCal = java.util.Calendar.getInstance()

    // 1. Kumpulkan kandidat bulan dari masa lalu sampai bulan berjalan
    for (i in 24 downTo 0) {
        checkCal.time = java.util.Date()
        checkCal.add(java.util.Calendar.MONTH, -i)
        val y = checkCal.get(java.util.Calendar.YEAR)
        val mIdx = checkCal.get(java.util.Calendar.MONTH)

        if (hasRegDate) {
            if (y < regYear || (y == regYear && mIdx < regMonthIdx)) {
                continue
            }
        } else {
            if (i > 12) continue
        }

        candidateMonths.add(Pair(mIdx, y))
    }

    // 2. Periksa apakah ada tagihan dengan status BELUM BAYAR di database
    val dbBelumBayar = tagihanList.filter { t ->
        t.customer_id == custId && (t.status.contains("BELUM", ignoreCase = true) || !t.status.contains("LUNAS", ignoreCase = true))
    }
    for (t in dbBelumBayar) {
        val mIdx = monthsList.indexOfFirst { it.equals(t.bulan, ignoreCase = true) }
        val y = if (t.tahun > 0) t.tahun else currentYear
        if (mIdx >= 0) {
            val p = Pair(mIdx, y)
            if (!candidateMonths.contains(p) && y <= currentYear) {
                candidateMonths.add(p)
            }
        }
    }

    // Urutkan kronologis dari paling lama ke terbaru
    val sortedCandidates = candidateMonths.distinct().sortedWith(compareBy({ it.second }, { it.first }))

    val unpaidMonths = mutableListOf<String>()
    for ((mIdx, y) in sortedCandidates) {
        val mName = monthsList.getOrElse(mIdx) { "" }
        if (mName.isEmpty()) continue
        val yStr = y.toString()
        val isPaid = isCustomerPaidForMonth(customer, mName, yStr, tagihanList, monthsList)
        if (!isPaid) {
            unpaidMonths.add("$mName $yStr")
        }
    }

    return unpaidMonths
}

fun getAmountForMonth(
    monthYearStr: String,
    customer: Customer?,
    tagihanList: List<TagihanEntity>,
    monthsList: List<String>
): Long {
    val parts = monthYearStr.trim().split(" ")
    val monthName = parts.getOrNull(0) ?: ""
    val yearStr = parts.getOrNull(1) ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString()
    val custId = customer?.id?.toIntOrNull()

    val tagihanRecord = if (custId != null) {
        tagihanList.firstOrNull { t ->
            t.customer_id == custId && isTagihanInMonthRecap(t, monthName, yearStr, monthsList)
        }
    } else null

    val amt = tagihanRecord?.amount?.toLong()
    return if (amt != null && amt > 0L) {
        amt
    } else {
        customer?.price?.replace(Regex("\\.0$"), "")?.replace(Regex("[^0-9]"), "")?.toLongOrNull() ?: 0L
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(customerId: String, onBack: () -> Unit, onNavigateToDetail: () -> Unit, onNavigateToSuccess: (String, String, String) -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val successGreen = Color(0xFF00FF00)

    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val localTagihanList by db.tagihanDao().getAllTagihan().collectAsState(initial = emptyList())
    val localPelangganList by db.pelangganDao().getAllPelanggan().collectAsState(initial = emptyList())

    val monthsList = remember {
        listOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    }

    val currentCal = remember { java.util.Calendar.getInstance() }
    val currentYear = remember { currentCal.get(java.util.Calendar.YEAR) }
    val currentMonthIdx = remember { currentCal.get(java.util.Calendar.MONTH) }
    val currentMonthName = remember { monthsList.getOrElse(currentMonthIdx) { "Januari" } }
    val currentYearStr = remember { currentYear.toString() }

    var customer by remember { mutableStateOf<Customer?>(null) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    
    val currentMonth = remember {
        val cal = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
        sdf.format(cal.time)
    }
    
    fun getMonthNameWithOffset(offset: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, offset)
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
        return sdf.format(cal.time)
    }

    val monthsToPay = remember { mutableStateListOf<String>() }
    var hasAutoLoadedMonths by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    var customDiscount by remember { mutableStateOf(0) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var discountInputText by remember { mutableStateOf("") }
    
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val availableMonths by remember(customer, localTagihanList) {
        derivedStateOf {
            val list = mutableListOf<String>()
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MONTH, -12)
            val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
            for (i in 0 until 25) {
                list.add(sdf.format(cal.time))
                cal.add(java.util.Calendar.MONTH, 1)
            }
            if (customer != null) {
                val unpaid = getCustomerAllUnpaidMonths(customer!!, localTagihanList, monthsList)
                for (m in unpaid) {
                    if (!list.contains(m)) {
                        list.add(0, m)
                    }
                }
            }
            list.distinct()
        }
    }
    
    val selectedOptionText by remember {
        derivedStateOf {
            when {
                monthsToPay.isEmpty() -> "Pilih Bulan (0 dipilih)"
                monthsToPay.size == 1 -> "1 Bulan (${monthsToPay.first()})"
                else -> "${monthsToPay.size} Bulan (${monthsToPay.joinToString(", ")})"
            }
        }
    }
    
    val totalAmount = remember(monthsToPay.toList(), customer, localTagihanList) {
        monthsToPay.sumOf { getAmountForMonth(it, customer, localTagihanList, monthsList) }
    }
    val finalAmount = (totalAmount - customDiscount).coerceAtLeast(0L)
    
    androidx.compose.runtime.LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }

            val syncData = ApiClient.apiService.syncData()
            val tagihanMapped = syncData.tagihan.map { item ->
                TagihanEntity(
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
            db.tagihanDao().insertAll(tagihanMapped)

            if (customer == null) {
                val found = syncData.customers.find { it.id == customerId }
                if (found != null) {
                    customer = Customer(
                        id = found.id,
                        name = found.name ?: "",
                        phone = found.phone ?: "",
                        area = found.area ?: "",
                        address = found.address,
                        username = found.username ?: "",
                        billingDate = found.billingDate ?: "",
                        status = found.status ?: "",
                        price = found.price ?: "",
                        discount = found.discount ?: "",
                        registerDate = found.register_date,
                        isolateDate = found.isolate_date,
                        packageName = found.package_name,
                        additionalCost1 = found.additionalCost1,
                        additionalCost2 = found.additionalCost2,
                        pppoeSecret = found.pppoe_secret,
                        odpId = found.odp_id,
                        odpPort = found.odp_port
                    )
                }
            }
        } catch(e: Exception) {
            val localCust = localPelangganList.find { it.id.toString() == customerId }
            if (localCust != null && customer == null) {
                customer = Customer(
                    id = localCust.id.toString(),
                    name = localCust.name,
                    phone = localCust.phone,
                    area = localCust.area,
                    address = localCust.address,
                    username = localCust.username,
                    billingDate = localCust.billingDate,
                    status = localCust.status,
                    price = localCust.price,
                    discount = localCust.discount,
                    registerDate = localCust.register_date,
                    isolateDate = localCust.isolate_date,
                    packageName = localCust.package_name,
                    additionalCost1 = localCust.additionalCost1,
                    additionalCost2 = localCust.additionalCost2,
                    pppoeSecret = localCust.pppoe_secret,
                    odpId = localCust.odp_id?.toString(),
                    odpPort = localCust.odp_port
                )
            }
        } finally {
            isBackgroundLoading = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(localPelangganList, customerId) {
        if (customer == null) {
            val localCust = localPelangganList.find { it.id.toString() == customerId }
            if (localCust != null) {
                customer = Customer(
                    id = localCust.id.toString(),
                    name = localCust.name,
                    phone = localCust.phone,
                    area = localCust.area,
                    address = localCust.address,
                    username = localCust.username,
                    billingDate = localCust.billingDate,
                    status = localCust.status,
                    price = localCust.price,
                    discount = localCust.discount,
                    registerDate = localCust.register_date,
                    isolateDate = localCust.isolate_date,
                    packageName = localCust.package_name,
                    additionalCost1 = localCust.additionalCost1,
                    additionalCost2 = localCust.additionalCost2,
                    pppoeSecret = localCust.pppoe_secret,
                    odpId = localCust.odp_id?.toString(),
                    odpPort = localCust.odp_port
                )
            }
        }
    }

    // Ambil data bulan tagihan dari riwayat tagihan pelanggan (belum lunas bulan sebelumnya & bulan ini)
    androidx.compose.runtime.LaunchedEffect(customer, localTagihanList, isBackgroundLoading) {
        val cust = customer
        if (cust != null && !hasAutoLoadedMonths) {
            if (localTagihanList.isNotEmpty() || !isBackgroundLoading) {
                val unpaid = getCustomerAllUnpaidMonths(cust, localTagihanList, monthsList)
                monthsToPay.clear()
                if (unpaid.isNotEmpty()) {
                    monthsToPay.addAll(unpaid)
                } else {
                    val isCurPaid = isCustomerPaidForMonth(cust, currentMonthName, currentYearStr, localTagihanList, monthsList)
                    if (!isCurPaid) {
                        monthsToPay.add(currentMonth)
                    }
                }
                hasAutoLoadedMonths = true
            }
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(customer) {
        if (customer != null) {
            val parsed = customer?.discount?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
            customDiscount = parsed
        }
    }
    
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val coroutineScope = rememberCoroutineScope()
    val currentUser by UserSession.currentUser.collectAsState()
    val totalFormatted = "Rp. ${formatter.format(totalAmount)}"
    val finalFormatted = "Rp. ${formatter.format(finalAmount)}"

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Belum Bayar", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = textMain)
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
                .verticalScroll(rememberScrollState())
        ) {
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonCyan,
                    trackColor = cardBorder
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bayar Cepat", color = textMain, fontSize = 16.sp)
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = textMain),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("PRINT", fontWeight = FontWeight.Bold)
                }
            }

            // Customer Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(neonCyan)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(customer?.name ?: "Loading...", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(customer?.phone ?: "-", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(customerId, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Details Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pembayaran", color = textSecondary, fontSize = 12.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(customer?.packageName ?: "Reguler", color = textMain, fontSize = 14.sp)
                    Text(customer?.price ?: "Rp. 0", color = textMain, fontSize = 14.sp)
                }
                if (customDiscount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon", color = textMain, fontSize = 14.sp)
                        Text("- Rp. ${formatter.format(customDiscount)}", color = Color.Green, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Perbulannya", color = textSecondary, fontSize = 14.sp)
                    val basePrice = customer?.price ?: "Rp. 0"
                    Text(basePrice, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Bulan yang Akan Dibayar", color = textSecondary, fontSize = 12.sp)
                
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedTrailingIconColor = neonCyan,
                            unfocusedTrailingIconColor = textMain.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        containerColor = cardBg,
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .border(1.dp, cardBorder)
                    ) {
                        // Quick Action Buttons inside Dropdown Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    if (customer != null) {
                                        val unpaid = getCustomerAllUnpaidMonths(customer!!, localTagihanList, monthsList)
                                        monthsToPay.clear()
                                        if (unpaid.isNotEmpty()) {
                                            monthsToPay.addAll(unpaid)
                                        } else {
                                            val isCurPaid = isCustomerPaidForMonth(customer!!, currentMonthName, currentYearStr, localTagihanList, monthsList)
                                            if (!isCurPaid) {
                                                monthsToPay.add(currentMonth)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Text("Tunggakan", fontSize = 11.sp, color = neonCyan, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = {
                                    monthsToPay.clear()
                                    monthsToPay.add(currentMonth)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Text("Bulan Ini", fontSize = 11.sp, color = neonCyan)
                            }
                            TextButton(
                                onClick = {
                                    monthsToPay.clear()
                                    for (i in 0..2) {
                                        monthsToPay.add(getMonthNameWithOffset(i))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Text("+3 Bulan", fontSize = 11.sp, color = neonCyan)
                            }
                            TextButton(
                                onClick = {
                                    monthsToPay.clear()
                                },
                                modifier = Modifier.weight(0.8f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                Text("Reset", fontSize = 11.sp, color = Color(0xFFFF003C))
                            }
                        }

                        HorizontalDivider(color = cardBorder.copy(alpha = 0.3f), thickness = 0.5.dp)

                        availableMonths.forEach { month ->
                            val isSelected = monthsToPay.contains(month)
                            val isCurrent = month.equals(currentMonth, ignoreCase = true)
                            val parts = month.trim().split(" ")
                            val mName = parts.getOrNull(0) ?: ""
                            val yStr = parts.getOrNull(1) ?: currentYearStr
                            val isPaid = customer?.let { isCustomerPaidForMonth(it, mName, yStr, localTagihanList, monthsList) } ?: false
                            val monthAmt = getAmountForMonth(month, customer, localTagihanList, monthsList)
                            val monthAmtFormatted = "Rp. ${formatter.format(monthAmt)}"

                            DropdownMenuItem(
                                leadingIcon = {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = neonCyan,
                                            checkmarkColor = Color.Black,
                                            uncheckedColor = textSecondary
                                        )
                                    )
                                },
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    month,
                                                    color = if (isSelected) neonCyan else textMain,
                                                    fontWeight = if (isCurrent || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                                if (isPaid) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(successGreen.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Lunas", color = successGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else if (!isCurrent) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFF003C).copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Belum Lunas", color = Color(0xFFFF003C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(neonCyan.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Bulan Ini", color = neonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            Text("Nominal: $monthAmtFormatted", color = textSecondary, fontSize = 11.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    if (isSelected) {
                                        monthsToPay.remove(month)
                                    } else {
                                        monthsToPay.add(month)
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rincian Bulan Tagihan (${monthsToPay.size} Bulan):", color = textSecondary, fontSize = 12.sp)
                    if (monthsToPay.isNotEmpty()) {
                        Text(
                            "Hapus Semua",
                            color = Color(0xFFFF003C),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { monthsToPay.clear() }
                        )
                    }
                }
                
                if (monthsToPay.isEmpty()) {
                    val allPaid = customer != null && getCustomerAllUnpaidMonths(customer!!, localTagihanList, monthsList).isEmpty()
                    if (allPaid) {
                        Text(
                            "✓ Semua tagihan bulan sebelumnya dan bulan ini sudah lunas.",
                            color = successGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text(
                            "• Belum ada bulan yang dipilih. Silakan pilih minimal 1 bulan di dropdown di atas.",
                            color = Color(0xFFFF003C),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    monthsToPay.forEach { month ->
                        val parts = month.trim().split(" ")
                        val mName = parts.getOrNull(0) ?: ""
                        val yStr = parts.getOrNull(1) ?: currentYearStr
                        val isPaid = customer?.let { isCustomerPaidForMonth(it, mName, yStr, localTagihanList, monthsList) } ?: false
                        val isCurrent = month.equals(currentMonth, ignoreCase = true)
                        val monthAmt = getAmountForMonth(month, customer, localTagihanList, monthsList)
                        val monthAmtStr = "Rp. ${formatter.format(monthAmt)}"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("• $month", color = textMain, fontSize = 14.sp)
                                if (isPaid) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(successGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("LUNAS", color = successGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (!isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF003C).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("TUNGGAKAN", color = Color(0xFFFF003C), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(neonCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("BULAN INI", color = neonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(monthAmtStr, color = neonCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                IconButton(
                                    onClick = { monthsToPay.remove(month) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus $month",
                                        tint = textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Notifikasi rekomendasi jika ada bulan tunggakan sebelumnya yang belum dipilih
                if (customer != null) {
                    val unpaidMonths = getCustomerAllUnpaidMonths(customer!!, localTagihanList, monthsList)
                    val unselectedUnpaid = unpaidMonths.filter { !monthsToPay.contains(it) }
                    if (unselectedUnpaid.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF003C).copy(alpha = 0.12f))
                                .clickable {
                                    unselectedUnpaid.forEach { if (!monthsToPay.contains(it)) monthsToPay.add(it) }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Terdapat ${unselectedUnpaid.size} bulan belum lunas sebelumnya (${unselectedUnpaid.joinToString(", ")})",
                                color = Color(0xFFFF003C),
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "+ Tambahkan",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Total & Hasil Akhir Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Tagihan", color = textSecondary, fontSize = 14.sp)
                    Text(totalFormatted, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                if (customDiscount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Potongan Diskon", color = successGreen, fontSize = 14.sp)
                        Text("- Rp. ${formatter.format(customDiscount)}", color = successGreen, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = cardBorder.copy(alpha = 0.1f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hasil Akhir", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(finalFormatted, color = neonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Discount Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, successGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { 
                        discountInputText = if (customDiscount > 0) customDiscount.toString() else ""
                        showDiscountDialog = true
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (customDiscount > 0) "Ubah Diskon (Rp. ${formatter.format(customDiscount)})" else "Tambahkan Diskon",
                    color = successGreen,
                    fontSize = 14.sp
                )
                Icon(Icons.Default.Edit, contentDescription = "Edit Discount", tint = successGreen)
            }

            // Warning Text
            Text(
                text = "Jika Tekan BAYAR SEKARANG maka pembayaran ini akan langsung lunas.",
                color = neonCyan,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardBg)
                    .padding(12.dp)
            )

            // Buttons
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF).copy(alpha = 0.1f), contentColor = neonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "QRIS")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bayar Via QRIS", fontWeight = FontWeight.SemiBold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToDetail,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = textMain),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DETAIL PELANGGAN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        customer?.let { cust ->
                            val formattedPhone = formatPhoneForWhatsapp(cust.phone)
                            val monthsDetailText = if (monthsToPay.isNotEmpty()) {
                                monthsToPay.joinToString("\n") { m ->
                                    val amt = getAmountForMonth(m, customer, localTagihanList, monthsList)
                                    "  • $m : Rp. ${formatter.format(amt)}"
                                }
                            } else "  (Belum ada bulan dipilih)"

                            val message = """
                                Halo *${cust.name}*,

                                Berikut adalah rincian tagihan internet Anda:
                                - Nama: ${cust.name}
                                - No HP: ${cust.phone}
                                - Area: ${cust.area}
                                - Paket: ${cust.packageName ?: "-"}
                                - Rincian Bulan:
$monthsDetailText
                                - Total Tagihan: $finalFormatted
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
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Kirim WA")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("KIRIM WA TAGIHAN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = monthsToPay.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF), 
                    contentColor = Color.Black,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (monthsToPay.isEmpty()) "PILIH BULAN TAGIHAN" else "BAYAR SEKARANG (${monthsToPay.size} BULAN)",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = bgMain,
                title = { Text("Konfirmasi Pembayaran", color = textMain, fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin menyelesaikan pembayaran ini sejumlah $finalFormatted untuk ${monthsToPay.size} bulan (${monthsToPay.joinToString(", ")})?", color = textSecondary) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showConfirmDialog = false 
                            coroutineScope.launch {
                                try {
                                    val req = com.example.ui.data.remote.PaymentRequest(
                                        customerId = customerId,
                                        adminName = currentUser?.name ?: "Admin",
                                        totalAmount = finalAmount.toDouble(),
                                        months = monthsToPay.toList()
                                    )
                                    ApiClient.apiService.payBilling(req)

                                    // Perbarui langsung status tagihan di Room DB agar sinkron seketika
                                    val custIdInt = customerId.toIntOrNull()
                                    if (custIdInt != null) {
                                        val updatedTagihans = localTagihanList.map { t ->
                                            if (t.customer_id == custIdInt && monthsToPay.any { m ->
                                                val p = m.trim().split(" ")
                                                isTagihanInMonthRecap(t, p.getOrNull(0) ?: "", p.getOrNull(1) ?: "", monthsList)
                                            }) {
                                                t.copy(status = "LUNAS CASH", admin_name = currentUser?.name ?: "Admin")
                                            } else {
                                                t
                                            }
                                        }
                                        db.tagihanDao().insertAll(updatedTagihans)
                                    }

                                    onNavigateToSuccess(customerId, finalAmount.toString(), monthsToPay.joinToString(", "))
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Pembayaran gagal: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black)
                    ) {
                        Text("Ya", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Tidak", color = textMain)
                    }
                }
            )
        }

        if (showDiscountDialog) {
            AlertDialog(
                onDismissRequest = { showDiscountDialog = false },
                containerColor = bgMain,
                title = { Text("Tambahkan Diskon", color = textMain, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Masukkan nominal diskon yang ingin diberikan:", color = textSecondary, fontSize = 14.sp)
                        OutlinedTextField(
                            value = discountInputText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    discountInputText = input
                                }
                            },
                            label = { Text("Nominal Diskon (Rp)", color = textMain.copy(alpha = 0.7f)) },
                            placeholder = { Text("Contoh: 10000", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = successGreen,
                                unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain,
                                focusedLabelColor = successGreen,
                                unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = discountInputText.toIntOrNull() ?: 0
                            if (amt > totalAmount) {
                                Toast.makeText(context, "Diskon tidak boleh melebihi total tagihan!", Toast.LENGTH_SHORT).show()
                            } else {
                                customDiscount = amt
                                showDiscountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = successGreen, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscountDialog = false }) {
                        Text("Batal", color = textMain)
                    }
                }
            )
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
