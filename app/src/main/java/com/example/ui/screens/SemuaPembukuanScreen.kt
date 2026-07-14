package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemuaPembukuanScreen(initialType: String = "Pilih Tipe Pembukuan", onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val primaryBlue = Color(0xFF2196F3)
    val warningYellow = Color(0xFFFF9800)
    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFF44336)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)

    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }
    
    var selectedMonth by remember { mutableStateOf(currentMonthYear) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    val months = listOf("Mei 2026", "Juni 2026", "Juli 2026", "Agustus 2026")

    var showAddDialog by remember { mutableStateOf(false) }
    var keterangan by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var tipePembukuan by remember { mutableStateOf(if (initialType.isNotEmpty()) initialType else "Pilih Tipe Pembukuan") }
    var tipeDropdownExpanded by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Pilih Tipe Pembukuan", "Pemasukan", "Pengeluaran", "Setor")

    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = bgMain,
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF11111A))) {
                TopAppBar(
                    title = { Text("Pembukuan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Berikut Pembukuan Bulan :", color = textMain, fontSize = 14.sp)
                        Box {
                            Text(
                                selectedMonth,
                                color = warningYellow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { monthDropdownExpanded = true }.padding(vertical = 4.dp)
                            )
                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false },
                                containerColor = Color(0xFF11111A)
                            ) {
                                months.forEach { month ->
                                    DropdownMenuItem(
                                        text = { Text(month, color = Color.White) },
                                        onClick = {
                                            selectedMonth = month
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = bgMain,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11111A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pengeluaran", color = textMain, fontSize = 14.sp)
                    Text("-", color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pemasukan Lain2", color = textMain, fontSize = 14.sp)
                    Text("-", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Setor", color = textMain, fontSize = 14.sp)
                    Text("-", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari Sesuatu...", color = textSecondary) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = textSecondary,
                        unfocusedIndicatorColor = textSecondary,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Tidak ada Data",
                color = textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = bgMain,
            title = {
                Text(
                    "Transaksi",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Bulan Input", color = textSecondary, fontSize = 12.sp)
                        Text("(Ini berdasarkan pada pemilihan bulan di beranda)", color = textSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(selectedMonth, color = primaryBlue, fontSize = 14.sp)
                    }

                    TextField(
                        value = keterangan,
                        onValueChange = { keterangan = it },
                        label = { Text("Keterangan", color = textSecondary) },
                        placeholder = { Text("Keterangan...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    TextField(
                        value = jumlah,
                        onValueChange = { jumlah = it },
                        label = { Text("Jumlah", color = textSecondary) },
                        placeholder = { Text("Jumlah", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    Column {
                        Text("Type", color = textSecondary, fontSize = 12.sp)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { tipeDropdownExpanded = true }.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tipePembukuan, color = textMain, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary)
                            }
                            DropdownMenu(
                                expanded = tipeDropdownExpanded,
                                onDismissRequest = { tipeDropdownExpanded = false },
                                containerColor = Color(0xFF11111A),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                tipeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color.White) },
                                        onClick = {
                                            tipePembukuan = option
                                            tipeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                try {
                                    val amountStr = jumlah.replace(Regex("[^0-9]"), "")
                                    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
                                    var type = "pemasukan"
                                    var category = "Lain-lain"
                                    
                                    if (tipePembukuan == "Pemasukan") {
                                        type = "pemasukan"
                                        category = "Pemasukkan Lain2"
                                    } else if (tipePembukuan == "Pengeluaran") {
                                        type = "pengeluaran"
                                        category = "Lain-lain"
                                    } else if (tipePembukuan == "Setor") {
                                        type = "setor"
                                        category = "Lain-lain"
                                    }
                                    
                                    ApiClient.apiService.addPembukuan(
                                        com.example.ui.data.remote.PembukuanRequest(
                                            type, category, amountDouble, keterangan
                                        )
                                    )
                                    showAddDialog = false
                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("SIMPAN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
