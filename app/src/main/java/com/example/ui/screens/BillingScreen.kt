package com.example.ui.screens

import androidx.compose.material.icons.filled.Lock


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

    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var customerToCancel by remember { mutableStateOf<Customer?>(null) }
    var cancelPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    fun fetchCustomers() {
        coroutineScope.launch {
            try {
                customers = ApiClient.apiService.getCustomers().filter { it.status != "TERHAPUS" }
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
    val filteredBySearch = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { it.name.contains(searchQuery, ignoreCase = true) }
    
    val unpaidCustomers = filteredBySearch.filter { it.status != "LUNAS CASH" }
    val paidCustomers = filteredBySearch.filter { it.status == "LUNAS CASH" }
    
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val unpaidSum = unpaidCustomers.sumOf { it.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
    val paidSum = paidCustomers.sumOf { it.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
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
                                    customers = customers.map { if (it.id == customerToCancel!!.id) it.copy(status = "BELUM BAYAR") else it }
                                    Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                                    showCancelDialog = false
                                    cancelPassword = ""
                                    customerToCancel = null
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

    Scaffold(containerColor = bgMain,
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
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
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

            // Dropdown Area Filter
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
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
                        containerColor = Color(0xFF11111A)
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

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Pelanggan", color = textMain.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain,
                    focusedLabelColor = neonCyan,
                    unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                ),
                singleLine = true
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // BELUM BAYAR Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorRed.copy(alpha = 0.2f))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Total Tagihan Belum Dibayar", color = textSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(totalUnpaid, color = errorRed, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                    
                    Text("Daftar Pelanggan Belum Bayar", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unpaidCustomers) { customer ->
                            BillingCustomerItem(
                                customer = customer, 
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
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                } else {
                    // SUDAH BAYAR Summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(successGreen.copy(alpha = 0.2f))
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Total Tagihan Sudah Dibayar", color = textSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(totalPaid, color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                    }
                    
                    Text("Daftar Pelanggan Sudah Bayar", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(paidCustomers) { customer ->
                            BillingCustomerItem(
                                customer = customer, 
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = {},
                                onDetailClick = {
                                    val amount = customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "Mei 2026")
                                },
                                onLongPress = {
                                    customerToCancel = customer
                                    showCancelDialog = true
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
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
    onIsolirClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))

            .padding(12.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(customer.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(customer.phone, color = textSecondary, fontSize = 12.sp)
                    Text("Area: ${customer.area}", color = textSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (customer.status != "LUNAS CASH") Color(0xFFFF003C).copy(alpha = 0.2f) else neonCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(customer.status, color = if (customer.status != "LUNAS CASH") Color(0xFFFF003C) else neonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onIsolirClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = "Isolir", tint = Color(0xFFD4AF37))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    } else {
                        Button(
                            onClick = onLongPress,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFFF003C)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF003C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BATAL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            if (customer.status != "LUNAS CASH") {
                                onPayClick()
                            } else {
                                onDetailClick()
                            }
                        },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (customer.status != "LUNAS CASH") neonCyan else Color.Transparent, contentColor = if (customer.status != "LUNAS CASH") Color.Black else neonCyan),
                        border = if (customer.status == "LUNAS CASH") androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (customer.status != "LUNAS CASH") "BAYAR" else "DETAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
