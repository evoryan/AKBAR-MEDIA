package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.UserSession
import com.example.ui.data.InventoryItem
import com.example.ui.data.StockHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockBarangScreen(
    onBack: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToKategori: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    var showTxDialog by remember { mutableStateOf(false) }
    var inventoryList by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }
    var historyList by remember { mutableStateOf<List<StockHistory>>(emptyList()) }
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val refreshData = {
        coroutineScope.launch {
            try {
                inventoryList = ApiClient.apiService.getInventory()
                historyList = ApiClient.apiService.getStockHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val startOfToday = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    val itemsTakenToday = historyList.filter { it.timestamp >= startOfToday && it.type == "OUT" }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Stock Barang", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { showTxDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBg,
                        contentColor = Color.Black
                    )
                ) {
                    Text("TRANSAKSI BARANG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            // Grid Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StockMenuCard(
                    title = "Barang",
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFF00FF4D), // Greenish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventory
                )
                StockMenuCard(
                    title = "Kategori",
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFFFB300), // Yellowish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToKategori
                )
                StockMenuCard(
                    title = "History",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF00BFFF), // Blueish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Barang Terambil hari ini :", color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (itemsTakenToday.isEmpty()) {
                        Text(
                            "Tidak ada Data",
                            color = textMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(itemsTakenToday) { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tx.itemName, color = textMain, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        Text("Oleh: ${tx.adminName}", color = textSecondary, fontSize = 11.sp)
                                    }
                                    Text("-${tx.quantity}", color = Color(0xFFFF003C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTxDialog) {
        var selectedItem by remember { mutableStateOf<InventoryItem?>(inventoryList.firstOrNull()) }
        var txType by remember { mutableStateOf("OUT") }
        var quantityStr by remember { mutableStateOf("") }
        var itemExpanded by remember { mutableStateOf(false) }
        var typeExpanded by remember { mutableStateOf(false) }

        LaunchedEffect(inventoryList) {
            if (selectedItem == null) {
                selectedItem = inventoryList.firstOrNull()
            }
        }

        AlertDialog(
            onDismissRequest = { showTxDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text("Transaksi Barang", fontWeight = FontWeight.Bold, color = textMain) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pilih Barang", color = textSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { itemExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Text(selectedItem?.let { "${it.name} (Stock: ${it.stock})" } ?: "Pilih Barang")
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                        }
                        DropdownMenu(
                            expanded = itemExpanded,
                            onDismissRequest = { itemExpanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder).fillMaxWidth(0.8f)
                        ) {
                            inventoryList.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} (Stock: ${item.stock})", color = textMain) },
                                    onClick = {
                                        selectedItem = item
                                        itemExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Text("Tipe Transaksi", color = textSecondary, fontSize = 12.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { typeExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Text(if (txType == "IN") "Masuk (IN)" else "Keluar (OUT)")
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                        }
                        DropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Masuk (IN)", color = textMain) },
                                onClick = {
                                    txType = "IN"
                                    typeExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Keluar (OUT)", color = textMain) },
                                onClick = {
                                    txType = "OUT"
                                    typeExpanded = false
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Jumlah") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val item = selectedItem
                        if (item != null && qty > 0) {
                            coroutineScope.launch {
                                try {
                                    val txData = mapOf(
                                        "type" to txType,
                                        "itemName" to item.name,
                                        "quantity" to qty.toString(),
                                        "adminName" to (currentUser?.name ?: "Admin"),
                                        "timestamp" to System.currentTimeMillis().toString(),
                                        "itemId" to item.id
                                    )
                                    ApiClient.apiService.addStockHistory(txData)
                                    refreshData()
                                    showTxDialog = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                ) {
                    Text("Simpan", color = primaryBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTxDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}

@Composable
fun StockMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF11111A))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
