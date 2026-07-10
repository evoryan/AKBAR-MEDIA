package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.ui.data.remote.PembukuanRequest
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembukuanScreen(onNavigateToBilling: (Int) -> Unit, onBack: () -> Unit, onNavigateToUangDiAdmin: () -> Unit, onNavigateToPembayaranByAdmin: () -> Unit, onNavigateToSemuaPembukuan: (String) -> Unit, onNavigateToRangkuman: () -> Unit) {
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val successGreen = Color(0xFF00C853)
    val errorRed = Color(0xFFFF5252)
    val primaryCyan = Color(0xFF00FFFF)
    val currentUser by UserSession.currentUser.collectAsState()
    var pemasukan by remember { mutableStateOf(0L) }
    var pengeluaran by remember { mutableStateOf(0L) }
    var categories by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var pengeluaranDetails by remember { mutableStateOf<List<com.example.ui.data.remote.PengeluaranItem>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }

    fun fetchData() {
        coroutineScope.launch {
            try {
                val res = ApiClient.apiService.getPembukuan()
                pemasukan = res.pemasukan
                pengeluaran = res.pengeluaran
                categories = res.categories
                
                try {
                    pengeluaranDetails = ApiClient.apiService.getPengeluaranDetail()
                } catch (e: Exception) {}
            } catch (e: Exception) {
            }
        }
    }
    LaunchedEffect(Unit) {
        fetchData()
    }



    
    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Pembukuan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pemasukkan
            if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pemasukkan", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rp. ${String.format("%,d", pemasukan).replace(",", ".")}", color = successGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { fetchData() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = primaryCyan)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = textSecondary.copy(alpha = 0.2f))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PembukuanItem("Transaksi Cash", "Rp. ${ (categories["Transaksi Cash"] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f).clickable { onNavigateToBilling(1) })
                            PembukuanItem("Transaksi Online", "Rp. ${ (categories["Transaksi Online"] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PembukuanItem(
                                "Total Pemasukkan Lain2", 
                                "Rp. ${categories["Pemasukkan Lain2"] ?: 0L.let { String.format("%,d", it).replace(",", ".") }}", 
                                successGreen, 
                                modifier = Modifier.weight(1f).clickable { onNavigateToSemuaPembukuan("Pemasukan") }
                            )
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            
            }
            if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
            // Pengeluaran
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pengeluaran", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Rp. ${String.format("%,d", pengeluaran).replace(",", ".")}", color = errorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { fetchData() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = primaryCyan)
                                }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = textSecondary.copy(alpha = 0.2f))
                        
                        val pengeluaranItems = listOf(
                            "Gaji Karyawan", "Pasang Baru",
                            "Perbaikan Alat", "Bayar Bandwidth",
                            "Bayar Kang Tagih", "Listrik / PDAM / Pulsa",
                            "Bayar Marketing", "Lain-lain"
                        )
                        
                        for (i in pengeluaranItems.indices step 2) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val cat1 = pengeluaranItems[i]
                                PembukuanItem(
                                    cat1, 
                                    "Rp. ${ (categories[cat1] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", 
                                    errorRed, 
                                    modifier = Modifier.weight(1f).clickable {
                                        selectedCategory = cat1
                                        selectedType = "pengeluaran"
                                        val detail = pengeluaranDetails.find { it.category == cat1 }
                                        inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                        inputDescription = detail?.description ?: ""
                                        showAddDialog = true
                                    }
                                )
                                if (i + 1 < pengeluaranItems.size) {
                                    val cat2 = pengeluaranItems[i + 1]
                                    PembukuanItem(
                                        cat2, 
                                        "Rp. ${ (categories[cat2] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", 
                                        errorRed, 
                                        modifier = Modifier.weight(1f).clickable {
                                            selectedCategory = cat2
                                            selectedType = "pengeluaran"
                                            val detail = pengeluaranDetails.find { it.category == cat2 }
                                            inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                            inputDescription = detail?.description ?: ""
                                            showAddDialog = true
                                        }
                                    )
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                            if (i < pengeluaranItems.size - 2) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            
            }
            if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
            // Total Pendapatan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Pendapatan", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = textSecondary.copy(alpha = 0.2f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Pemasukkan", color = textMain, fontSize = 12.sp)
                                Text("Rp. ${ String.format("%,d", pemasukan).replace(",", ".") }", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("-", color = errorRed, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Pengeluaran", color = textMain, fontSize = 12.sp)
                                Text("Rp. ${ String.format("%,d", pengeluaran).replace(",", ".") }", color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(16.dp))
                                .background(successGreen)
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pendapatan", color = Color.White, fontSize = 12.sp)
                                Text("Rp. ${ String.format("%,d", pemasukan - pengeluaran).replace(",", ".") }", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            
            }
    
        // Menu Lain
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Menu Lain",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                            MenuLainItem(
                                icon = Icons.Default.AccountCircle,
                                title = "Uang di Admin",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToUangDiAdmin
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        MenuLainItem(
                            icon = Icons.Default.List,
                            title = "Semua Pembukuan",
                            tint = Color(0xFF03A9F4),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToSemuaPembukuan("") }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.COLLECTOR) {
                            MenuLainItem(
                                icon = Icons.Default.DirectionsRun,
                                title = "Pembyrn By Admin",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToPembayaranByAdmin
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                            MenuLainItem(
                                icon = Icons.Default.PieChart,
                                title = "Rangkuman Keuangan",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToRangkuman
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF11111A),
                title = { Text("Input Nominal ($selectedCategory)", color = textMain, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("Nominal (Rp)", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputDescription,
                            onValueChange = { inputDescription = it },
                            label = { Text("Keterangan", color = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val amount = inputAmount.toLongOrNull() ?: 0L
                                    if (selectedType == "pengeluaran") {
                                        ApiClient.apiService.updatePengeluaranDetail(com.example.ui.data.remote.PengeluaranRequest(selectedCategory, amount, inputDescription))
                                    } else {
                                        ApiClient.apiService.addPembukuan(com.example.ui.data.remote.PembukuanRequest(selectedType, selectedCategory, amount.toDouble(), inputDescription))
                                    }
                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Batal", color = textMain)
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}

@Composable
fun PembukuanItem(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MenuLainItem(icon: ImageVector, title: String, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(36.dp))
        Text(title, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
