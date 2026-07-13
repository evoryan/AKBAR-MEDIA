import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

# I will write a custom AddCustomerScreen to implement the logic
new_code = """package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onBack: () -> Unit
) {
    val bgDark = Color(0xFF000000)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val primaryPurple = Color(0xFF9D00FF)
    val errorRed = Color(0xFFFF5555)
    val neonCyan = Color(0xFF00FFFF)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    var registerDate by remember { mutableStateOf("") }
    var billingDate by remember { mutableStateOf("") }
    var isolateDate by remember { mutableStateOf("") }
    
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var selectedPackage by remember { mutableStateOf<InternetPackage?>(null) }
    
    var additionalCost1 by remember { mutableStateOf("") }
    var additionalCost2 by remember { mutableStateOf("") }
    var isProrata by remember { mutableStateOf(false) }

    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var packages by remember { mutableStateOf<List<InternetPackage>>(emptyList()) }

    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    
    var selectedTabIndex by remember { mutableStateOf(0) }

    var showAreaDialog by remember { mutableStateOf(false) }
    var showPackageDialog by remember { mutableStateOf(false) }
    var showCost1Dialog by remember { mutableStateOf(false) }
    var showCost2Dialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            areas = ApiClient.apiService.getAreas()
            packages = ApiClient.apiService.getPackages()
        } catch (e: Exception) {
            // handle error
        }
    }

    val isPhoneValid = phone.length >= 10 && phone.all { it.isDigit() }
    val isFormValid = name.isNotBlank() && isPhoneValid

    // Helper to format date picker selection
    fun getDatePickerLabel(millis: Long?): String {
        if (millis == null) return ""
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return formatter.format(calendar.time)
    }
    
    // Custom date picker function using standard Compose DatePickerDialog is not available directly, so we'll use Material3 DatePickerDialog if available, or just use a custom simple dialog or Android native DatePickerDialog. Wait, Compose Material3 has DatePickerDialog!
    
    var showRegisterDatePicker by remember { mutableStateOf(false) }
    var showBillingDatePicker by remember { mutableStateOf(false) }
    var showIsolateDatePicker by remember { mutableStateOf(false) }
    
    val registerDatePickerState = rememberDatePickerState()
    val billingDatePickerState = rememberDatePickerState()
    val isolateDatePickerState = rememberDatePickerState()

    if (showRegisterDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showRegisterDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    registerDate = getDatePickerLabel(registerDatePickerState.selectedDateMillis)
                    showRegisterDatePicker = false
                }) { Text("OK", color = neonCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDatePicker = false }) { Text("Batal", color = textSecondary) }
            }
        ) {
            DatePicker(state = registerDatePickerState)
        }
    }

    if (showBillingDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showBillingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Extract just the day part if needed, or keep full date. Instructions: "Pilih Tanggal Tagihan" 
                    billingDate = getDatePickerLabel(billingDatePickerState.selectedDateMillis)
                    showBillingDatePicker = false
                }) { Text("OK", color = neonCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showBillingDatePicker = false }) { Text("Batal", color = textSecondary) }
            }
        ) {
            DatePicker(state = billingDatePickerState)
        }
    }

    if (showIsolateDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showIsolateDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    isolateDate = getDatePickerLabel(isolateDatePickerState.selectedDateMillis)
                    showIsolateDatePicker = false
                }) { Text("OK", color = neonCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showIsolateDatePicker = false }) { Text("Batal", color = textSecondary) }
            }
        ) {
            DatePicker(state = isolateDatePickerState)
        }
    }

    if (showAreaDialog) {
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            title = { Text("Pilih Area") },
            text = {
                LazyColumn {
                    items(areas.size) { index ->
                        val area = areas[index]
                        Text(
                            text = area.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedArea = area
                                    showAreaDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAreaDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showPackageDialog) {
        AlertDialog(
            onDismissRequest = { showPackageDialog = false },
            title = { Text("Pilih Paket") },
            text = {
                LazyColumn {
                    items(packages.size) { index ->
                        val pkg = packages[index]
                        Text(
                            text = "${pkg.name} - Rp ${pkg.price}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPackage = pkg
                                    showPackageDialog = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPackageDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showCost1Dialog) {
        var tempCost by remember { mutableStateOf(additionalCost1) }
        AlertDialog(
            onDismissRequest = { showCost1Dialog = false },
            title = { Text("Biaya Tambahan 1") },
            text = {
                OutlinedTextField(
                    value = tempCost,
                    onValueChange = { tempCost = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    additionalCost1 = tempCost
                    showCost1Dialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showCost1Dialog = false }) { Text("Batal") }
            }
        )
    }

    if (showCost2Dialog) {
        var tempCost by remember { mutableStateOf(additionalCost2) }
        AlertDialog(
            onDismissRequest = { showCost2Dialog = false },
            title = { Text("Biaya Tambahan 2") },
            text = {
                OutlinedTextField(
                    value = tempCost,
                    onValueChange = { tempCost = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    additionalCost2 = tempCost
                    showCost2Dialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showCost2Dialog = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgDark)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                }
                Text(
                    "Tambah Pelanggan",
                    color = textMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        containerColor = bgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgDark)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Akun", "Mikrotik", "ODP").forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Text(
                        title,
                        color = if (isSelected) neonCyan else textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { selectedTabIndex = index }
                            .padding(vertical = 8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    if (selectedTabIndex == 0) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                isNameError = it.isBlank()
                            },
                            label = { Text("Nama", color = if (isNameError) errorRed else textSecondary) },
                            placeholder = { Text("Nama...", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = isNameError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            singleLine = true
                        )
                        if (isNameError) {
                            Text("Nama tidak boleh kosong", color = errorRed, fontSize = 10.sp, modifier = Modifier.padding(start = 16.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { 
                                phone = it
                                isPhoneError = it.length < 10 || !it.all { char -> char.isDigit() }
                            },
                            label = { Text("Phone", color = if (isPhoneError) errorRed else textSecondary) },
                            placeholder = { Text("Phone...", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = isPhoneError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            singleLine = true
                        )
                        if (isPhoneError) {
                            Text("Format telepon tidak valid", color = errorRed, fontSize = 10.sp, modifier = Modifier.padding(start = 16.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ClickableField(
                            title = "Tanggal Register", 
                            subtitle = if (registerDate.isNotEmpty()) registerDate else "(opsional, jika kosong otomatis tgl hari ini)", 
                            actionText = "Pilih Tanggal Register", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showRegisterDatePicker = true }
                        )
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(
                            title = "Tanggal Tagihan", 
                            subtitle = if (billingDate.isNotEmpty()) billingDate else null,
                            actionText = "Pilih Tanggal Tagihan", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showBillingDatePicker = true }
                        )
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(
                            title = "Tanggal Isolir (Optional)", 
                            subtitle = if (isolateDate.isNotEmpty()) isolateDate else null,
                            actionText = "Pilih Tanggal Isolir", 
                            neonCyan = neonCyan, textSecondary = textSecondary, titleColor = errorRed,
                            onClick = { showIsolateDatePicker = true }
                        )
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(
                            title = "Area", 
                            subtitle = selectedArea?.name,
                            actionText = "Pilih Area", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showAreaDialog = true }
                        )
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(
                            title = "Paket", 
                            subtitle = selectedPackage?.name?.let { "$it (Rp ${selectedPackage?.price})" },
                            actionText = "Pilih Paket", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showPackageDialog = true }
                        )
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(
                            title = "Biaya Tambahan 1", 
                            subtitle = if (additionalCost1.isNotEmpty()) "Rp $additionalCost1" else null,
                            actionText = "Tambahkan Biaya Tambahan 1", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showCost1Dialog = true }
                        )
                        ClickableField(
                            title = "Biaya Tambahan 2", 
                            subtitle = if (additionalCost2.isNotEmpty()) "Rp $additionalCost2" else null,
                            actionText = "Tambahkan Biaya Tambahan 2", 
                            neonCyan = neonCyan, textSecondary = textSecondary,
                            onClick = { showCost2Dialog = true }
                        )

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
                        Button(
                            onClick = { selectedTabIndex = 2 },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("LANJUT", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val newCust = Customer(
                                            id = "",
                                            name = name,
                                            phone = phone,
                                            area = selectedArea?.name ?: "Semua",
                                            username = name.lowercase().replace(" ", ""),
                                            billingDate = billingDate.ifEmpty { "1" },
                                            registerDate = registerDate,
                                            isolateDate = isolateDate,
                                            packageName = selectedPackage?.name ?: "",
                                            status = "BELUM BAYAR",
                                            price = selectedPackage?.price?.let { "Rp. $it" } ?: "Rp. 0",
                                            discount = "- Dskn : Rp. 0",
                                            additionalCost1 = additionalCost1,
                                            additionalCost2 = additionalCost2
                                        )
                                        ApiClient.apiService.addCustomer(newCust)
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
}

@Composable
fun ClickableField(title: String, subtitle: String? = null, actionText: String, neonCyan: Color, textSecondary: Color, titleColor: Color = textSecondary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
"""

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(new_code)
