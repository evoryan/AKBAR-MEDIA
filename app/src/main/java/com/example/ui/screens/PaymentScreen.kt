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
import androidx.compose.ui.graphics.luminance
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
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val successGreen = Color(0xFF00FF00)

    val context = androidx.compose.ui.platform.LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val currentMonth = remember {
        val cal = java.util.Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
        sdf.format(cal.time)
    }
    
    fun getMonthNameWithOffset(offset: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, offset)
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
        return sdf.format(cal.time)
    }

    val monthsToPay = remember { mutableStateListOf(currentMonth) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    var customDiscount by remember { mutableStateOf(0) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var discountInputText by remember { mutableStateOf("") }
    
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("1 Bulan ($currentMonth)") }
    
    val options = remember {
        listOf(
            "1 Bulan ($currentMonth)" to listOf(getMonthNameWithOffset(0)),
            "2 Bulan (${getMonthNameWithOffset(0).substringBefore(" ")} - ${getMonthNameWithOffset(1)})" to (0..1).map { getMonthNameWithOffset(it) },
            "3 Bulan (${getMonthNameWithOffset(0).substringBefore(" ")} - ${getMonthNameWithOffset(2)})" to (0..2).map { getMonthNameWithOffset(it) },
            "6 Bulan (${getMonthNameWithOffset(0).substringBefore(" ")} - ${getMonthNameWithOffset(5)})" to (0..5).map { getMonthNameWithOffset(it) },
            "12 Bulan / 1 Tahun (${getMonthNameWithOffset(0).substringBefore(" ")} - ${getMonthNameWithOffset(11)})" to (0..11).map { getMonthNameWithOffset(it) }
        )
    }
    
    val monthlyFee = customer?.price?.replace(Regex("\\.0$"), "")?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
    val totalAmount = monthsToPay.size * monthlyFee
    val finalAmount = (totalAmount - customDiscount).coerceAtLeast(0)
    
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
    
    androidx.compose.runtime.LaunchedEffect(customer) {
        if (customer != null) {
            val parsed = customer?.discount?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
            customDiscount = parsed
        }
    }
    
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val coroutineScope = rememberCoroutineScope()
    val currentUser by UserSession.currentUser.collectAsState()
    val totalFormatted = "Rp. ${formatter.format(totalAmount)}"
    val finalFormatted = "Rp. ${formatter.format(finalAmount)}"

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
                if (customDiscount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon", color = textMain, fontSize = 14.sp)
                        Text("- Rp. ${formatter.format(customDiscount)}", color = Color.Green, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Perbulannya", color = textSecondary, fontSize = 14.sp)
                    val basePrice = customer?.price ?: "Rp. 0"
                    Text(basePrice, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Bulan yang Akan Dibayar", color = textSecondary, fontSize = 12.sp)
                
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
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
                            unfocusedTrailingIconColor = textMain.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        containerColor = cardBg
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.first, color = textMain) },
                                onClick = {
                                    selectedOptionText = option.first
                                    monthsToPay.clear()
                                    monthsToPay.addAll(option.second)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                Text("Rincian Bulan Tagihan:", color = textSecondary, fontSize = 12.sp)
                monthsToPay.forEach { month ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• $month", color = textMain, fontSize = 14.sp)
                    }
                }
            }

            // Total & Hasil Akhir Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Tagihan", color = textSecondary, fontSize = 14.sp)
                    Text(totalFormatted, color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                if (customDiscount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Potongan Diskon", color = successGreen, fontSize = 14.sp)
                        Text("- Rp. ${formatter.format(customDiscount)}", color = successGreen, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(color = cardBorder.copy(alpha = 0.1f), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hasil Akhir", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(finalFormatted, color = neonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Discount Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, successGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { 
                        discountInputText = if (customDiscount > 0) customDiscount.toString() else ""
                        showDiscountDialog = true
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (customDiscount > 0) "Ubah Diskon (Rp. ${formatter.format(customDiscount)})" else "Tambahkan Diskon",
                    color = successGreen,
                    fontSize = 14.sp
                )
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
                text = { Text("Apakah Anda yakin ingin menyelesaikan pembayaran ini sejumlah $finalFormatted?", color = textSecondary) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showConfirmDialog = false 
                            coroutineScope.launch {
                                try {
                                    val req = com.example.ui.data.remote.PaymentRequest(
                                        customerId = customerId,
                                        adminName = currentUser?.name ?: "Admin",
                                        totalAmount = finalAmount.toDouble()
                                    )
                                    ApiClient.apiService.payBilling(req)
                                    onNavigateToSuccess(customerId, finalAmount.toString(), monthsToPay.joinToString(", "))
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

        if (showDiscountDialog) {
            AlertDialog(
                onDismissRequest = { showDiscountDialog = false },
                containerColor = bgMain,
                title = { Text("Tambahkan Diskon", color = textMain, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Masukkan nominal diskon yang ingin diberikan:", color = textSecondary, fontSize = 14.sp)
                        OutlinedTextField(
                            value = discountInputText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    discountInputText = input
                                }
                            },
                            label = { Text("Nominal Diskon (Rp)", color = textMain.copy(alpha = 0.7f)) },
                            placeholder = { Text("Contoh: 10000", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = successGreen,
                                unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain,
                                focusedLabelColor = successGreen,
                                unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = discountInputText.toIntOrNull() ?: 0
                            if (amt > totalAmount) {
                                Toast.makeText(context, "Diskon tidak boleh melebihi total tagihan!", Toast.LENGTH_SHORT).show()
                            } else {
                                customDiscount = amt
                                showDiscountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = successGreen, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscountDialog = false }) {
                        Text("Batal", color = textMain)
                    }
                }
            )
        }
    }
}

}
