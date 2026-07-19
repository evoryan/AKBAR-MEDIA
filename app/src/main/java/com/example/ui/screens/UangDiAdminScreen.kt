package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient

data class AdminData(
    val name: String,
    val totalDiterima: String,
    val setor: String,
    val sisa: String,
    val sisaColor: Color,
    val persentase: String,
    val pengeluaran: String,
    val persentaseSisa: String,
    val jmlPlggn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UangDiAdminScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBgLighter = Color(0xFF1A1A2A)
    val successGreen = Color(0xFF00C853)
    val errorRed = Color(0xFFFF5252)
    val warningYellow = Color(0xFFFFC107)
    val primaryCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }

    var adminList by remember { mutableStateOf<List<AdminData>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedAdmin by remember { mutableStateOf<AdminData?>(null) }
    var dialogPayments by remember { mutableStateOf<List<com.example.ui.data.remote.PembayaranHistoryItem>>(emptyList()) }
    var isLoadingDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        try {
            val admins = ApiClient.apiService.getAdmins()
            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }
            val allPayments = try { ApiClient.apiService.getPembayaranHistory() } catch (e: Exception) { emptyList() }
            
            val mapped = admins.map { admin ->
                val record = uangAdmin.find { it.adminName == admin.name }
                val diterima = record?.totalDiterima ?: 0.0
                val setor = record?.setor ?: 0.0
                val pengeluaran = record?.pengeluaran ?: 0.0
                val sisa = diterima - setor
                
                val formattedDiterima = "Rp. ${String.format("%,d", diterima.toLong()).replace(",", ".")}"
                val formattedSetor = "Rp. ${String.format("%,d", setor.toLong()).replace(",", ".")}"
                val formattedPengeluaran = "Rp. ${String.format("%,d", pengeluaran.toLong()).replace(",", ".")}"
                val formattedSisa = "Rp. ${String.format("%,d", sisa.toLong()).replace(",", ".")}"
                
                val jml = record?.jmlPlggn ?: 0
                AdminData(
                    name = admin.name,
                    totalDiterima = formattedDiterima,
                    setor = formattedSetor,
                    sisa = formattedSisa,
                    sisaColor = if (sisa < 0) errorRed else if (sisa == 0.0) successGreen else warningYellow,
                    persentase = "100%", // could be calculated if we have a target
                    pengeluaran = formattedPengeluaran,
                    persentaseSisa = "100%",
                    jmlPlggn = jml.toString()
                )
            }
            adminList = mapped
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(modifier = Modifier.background(bgMain)) {
                TopAppBar(
                    title = { Text("Sisa Uang Bulan $currentMonthYear", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
                )
                Text(
                    text = "Berikut List Sisa Uang Di Admin.\nPilih admin untuk menambahkan setoran",
                    color = textMain,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Tap Tahan untuk melihat opsi lain.",
                    color = textSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(adminList) { admin ->
                AdminItemCard(
                    onPelangganClick = { selectedAdmin = admin; showDialog = true; coroutineScope.launch { try { isLoadingDialog = true; val allPayments = com.example.ui.data.remote.ApiClient.apiService.getPembayaranHistory(); dialogPayments = allPayments.filter { it.admin_name == admin.name } } catch(e: Exception) {} finally { isLoadingDialog = false } } }, 
                    admin = admin,
                    textMain = textMain,
                    textSecondary = textSecondary,
                    successGreen = successGreen,
                    errorRed = errorRed,
                    primaryCyan = primaryCyan,
                    cardBg = cardBg,
                    onSetorSuccess = { refreshTrigger++ }
                )
            }
        }
    }
if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text("Pelanggan - ${selectedAdmin?.name}") },
            text = {
                if (isLoadingDialog) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryCyan)
                    }
                } else if (dialogPayments.isEmpty()) {
                    Text("Tidak ada data", color = textSecondary)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(dialogPayments) { payment ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text("${payment.customer_name ?: "-"} (${payment.bulan} ${payment.tahun})", color = textMain, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(payment.created_at ?: "-", color = textSecondary, fontSize = 12.sp)
                                    Text("Rp. ${String.format("%,d", payment.amount?.toLong() ?: 0).replace(",", ".")}", color = successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = cardBgLighter)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup", color = primaryCyan)
                }
            }
        )
    }
}

@Composable
fun AdminItemCard(
    onPelangganClick: () -> Unit = {},
    admin: AdminData,
    textMain: Color,
    textSecondary: Color,
    successGreen: Color,
    errorRed: Color,
    primaryCyan: Color,
    cardBg: Color,
    onSetorSuccess: () -> Unit = {}
) {
    var showSetorDialog by remember { mutableStateOf(false) }
    var nominalSetoran by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(admin.name, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { showSetorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Setor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                // Column 1
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total Diterima", color = textSecondary, fontSize = 12.sp)
                    Text(admin.totalDiterima, color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Persentase (${admin.persentase})", color = textSecondary, fontSize = 12.sp)
                    Text(admin.persentase, color = primaryCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onPelangganClick() }
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Jml Plggn", color = textSecondary, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(admin.jmlPlggn, color = primaryCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                // Column 2
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Setor", color = textSecondary, fontSize = 12.sp)
                    Text(admin.setor, color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Pengeluaran", color = textSecondary, fontSize = 12.sp)
                    Text(admin.pengeluaran, color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                
                // Column 3
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sisa", color = textSecondary, fontSize = 12.sp)
                    Text(admin.sisa, color = admin.sisaColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onPelangganClick() }) {
                        Text("% Sisa (${admin.persentaseSisa})", color = textSecondary, fontSize = 12.sp)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = textSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(admin.persentaseSisa, color = primaryCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSetorDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) { showSetorDialog = false; updateResult = null } },
            containerColor = Color.Transparent,
            title = { Text("Setor Uang", color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("masukkan nominal jumlah setoran", color = textSecondary, fontSize = 14.sp)
                    
                    OutlinedTextField(
                        value = nominalSetoran,
                        onValueChange = { nominalSetoran = it },
                        label = { Text("Jumlah Setor", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    if (isUpdating) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(color = primaryCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Memproses...", color = primaryCyan, fontSize = 12.sp)
                        }
                    } else if (updateResult != null) {
                        Text(
                            text = updateResult ?: "",
                            color = successGreen,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        isUpdating = true
                        updateResult = null
                        coroutineScope.launch {
                            try {
                                val amount = nominalSetoran.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
                                com.example.ui.data.remote.ApiClient.apiService.addSetoran(
                                    com.example.ui.data.remote.SetoranRequest(admin.name, amount)
                                )
                                isUpdating = false
                                updateResult = "Setoran sebesar Rp. $nominalSetoran berhasil ditambahkan!"
                                nominalSetoran = ""
                                onSetorSuccess()
                            } catch (e: Exception) {
                                isUpdating = false
                                updateResult = "Gagal menyetor uang"
                            }
                        }
                    },
                    enabled = !isUpdating && nominalSetoran.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black)
                ) {
                    Text("Konfirmasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetorDialog = false; updateResult = null }, enabled = !isUpdating) {
                    Text("Batal", color = textMain)
                }
            }
        )
    }

    }
