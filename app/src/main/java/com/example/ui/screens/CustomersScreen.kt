package com.example.ui.screens

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch

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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

import com.example.ui.data.local.AppDatabase
import com.example.ui.data.local.PelangganEntity
import com.example.ui.data.local.TagihanEntity
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onBack: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToEditCustomer: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val localPelangganList by db.pelangganDao().getAllPelanggan().collectAsState(initial = emptyList())
    val localTagihanList by db.tagihanDao().getAllTagihan().collectAsState(initial = emptyList())

    val months = remember {
        listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    }

    val currentUser by UserSession.currentUser.collectAsState()

    val customers = remember(localPelangganList, currentUser) {
        localPelangganList.map { it.toCustomer() }
            .filter { UserSession.isAreaNameAllowed(it.area) }
    }

    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadCustomersFromRemote() {
        coroutineScope.launch {
            isRefreshing = true
            errorMessage = null
            try {
                if (UserSession.currentUser.value == null) {
                    UserSession.loadSession(context)
                }
                UserSession.getOrFetchAreas()
                var loaded = false
                try {
                    val syncResponse = ApiClient.apiService.syncData()
                    val mapped = syncResponse.customers.map { item ->
                        val resolvedAddress = item.address?.takeIf { it.isNotBlank() } ?: item.alamat
                        PelangganEntity(
                            id = item.id?.toIntOrNull() ?: 0,
                            name = item.name ?: "",
                            phone = item.phone ?: "",
                            area = item.area ?: "",
                            address = resolvedAddress,
                            alamat = resolvedAddress,
                            username = item.username ?: "",
                            billingDate = item.billingDate ?: "1",
                            status = item.status ?: "BELUM BAYAR",
                            price = item.price ?: "0",
                            discount = item.discount ?: "0",
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
                    if (mapped.isNotEmpty()) {
                        db.pelangganDao().insertAll(mapped)
                    }
                    val tagihanMapped = syncResponse.tagihan.map { item ->
                        TagihanEntity(
                            id = item.id?.toIntOrNull() ?: 0,
                            customer_id = item.customer_id?.toIntOrNull() ?: 0,
                            bulan = item.bulan ?: "",
                            tahun = item.tahun ?: 0,
                            amount = item.amount?.toDoubleOrNull() ?: 0.0,
                            status = item.status ?: "BELUM BAYAR",
                            admin_name = item.admin_name,
                            created_at = item.created_at
                        )
                    }
                    if (tagihanMapped.isNotEmpty()) {
                        db.tagihanDao().insertAll(tagihanMapped)
                    }
                    if (!syncResponse.gangguan.isNullOrEmpty()) {
                        db.gangguanDao().insertAll(syncResponse.gangguan)
                    }
                    loaded = true
                } catch (eSync: Exception) {
                    android.util.Log.w("CustomersScreen", "syncData failed, falling back to direct getCustomers: ${eSync.message}")
                }

                if (!loaded) {
                    try {
                        val directCustomers = ApiClient.apiService.getCustomers()
                        val mapped = directCustomers.map { item ->
                            val resolvedAddress = item.getEffectiveAddress().ifEmpty { item.address ?: item.alamat }
                            PelangganEntity(
                                id = item.id.toIntOrNull() ?: 0,
                                name = item.name,
                                phone = item.phone,
                                area = item.area,
                                address = resolvedAddress,
                                alamat = resolvedAddress,
                                username = item.username,
                                billingDate = item.billingDate,
                                status = item.status,
                                price = item.price,
                                discount = item.discount,
                                register_date = item.getEffectiveRegisterDate(),
                                isolate_date = item.isolateDate ?: item.isolate_date,
                                package_name = item.packageName,
                                pppoe_secret = item.pppoeSecret,
                                odp_id = item.odpId?.toIntOrNull(),
                                odp_port = item.odpPort,
                                additionalCost1 = item.getEffectiveCost1(),
                                additionalCost2 = item.getEffectiveCost2(),
                                additionalCostDesc1 = item.getEffectiveCostDesc1(),
                                additionalCostDesc2 = item.getEffectiveCostDesc2()
                            )
                        }
                        if (mapped.isNotEmpty()) {
                            db.pelangganDao().insertAll(mapped)
                        }
                        loaded = true
                    } catch (eDirect: Exception) {
                        android.util.Log.e("CustomersScreen", "direct getCustomers failed: ${eDirect.message}", eDirect)
                        throw eDirect
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CustomersScreen", "loadCustomersFromRemote error: ${e.message}", e)
                if (localPelangganList.isEmpty()) {
                    errorMessage = "Gagal memuat data pelanggan: ${e.message ?: "Periksa koneksi internet"}"
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCustomersFromRemote()
    }
    
    val areas = listOf("Semua") + customers.map { it.area }.distinct().sorted()
    var selectedArea by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredByArea = if (selectedArea == "Semua") customers else customers.filter { it.area == selectedArea }
    val filteredCustomers = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.username.contains(searchQuery, ignoreCase = true) || 
        it.phone.contains(searchQuery, ignoreCase = true)
    }
    
    val activeCustomers = customers.filter { it.status != "TERHAPUS" }
    val totalPendapatanGlobal = activeCustomers.sumOf { it.getTotalBillAmount() }
    val activeFilteredCustomers = filteredCustomers.filter { it.status != "TERHAPUS" }
    val totalPendapatanArea = activeFilteredCustomers.sumOf { it.getTotalBillAmount() }

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    val localFocusManager = LocalFocusManager.current

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari pelanggan...", color = textMain.copy(alpha = 0.5f), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textMain, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = neonCyan
                            ),
                            singleLine = true
                        )
                    } else {
                        Text("Daftar Pelanggan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchActive = !isSearchActive 
                        if (!isSearchActive) searchQuery = ""
                    }) {
                        Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search", tint = textMain)
                    }
                    if (!isSearchActive) {
                        IconButton(onClick = { loadCustomersFromRemote() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = textMain)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomer,
                containerColor = neonCyan,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pelanggan")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { localFocusManager.clearFocus() })
                }
        ) {
            var showDeleteConfirm by remember { mutableStateOf(false) }
    var customerToDeleteState by remember { mutableStateOf<Customer?>(null) }
    var expanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Global", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(totalPendapatanGlobal)}",
                            color = neonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total $selectedArea", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(totalPendapatanArea)}",
                            color = neonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter Area", color = textMain.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
                            unfocusedTrailingIconColor = textMain.copy(alpha = 0.5f),
                            focusedLabelColor = neonCyan,
                            unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = headerBg
                    ) {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = { Text(area, color = if (selectedArea == area) neonCyan else textMain) },
                                onClick = {
                                    selectedArea = area
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
    
    if (showDeleteConfirm && customerToDeleteState != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Konfirmasi") },
            text = { Text("Yakin ingin menghapus pelanggan ${customerToDeleteState?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.deleteCustomer(customerToDeleteState!!.id)
                            val delId = customerToDeleteState!!.id.toIntOrNull()
                            if (delId != null) {
                                db.pelangganDao().deleteById(delId)
                            }
                            showDeleteConfirm = false
                            customerToDeleteState = null
                        } catch (e: Exception) {}
                    }
                }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = textMain)
                }
            },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textMain
        )
    }

    when {
        isRefreshing && localPelangganList.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = neonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memuat data pelanggan...", color = textSecondary, fontSize = 14.sp)
                }
            }
        }
        errorMessage != null && localPelangganList.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        errorMessage ?: "Gagal memuat data pelanggan",
                        color = textMain,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { loadCustomersFromRemote() },
                        colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Coba Lagi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        filteredCustomers.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = textSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        when {
                            searchQuery.isNotBlank() -> "Tidak ada pelanggan yang cocok dengan pencarian" 
                            selectedArea != "Semua" -> "Tidak ada pelanggan di area $selectedArea"
                            localPelangganList.isNotEmpty() && customers.isEmpty() -> "Tidak ada pelanggan di area akses akun Anda"
                            else -> "Belum ada data pelanggan"
                        },
                        color = textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { loadCustomersFromRemote() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                            border = BorderStroke(1.dp, neonCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Muat Ulang")
                        }
                        Button(
                            onClick = onNavigateToAddCustomer,
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tambah Pelanggan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers) { customer ->
                    CustomerItem(
                        customer = customer, 
                        onNavigateToCustomerDetail = onNavigateToCustomerDetail, 
                        onDeleteCustomer = { customerToDelete ->
                            customerToDeleteState = customerToDelete
                            showDeleteConfirm = true
                        }, 
                        onIsolirCustomer = { customerToIsolir ->
                            coroutineScope.launch {
                                try {
                                    val isCustIsolir = customerToIsolir.status.contains("ISOLIR", ignoreCase = true) ||
                                                       (!customerToIsolir.isolateDate.isNullOrBlank() && customerToIsolir.isolateDate != "-")
                                    if (isCustIsolir) {
                                        val resp = com.example.ui.data.remote.ApiClient.apiService.unIsolateCustomer(customerToIsolir.id)
                                        val custIdInt = customerToIsolir.id.toIntOrNull() ?: 0
                                        if (custIdInt > 0) {
                                            db.pelangganDao().updateStatus(custIdInt, "BELUM BAYAR")
                                        }
                                        android.widget.Toast.makeText(context, resp.message ?: "Berhasil membuka isolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        val resp = com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customerToIsolir.id)
                                        val custIdInt = customerToIsolir.id.toIntOrNull() ?: 0
                                        if (custIdInt > 0) {
                                            db.pelangganDao().updateStatus(custIdInt, "ISOLIR")
                                        }
                                        android.widget.Toast.makeText(context, resp.message ?: "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }, 
                        onEditCustomer = { id ->
                            onNavigateToEditCustomer(id)
                        }
                    )
                }
            }
        }
    }
}
}
}

@JsonClass(generateAdapter = true)
data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val area: String = "",
    val address: String? = null,
    val alamat: String? = null,
    val username: String = "",
    val billingDate: String = "",
    val status: String = "",
    val price: String = "",
    val discount: String = "",
    @Json(name = "register_date") val registerDate: String? = null,
    @Json(name = "isolate_date") val isolateDate: String? = null,
    @Json(name = "package_name") val packageName: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null,
    @Json(name = "additional_cost1") val additional_cost1: String? = null,
    @Json(name = "additional_cost2") val additional_cost2: String? = null,
    val additionalCostDesc1: String? = null,
    val additionalCostDesc2: String? = null,
    @Json(name = "additional_cost_desc1") val additional_cost_desc1: String? = null,
    @Json(name = "additional_cost_desc2") val additional_cost_desc2: String? = null,
    @Json(name = "pppoe_secret") val pppoeSecret: String? = null,
    @Json(name = "odp_id") val odpId: String? = null,
    @Json(name = "odp_port") val odpPort: String? = null
) {
    val register_date: String? get() = registerDate
    val isolate_date: String? get() = isolateDate

    fun getEffectiveRegisterDate(): String? {
        return registerDate?.takeIf { it.isNotBlank() }
    }

    fun getEffectiveCost1(): String? {
        return additionalCost1?.takeIf { it.isNotBlank() } ?: additional_cost1?.takeIf { it.isNotBlank() }
    }

    fun getEffectiveCost2(): String? {
        return additionalCost2?.takeIf { it.isNotBlank() } ?: additional_cost2?.takeIf { it.isNotBlank() }
    }

    fun getEffectiveCostDesc1(): String? {
        return additionalCostDesc1?.takeIf { it.isNotBlank() } ?: additional_cost_desc1?.takeIf { it.isNotBlank() }
    }

    fun getEffectiveCostDesc2(): String? {
        return additionalCostDesc2?.takeIf { it.isNotBlank() } ?: additional_cost_desc2?.takeIf { it.isNotBlank() }
    }
    fun getEffectiveAddress(): String {
        return address?.takeIf { it.isNotBlank() } ?: alamat?.takeIf { it.isNotBlank() } ?: ""
    }

    fun getBasePriceAmount(): Long {
        return com.example.ui.util.InvoiceGenerator.parseInvoiceAmount(price)
    }

    fun getDiscountAmount(): Long {
        return com.example.ui.util.InvoiceGenerator.parseInvoiceAmount(discount)
    }

    fun getAdditionalCost1Amount(): Long {
        return com.example.ui.util.InvoiceGenerator.parseInvoiceAmount(getEffectiveCost1())
    }

    fun getAdditionalCost2Amount(): Long {
        return com.example.ui.util.InvoiceGenerator.parseInvoiceAmount(getEffectiveCost2())
    }

    fun getTotalAdditionalCost(): Long {
        return getAdditionalCost1Amount() + getAdditionalCost2Amount()
    }

    fun getTotalBillAmount(): Long {
        val total = getBasePriceAmount() - getDiscountAmount() + getTotalAdditionalCost()
        return total.coerceAtLeast(0L)
    }

    fun getFormattedTotalBill(): String {
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
        return "Rp. ${formatter.format(getTotalBillAmount())}"
    }
}

