package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBgLighter = Color(0xFF1A1A2A)
    val successGreen = Color(0xFF00C853)
    val errorRed = Color(0xFFFF5252)
    val warningYellow = Color(0xFFFFC107)
    val primaryCyan = Color(0xFF00FFFF)

    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
    }

    val adminList = listOf(
        AdminData(
            name = "PT.Akbar Media Group",
            totalDiterima = "Rp. 125.000",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "1"
        ),
        AdminData(
            name = "Fitri",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        ),
        AdminData(
            name = "Al-Mufit",
            totalDiterima = "Rp. 2.450.000",
            setor = "Rp. 0",
            sisa = "Rp. 2.450.000",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "21"
        ),
        AdminData(
            name = "kholis",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        ),
        AdminData(
            name = "admin",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF141F4A))) {
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
                    admin = admin,
                    textMain = textMain,
                    textSecondary = textSecondary,
                    successGreen = successGreen,
                    errorRed = errorRed,
                    primaryCyan = primaryCyan,
                    cardBg = cardBg
                )
            }
        }
    }
}

@Composable
fun AdminItemCard(
    admin: AdminData,
    textMain: Color,
    textSecondary: Color,
    successGreen: Color,
    errorRed: Color,
    primaryCyan: Color,
    cardBg: Color
) {
    var showSetorDialog by remember { mutableStateOf(false) }
    var nominalSetoran by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(admin.name, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { showSetorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
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
                    Text("Jml Plggn", color = textSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(admin.jmlPlggn, color = primaryCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(16.dp))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            delay(1500)
                            isUpdating = false
                            updateResult = "Setoran sebesar Rp. $nominalSetoran berhasil ditambahkan!"
                            nominalSetoran = ""
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
