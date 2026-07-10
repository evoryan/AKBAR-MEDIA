package com.example.ui.screens
import com.example.ui.screens.Customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(onBack: () -> Unit) {
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val primaryPurple = Color(0xFF2B0B3F) // Dark Purple
    val neonCyan = Color(0xFF00FFFF)
    val neonPink = Color(0xFFFF00FF)
    val errorRed = Color(0xFFFF003C)
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isProrata by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    
    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    
    val isPhoneValid = phone.isNotBlank() && phone.all { it.isDigit() } && phone.length >= 10
    val isFormValid = name.isNotBlank() && isPhoneValid

    val localFocusManager = LocalFocusManager.current
    
    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Tambah User", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                    .background(bgMain)
            ) {
                val tabs = listOf(
                    "Identitas" to Icons.Default.Person,
                    "Modem" to Icons.Default.Router,
                    "Koordinat dan ODP" to Icons.Default.Storefront
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = headerBg,
                    contentColor = textMain,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = neonCyan
                        )
                    },
                    divider = { }
                ) {
                    tabs.forEachIndexed { index, (title, icon) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (selectedTabIndex == index) neonCyan else textSecondary) },
                            icon = { Icon(icon, contentDescription = title, tint = if (selectedTabIndex == index) neonCyan else textSecondary) }
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    Text(
                        text = "Lengkapi data identitas dan tarif di bawah ini :",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Form fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            isNameError = it.isBlank()
                        },
                        label = { Text("Nama", color = textSecondary) },
                        placeholder = { Text("Nama...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isNameError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            errorBorderColor = errorRed,
                            errorTextColor = textMain
                        ),
                        singleLine = true
                    )
                    if (isNameError) {
                        Text("Nama wajib diisi", color = errorRed, fontSize = 10.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { 
                            phone = it
                            isPhoneError = !it.all { char -> char.isDigit() } || it.length < 10
                        },
                        label = { Text("Nomor Telepon / WA", color = textSecondary) },
                        placeholder = { Text("Telepon / WA...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isPhoneError,
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(neonCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Contacts, contentDescription = "Contacts", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            errorBorderColor = errorRed,
                            errorTextColor = textMain
                        ),
                        singleLine = true
                    )
                    if (isPhoneError) {
                        Text("Format telepon tidak valid (minimal 10 angka)", color = errorRed, fontSize = 10.sp, modifier = Modifier.padding(start = 16.dp))
                    }
                    
                    ClickableField(title = "Tanggal Register", subtitle = "(opsional, jika kosong otomatis tgl hari ini)", actionText = "Pilih Tanggal Register", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    ClickableField(title = "Tanggal Tagihan", actionText = "Pilih Tanggal", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    ClickableField(title = "Tanggal Isolir (Optional)", actionText = "Pilih Tanggal", neonCyan = neonCyan, textSecondary = textSecondary, titleColor = errorRed)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    ClickableField(title = "Paket", actionText = "Pilih Paket", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    ClickableField(title = "Area", actionText = "Pilih Area", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    ClickableField(title = "Biaya Tambahan 1", actionText = "Tambahkan Biaya Tambahan 1", neonCyan = neonCyan, textSecondary = textSecondary)
                    
                    ClickableField(title = "Biaya Tambahan 2", actionText = "Tambahkan Biaya Tambahan 2", neonCyan = neonCyan, textSecondary = textSecondary)
                    
                    ClickableField(title = "Diskon", actionText = "Tambahkan Diskon", neonCyan = neonCyan, textSecondary = textSecondary)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aktifkan Prorata", color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = isProrata,
                            onCheckedChange = { isProrata = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = neonCyan,
                                checkedTrackColor = neonCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            isNameError = name.isBlank()
                            isPhoneError = !isPhoneValid
                            if (isFormValid) {
                                selectedTabIndex = 1 
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) primaryPurple else primaryPurple.copy(alpha = 0.5f), 
                            contentColor = textMain
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LANJUT", fontWeight = FontWeight.Bold)
                    }
                } else if (selectedTabIndex == 1) {
                    Text(
                        text = "Tentang Mikrotik Pelanggan",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClickableField(title = "Mikrotik (Opsional)", actionText = "Pilih Mikrotik", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { selectedTabIndex = 2 },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LANJUT", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, textSecondary, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text("Catatan :\nAnda bisa melewati proses ini jika tidak dibutuhkan.", color = textSecondary, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = "Tentang ODP (CUSTOM Only)",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat (Opsional)", color = textSecondary) },
                        placeholder = { Text("Alamat...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClickableField(title = "ODP Pelanggan (Opsional)", actionText = "Pilih ODP", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ClickableField(title = "Koordinat Pelanggan (Opsional)", actionText = "Pilih Koordinat Pelanggan", neonCyan = neonCyan, textSecondary = textSecondary)
                    HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val newCust = Customer(
                                        id = "",
                                        name = name,
                                        phone = phone,
                                        area = "Semua", // fallback, but can be updated if there is area selector
                                        username = name.lowercase().replace(" ", ""),
                                        billingDate = "10",
                                        status = "BELUM BAYAR",
                                        price = "Rp. 150.000",
                                        discount = "- Dskn : Rp. 0"
                                    )
                                    com.example.ui.data.remote.ApiClient.apiService.addCustomer(newCust)
                                    onBack()
                                } catch (e: Exception) {
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SIMPAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClickableField(title: String, subtitle: String? = null, actionText: String, neonCyan: Color, textSecondary: Color, titleColor: Color = textSecondary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /*TODO*/ }
            .padding(vertical = 4.dp)
    ) {
        Text(title, color = titleColor, fontSize = 12.sp)
        if (subtitle != null) {
            Text(subtitle, color = textSecondary, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(actionText, color = neonCyan, fontSize = 14.sp)
    }
}
