package com.example.ui.screens

import com.squareup.moshi.Json
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
        }.filter { UserSession.isAreaNameAllowed(it.area) }
    }

    LaunchedEffect(Unit) {
        try {
            UserSession.getOrFetchAreas()
            val syncResponse = ApiClient.apiService.syncData()
            val mapped = syncResponse.customers.map { item ->
                PelangganEntity(
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
            db.pelangganDao().deleteAll()
            db.pelangganDao().insertAll(mapped)
        } catch (e: Exception) {
            // Silently fall back to cached copy
        }
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
    val totalPendapatanGlobal = activeCustomers.sumOf { it.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
    val activeFilteredCustomers = filteredCustomers.filter { it.status != "TERHAPUS" }
    val totalPendapatanArea = activeFilteredCustomers.sumOf { it.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
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
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = textMain)
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

        LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCustomers) { customer ->
                    
                    CustomerItem(customer, onNavigateToCustomerDetail, onDeleteCustomer = { customerToDelete ->
                        customerToDeleteState = customerToDelete
                        showDeleteConfirm = true
                    }, onIsolirCustomer = { customerToIsolir ->
                        coroutineScope.launch {
                            try {
                                com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customerToIsolir.id)
                                android.widget.Toast.makeText(context, "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Gagal mengisolir pelanggan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, onEditCustomer = { id ->
                        onNavigateToEditCustomer(id)
                    })

                }
            }
        }
    }
}

data class Customer(
    val id: String = "",
    val name: String,
    val phone: String,
    val area: String,
    val address: String? = null,
    val username: String,
    val billingDate: String,
    val status: String,
    val price: String,
    val discount: String,
    @Json(name = "register_date") val registerDate: String? = null,
    @Json(name = "isolate_date") val isolateDate: String? = null,
    @Json(name = "package_name") val packageName: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null,
    @Json(name = "pppoe_secret") val pppoeSecret: String? = null,
    @Json(name = "odp_id") val odpId: String? = null,
    @Json(name = "odp_port") val odpPort: String? = null
)

@Composable
fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit, onIsolirCustomer: (Customer) -> Unit = {}, onEditCustomer: (String) -> Unit = {}) {
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
            val billDate = sdf.parse(customer.billingDate)
            val today = java.util.Date()
            if (regDate != null && billDate != null) {
                val belumMemasukiMasaTagihan = today.before(billDate)
                val diffInMillis = kotlin.math.abs(billDate.time - regDate.time)
                val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                belumMemasukiMasaTagihan && diffInDays < 10
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }

    val displayStatus = if (isNewCustomer) "PELANGGAN BARU" else customer.status
    val statusColor = if (isNewCustomer) Color(0xFF00FFD2) else if (displayStatus.contains("LUNAS")) neonCyan else redText

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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayStatus,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (!customer.discount.isNullOrEmpty() && customer.discount != "0" && !customer.discount.contains("Rp. 0") && !customer.discount.contains("Dskn : Rp. 0")) {
                        Text(
                            text = customer.discount.replace("- Dskn : ", "Dskn: "),
                            fontSize = 11.sp,
                            color = greenText
                        )
                    }
                }
            }
            
            // Right Price & Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = customer.price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                
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

