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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onBack: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit,
    onNavigateToAddCustomer: () -> Unit
) {
        var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            customers = ApiClient.apiService.getCustomers()
        } catch (e: Exception) {
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
                            customers = customers.filter { it.id != customerToDeleteState!!.id }
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCustomers) { customer ->
                    
                    CustomerItem(customer, onNavigateToCustomerDetail, onDeleteCustomer = { customerToDelete ->
                        customerToDeleteState = customerToDelete
                        showDeleteConfirm = true
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
fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit) {
    val context = LocalContext.current
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val yellowBadge = Color(0xFFD4AF37)
    val greenText = Color(0xFF00FF4D)
    val errorRed = Color(0xFFFF003C)
    val redText = Color(0xFFFF4C4C)
    
    val statusColor = if (customer.status.contains("LUNAS")) neonCyan else redText

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { onNavigateToCustomerDetail(customer.id) }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Left Column
            Column(modifier = Modifier.weight(1f)) {
                Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textMain)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${customer.phone} • ${customer.area}", fontSize = 12.sp, color = textSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Status :", fontSize = 12.sp, color = textSecondary)
                Text(text = customer.status, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold)
            }
            
            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { Toast.makeText(context, "Fitur edit pelanggan akan segera hadir", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = neonCyan)
                    }
                    IconButton(onClick = { onDeleteCustomer(customer) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(neonCyan.copy(alpha = 0.2f))
                        .border(1.dp, neonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = customer.id, color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = customer.price, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)
                Text(text = customer.discount, fontSize = 12.sp, color = greenText)
            }
        }
    }
}