@Composable
fun CustomerItem(
    customer: Customer, 
    onNavigateToCustomerDetail: (String) -> Unit, 
    onDeleteCustomer: (Customer) -> Unit, 
    onIsolirCustomer: (Customer) -> Unit = {}, 
    onEditCustomer: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val greenText = Color(0xFF00FF4D)
    val errorRed = Color(0xFFFF003C)
    val redText = Color(0xFFFF4C4C)
    
    val isNewCustomer = try {
        if (!customer.registerDate.isNullOrEmpty() && !customer.billingDate.isNullOrEmpty()) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val regDate = sdf.parse(customer.registerDate)
            val today = sdf.parse(sdf.format(java.util.Date()))
            
            val dayNumber = try {
                val parsedDate = sdf.parse(customer.billingDate)
                if (parsedDate != null) {
                    val cal = java.util.Calendar.getInstance()
                    cal.time = parsedDate
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                } else {
                    customer.billingDate.toIntOrNull()
                }
            } catch (e: Exception) {
                customer.billingDate.toIntOrNull()
            }
            
            var billDate: java.util.Date? = null
            if (dayNumber != null && regDate != null) {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = regDate
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                val maxDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                val targetDay = if (dayNumber > maxDay) maxDay else dayNumber
                calendar.set(java.util.Calendar.DAY_OF_MONTH, targetDay)
                if (calendar.time.before(regDate)) {
                    calendar.add(java.util.Calendar.MONTH, 1)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val nextMax = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    val nextTarget = if (dayNumber > nextMax) nextMax else dayNumber
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, nextTarget)
                }
                billDate = calendar.time
            }
            
            if (billDate != null && today != null) {
                today.before(billDate)
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
            .clickable { onNavigateToCustomerDetail(customer.id) }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Info Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(neonCyan.copy(alpha = 0.15f))
                            .border(0.5.dp, neonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = customer.id, color = neonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textMain, maxLines = 1)
                }
                
                Text(text = "${customer.phone} • ${customer.area}", fontSize = 11.sp, color = textSecondary, maxLines = 1)
                
                val hasDiscount = !customer.discount.isNullOrEmpty() && customer.discount != "0" && !customer.discount.contains("Rp. 0") && !customer.discount.contains("Dskn : Rp. 0")
                if (isNewCustomer || customer.status.contains("ISOLIR", ignoreCase = true) || hasDiscount) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isNewCustomer) {
                            Text(
                                text = "PELANGGAN BARU",
                                fontSize = 11.sp,
                                color = Color(0xFF00FFD2),
                                fontWeight = FontWeight.Bold
                            )
                        } else if (customer.status.contains("ISOLIR", ignoreCase = true)) {
                            Text(
                                text = "ISOLIR",
                                fontSize = 11.sp,
                                color = errorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (hasDiscount) {
                            Text(
                                text = customer.discount.replace("- Dskn : ", "Dskn: "),
                                fontSize = 11.sp,
                                color = greenText
                            )
                        }
                    }
                }
            }
            
            // Right Price & Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = customer.getFormattedTotalBill(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                if (customer.getTotalAdditionalCost() > 0L || customer.getDiscountAmount() > 0L) {
                    Text(text = "Paket: ${customer.price}", fontSize = 10.sp, color = textSecondary)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (customer.status.contains("BELUM BAYAR", ignoreCase = true)) {
                        IconButton(
                            onClick = { onIsolirCustomer(customer) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Isolir", tint = errorRed, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = { onEditCustomer(customer.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = neonCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDeleteCustomer(customer) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

