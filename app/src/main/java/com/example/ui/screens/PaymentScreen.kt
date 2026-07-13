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

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(customerId: String, onBack: () -> Unit, onNavigateToDetail: () -> Unit, onNavigateToSuccess: (String, String, String) -> Unit) {
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val neonCyan = Color(0xFF00FFFF)
    val successGreen = Color(0xFF00FF00)

    val context = androidx.compose.ui.platform.LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val monthsToPay = remember { mutableStateListOf("Tagihan Bulan Ini") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val monthlyFee = customer?.price?.replace(Regex("\\.0$"), "")?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
    val totalAmount = monthsToPay.size * monthlyFee
    
    androidx.compose.runtime.LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
            if (customer == null) {
                android.widget.Toast.makeText(context, "Pelanggan tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            android.widget.Toast.makeText(context, "Gagal memuat pelanggan", android.widget.Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val coroutineScope = rememberCoroutineScope()
    val currentUser by UserSession.currentUser.collectAsState()
    val totalFormatted = "Rp. ${formatter.format(totalAmount)}"

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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = neonCyan)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(bgMain)
                    .verticalScroll(rememberScrollState())
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
                if (!customer?.discount.isNullOrEmpty() && customer?.discount != "0") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon", color = textMain, fontSize = 14.sp)
                        Text("- ${customer?.discount}", color = Color.Green, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Perbulannya", color = textSecondary, fontSize = 14.sp)
                    Text(customer?.price ?: "Rp. 0", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Bulan", color = textSecondary, fontSize = 12.sp)
                monthsToPay.forEach { month ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(month, color = textMain, fontSize = 14.sp)
                        if (monthsToPay.size > 1) {
                            IconButton(
                                onClick = { monthsToPay.remove(month) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Total Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Pembayaran", color = textMain, fontSize = 16.sp)
                Text(totalFormatted, color = neonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Discount Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, successGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { /*TODO*/ }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tambahkan Diskon", color = successGreen, fontSize = 14.sp)
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
                    onClick = { /*TODO*/ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("KIRIM WA TAGIHAN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            val isPaid = customer?.status?.contains("LUNAS", ignoreCase = true) ?: false
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isPaid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF), 
                    contentColor = Color.Black,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isPaid) "SUDAH LUNAS" else "BAYAR SEKARANG", fontWeight = FontWeight.Bold)
            }
        }
        
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                containerColor = bgMain,
                title = { Text("Konfirmasi Pembayaran", color = textMain, fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin menyelesaikan pembayaran ini sejumlah $totalFormatted?", color = textSecondary) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showConfirmDialog = false 
                            coroutineScope.launch {
                                try {
                                    val req = com.example.ui.data.remote.PaymentRequest(
                                        customerId = customerId,
                                        adminName = currentUser?.name ?: "Admin",
                                        totalAmount = totalAmount.toDouble()
                                    )
                                    ApiClient.apiService.payBilling(req)
                                    onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
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
    }
}

}
