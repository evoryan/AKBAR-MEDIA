package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.OdpItem
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onBack: () -> Unit
) {
    val bgDark = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val primaryPurple = Color(0xFF9D00FF)
    val errorRed = Color(0xFFFF5555)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val successGreen = Color(0xFF00FF00)
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    var registerDate by remember { mutableStateOf("") }
    var billingDate by remember { mutableStateOf("") }
    var isolateDate by remember { mutableStateOf("") }
    
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var selectedPackage by remember { mutableStateOf<InternetPackage?>(null) }
    var selectedSecret by remember { mutableStateOf<PPPoESecret?>(null) }
    var selectedOdp by remember { mutableStateOf<OdpItem?>(null) }
    var selectedPort by remember { mutableStateOf("") }
    
    var additionalCost1 by remember { mutableStateOf("") }
    var additionalCost2 by remember { mutableStateOf("") }

    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var packages by remember { mutableStateOf<List<InternetPackage>>(emptyList()) }
    var secrets by remember { mutableStateOf<List<PPPoESecret>>(emptyList()) }
    var odps by remember { mutableStateOf<List<OdpItem>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var isLoadingSecrets by remember { mutableStateOf(false) }

    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { uri ->
            if (uri != null) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    if (idIndex != -1 && hasPhoneIndex != -1) {
                        val id = cursor.getString(idIndex)
                        val hasPhone = cursor.getString(hasPhoneIndex)
                        if (hasPhone.toInt() > 0) {
                            val phones = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id),
                                null
                            )
                            if (phones != null && phones.moveToFirst()) {
                                val numIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIndex != -1) {
                                    var num = phones.getString(numIndex)
                                    num = num.replace(Regex("[^0-9]"), "")
                                    if (num.startsWith("62")) {
                                        num = "0" + num.substring(2)
                                    }
                                    phone = num
                                    isPhoneError = false
                                }
                                phones.close()
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin membaca kontak ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    
    var selectedTabIndex by remember { mutableStateOf(0) }

    var showAreaDialog by remember { mutableStateOf(false) }
    var showPackageDialog by remember { mutableStateOf(false) }
    var showSecretDialog by remember { mutableStateOf(false) }
    var secretSearchQuery by remember { mutableStateOf("") }
    var showAddSecretDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf<List<com.example.ui.data.remote.MikrotikProfile>>(emptyList()) }
    var newSecretUsername by remember { mutableStateOf("") }
    var newSecretPassword by remember { mutableStateOf("") }
    var newSecretProfile by remember { mutableStateOf("") }
    var isAddingSecret by remember { mutableStateOf(false) }
    var showOdpDialog by remember { mutableStateOf(false) }
    var showPortDialog by remember { mutableStateOf(false) }
    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    var rasioList by remember { mutableStateOf<List<com.example.ui.data.RasioItem>>(emptyList()) }
    
    var showRegisterDatePicker by remember { mutableStateOf(false) }
    var showBillingDatePicker by remember { mutableStateOf(false) }
    
    val registerDatePickerState = rememberDatePickerState()
    val billingDatePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        try {
            areas = ApiClient.apiService.getAreas()
            packages = ApiClient.apiService.getPackages()
            odps = ApiClient.apiService.getOdpList()
            customers = ApiClient.apiService.getCustomers()
            odcList = ApiClient.apiService.getOdcList()
            rasioList = ApiClient.apiService.getRasioList()
        } catch (e: Exception) {
            // handle error
        }
    }

    LaunchedEffect(selectedArea) {
        if (selectedArea != null) {
            selectedSecret = null
            isLoadingSecrets = true
            
            // Fetch Secrets
            try {
                secrets = ApiClient.apiService.getMikrotikSecrets(selectedArea!!.id)
            } catch (e: Exception) {
                secrets = emptyList()
            }
            
            // Fetch Profiles
            try {
                profiles = ApiClient.apiService.getMikrotikProfiles(selectedArea!!.id)
                if (profiles.isEmpty()) {
                    profiles = listOf(
                        com.example.ui.data.remote.MikrotikProfile("1", "default"),
                        com.example.ui.data.remote.MikrotikProfile("2", "1M"),
                        com.example.ui.data.remote.MikrotikProfile("3", "2M"),
                        com.example.ui.data.remote.MikrotikProfile("4", "3M"),
                        com.example.ui.data.remote.MikrotikProfile("5", "4M"),
                        com.example.ui.data.remote.MikrotikProfile("6", "5M"),
                        com.example.ui.data.remote.MikrotikProfile("7", "10M"),
                        com.example.ui.data.remote.MikrotikProfile("8", "20M")
                    )
                }
            } catch (e: Exception) {
                profiles = listOf(
                    com.example.ui.data.remote.MikrotikProfile("1", "default"),
                    com.example.ui.data.remote.MikrotikProfile("2", "1M"),
                    com.example.ui.data.remote.MikrotikProfile("3", "2M"),
                    com.example.ui.data.remote.MikrotikProfile("4", "3M"),
                    com.example.ui.data.remote.MikrotikProfile("5", "4M"),
                    com.example.ui.data.remote.MikrotikProfile("6", "5M"),
                    com.example.ui.data.remote.MikrotikProfile("7", "10M"),
                    com.example.ui.data.remote.MikrotikProfile("8", "20M")
                )
            }
            
            isLoadingSecrets = false
        }
    }

    val isPhoneValid = phone.length >= 10 && (if (phone.startsWith("+")) phone.substring(1).all { it.isDigit() } else phone.all { it.isDigit() })
    val isFormValid = name.isNotBlank() && isPhoneValid

    fun getDatePickerLabel(millis: Long?): String {
        if (millis == null) return ""
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return formatter.format(calendar.time)
    }

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
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedArea = area
                                showAreaDialog = false
                            }.padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAreaDialog = false }) { Text("Tutup") } }
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
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedPackage = pkg
                                showPackageDialog = false
                            }.padding(16.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPackageDialog = false }) { Text("Tutup") } }
        )
    }

    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            title = { Text("Pilih PPPoE Secret") },
            text = {
                if (isLoadingSecrets) {
                    CircularProgressIndicator(color = neonCyan)
                } else if (secrets.isEmpty()) {
                    Text("Tidak ada secret di area ini atau gagal memuat.")
                } else {
                    Column {
                        OutlinedTextField(
                            value = secretSearchQuery,
                            onValueChange = { secretSearchQuery = it },
                            label = { Text("Cari Secret") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true
                        )
                        val filteredSecrets = secrets.filter { it.name.contains(secretSearchQuery, ignoreCase = true) }
                        if (filteredSecrets.isEmpty()) {
                            Text("Tidak ada secret yang cocok.")
                        } else {
                            LazyColumn {
                                items(filteredSecrets.size) { index ->
                                    val secret = filteredSecrets[index]
                                    Text(
                                        text = secret.name,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            selectedSecret = secret
                                            showSecretDialog = false
                                            secretSearchQuery = ""
                                        }.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSecretDialog = false; secretSearchQuery = "" }) { Text("Tutup") } }
        )
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Pilih Profile", color = neonCyan) },
            text = {
                if (profiles.isEmpty()) {
                    Text("Tidak ada profile di area ini atau gagal memuat.")
                } else {
                    LazyColumn {
                        items(profiles.size) { index ->
                            val profile = profiles[index]
                            Text(
                                text = profile.name,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    newSecretProfile = profile.name
                                    showProfileDialog = false
                                }.padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Tutup") } },
            containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            titleContentColor = neonCyan,
            textContentColor = textMain
        )
    }

    if (showAddSecretDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAddingSecret) showAddSecretDialog = false },
            title = { Text("Tambah Secret Baru", color = neonCyan) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSecretUsername,
                        onValueChange = { newSecretUsername = it },
                        label = { Text("Username Secret", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSecretPassword,
                        onValueChange = { newSecretPassword = it },
                        label = { Text("Password", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    Box {
                        OutlinedTextField(
                            value = newSecretProfile.ifBlank { "default" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Profile", color = textSecondary) },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showProfileDialog = true })
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSecretUsername.isNotBlank() && newSecretPassword.isNotBlank() && selectedArea != null) {
                            isAddingSecret = true
                            coroutineScope.launch {
                                try {
                                    ApiClient.apiService.addMikrotikSecret(
                                        selectedArea!!.id,
                                        mapOf(
                                            "name" to newSecretUsername,
                                            "password" to newSecretPassword,
                                            "profile" to newSecretProfile
                                        )
                                    )
                                    // Refresh secrets
                                    secrets = ApiClient.apiService.getMikrotikSecrets(selectedArea!!.id)
                                    
                                    // Select the newly added secret
                                    selectedSecret = secrets.find { it.name == newSecretUsername } ?: selectedSecret
                                    
                                    Toast.makeText(context, "Secret berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    showAddSecretDialog = false
                                    newSecretUsername = ""
                                    newSecretPassword = ""
                                    newSecretProfile = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal menambah secret: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isAddingSecret = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isAddingSecret
                ) { 
                    if (isAddingSecret) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = neonCyan, strokeWidth = 2.dp)
                    } else {
                        Text("Simpan", color = neonCyan) 
                    }
                }
            },
            dismissButton = { 
                TextButton(
                    onClick = { showAddSecretDialog = false },
                    enabled = !isAddingSecret
                ) { Text("Batal", color = textSecondary) } 
            },
            containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF),
            titleContentColor = neonCyan,
            textContentColor = textMain
        )
    }

    if (showOdpDialog) {
        var odpSearchQuery by remember { mutableStateOf("") }
        val selectableItems = remember(odps, odcList, rasioList, selectedArea, odpSearchQuery) {
            val list = mutableListOf<AddSelectableConnectionItem>()
            
            // 1. Add ODPs
            odps.forEach { odp ->
                val matchingOdc = odcList.find { it.id == odp.odcId || it.name == odp.portInput || odp.portInput.contains(it.name) }
                val matchingRasio = rasioList.find { it.name == odp.portInput || odp.portInput.contains(it.name) }
                val odpArea = odp.area.ifEmpty { matchingOdc?.area ?: matchingRasio?.area ?: "" }
                val matchesArea = selectedArea == null || odpArea.equals(selectedArea?.name ?: "", ignoreCase = true)
                
                val matchesSearch = odpSearchQuery.isEmpty() ||
                        odp.name.contains(odpSearchQuery, ignoreCase = true) ||
                        (matchingOdc?.name?.contains(odpSearchQuery, ignoreCase = true) == true) ||
                        (matchingRasio?.name?.contains(odpSearchQuery, ignoreCase = true) == true)
                        
                if (matchesArea && matchesSearch) {
                    list.add(
                        AddSelectableConnectionItem(
                            id = "odp_${odp.id}",
                            name = odp.name,
                            type = "ODP",
                            originalId = odp.id,
                            portCount = odp.portCount,
                            area = odpArea,
                            details = "ODC: ${matchingOdc?.name ?: odp.portInput.takeIf { it.isNotEmpty() } ?: "-"} | Rasio: ${matchingRasio?.name ?: matchingRasio?.size ?: "-"}"
                        )
                    )
                }
            }
            
            // 2. Add ODCs
            odcList.forEach { odc ->
                val matchesArea = selectedArea == null || odc.area.equals(selectedArea?.name ?: "", ignoreCase = true)
                val matchesSearch = odpSearchQuery.isEmpty() || odc.name.contains(odpSearchQuery, ignoreCase = true)
                
                if (matchesArea && matchesSearch) {
                    list.add(
                        AddSelectableConnectionItem(
                            id = "odc_${odc.id}",
                            name = odc.name,
                            type = "ODC",
                            originalId = odc.id,
                            portCount = odc.portCount,
                            area = odc.area,
                            details = "Lokasi: ${odc.location} | Port: ${odc.portCount}"
                        )
                    )
                }
            }
            
            // 3. Add Rasios
            rasioList.forEach { rasio ->
                val matchesArea = selectedArea == null || rasio.area.equals(selectedArea?.name ?: "", ignoreCase = true)
                val matchesSearch = odpSearchQuery.isEmpty() || rasio.name.contains(odpSearchQuery, ignoreCase = true)
                
                if (matchesArea && matchesSearch) {
                    val pCount = rasio.size.split(":").lastOrNull()?.toIntOrNull() ?: 8
                    list.add(
                        AddSelectableConnectionItem(
                            id = "rasio_${rasio.id}",
                            name = rasio.name,
                            type = "Rasio",
                            originalId = rasio.id,
                            portCount = pCount,
                            area = rasio.area,
                            details = "Lokasi: ${rasio.location} | Ukuran: ${rasio.size}"
                        )
                    )
                }
            }
            
            list
        }

        AlertDialog(
            onDismissRequest = { showOdpDialog = false },
            title = { Text("Pilih Koneksi (${selectedArea?.name ?: "Semua Area"})") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = odpSearchQuery,
                        onValueChange = { odpSearchQuery = it },
                        placeholder = { Text("Cari ODP/ODC/Rasio...", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain,
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSecondary) }
                    )
                    
                    if (selectableItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada ODP/ODC/Rasio ditemukan di area ini", color = textSecondary)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(selectableItems.size) { index ->
                                val item = selectableItems[index]
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                try {
                                                    if (item.type == "ODP") {
                                                        selectedOdp = odps.find { it.id == item.originalId }
                                                    } else {
                                                        val existingOdp = odps.find { it.name.equals(item.name, ignoreCase = true) }
                                                        if (existingOdp != null) {
                                                            selectedOdp = existingOdp
                                                        } else {
                                                            val newOdp = com.example.ui.data.OdpItem(
                                                                id = "",
                                                                name = item.name,
                                                                odcId = if (item.type == "ODC") item.originalId else "0",
                                                                portCount = item.portCount,
                                                                portInput = if (item.type == "Rasio") item.name else "",
                                                                area = item.area
                                                            )
                                                            ApiClient.apiService.addOdp(newOdp)
                                                            odps = ApiClient.apiService.getOdpList()
                                                            selectedOdp = odps.find { it.name.equals(item.name, ignoreCase = true) }
                                                        }
                                                    }
                                                    selectedPort = ""
                                                    showOdpDialog = false
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Gagal memilih koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1E1E2A) else androidx.compose.ui.graphics.Color(0xFFF0F2F5))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                item.type,
                                                color = if (item.type == "ODP") neonCyan else if (item.type == "ODC") Color(0xFFFF00FF) else successGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(item.details, color = textSecondary, fontSize = 12.sp)
                                        Text("Area: ${item.area}", color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showOdpDialog = false }) { Text("Tutup", color = neonCyan) } },
            containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
        )
    }

    if (showPortDialog && selectedOdp != null) {
        val odp = selectedOdp!!
        AlertDialog(
            onDismissRequest = { showPortDialog = false },
            title = { Text("Pilih Port ODP") },
            text = {
                LazyColumn {
                    items(odp.portCount) { index ->
                        val portNum = (index + 1).toString()
                        val customerOnPort = customers.find { it.odpId == odp.id && it.odpPort == portNum }
                        val isUsed = customerOnPort != null
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = !isUsed) {
                                selectedPort = portNum
                                showPortDialog = false
                            }.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Port $portNum", color = if (isUsed) textSecondary else textMain)
                            if (isUsed) {
                                Text("Terpakai: ${customerOnPort?.name}", color = errorRed, fontSize = 12.sp)
                            } else {
                                Text("Kosong", color = successGreen, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPortDialog = false }) { Text("Tutup") } }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(bgDark).padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                }
                Text("Tambah Pelanggan", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
            }
        },
        containerColor = bgDark
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding().background(bgDark)) {
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Akun", "Mikrotik", "ODP").forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Text(
                        title,
                        color = if (isSelected) neonCyan else textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { selectedTabIndex = index }.padding(vertical = 8.dp)
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp)) {
                item {
                    if (selectedTabIndex == 0) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; isNameError = it.isBlank() },
                            label = { Text("Nama", color = if (isNameError) errorRed else textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            isError = isNameError,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; isPhoneError = it.length < 10 || !(if (it.startsWith("+")) it.substring(1).all { char -> char.isDigit() } else it.all { char -> char.isDigit() }) },
                            label = { Text("Phone", color = if (isPhoneError) errorRed else textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = isPhoneError,
                            trailingIcon = {
                                IconButton(onClick = { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Contacts, contentDescription = "Pilih Kontak", tint = neonCyan)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Alamat (Opsional)", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tanggal Register
                        OutlinedTextField(
                            value = registerDate,
                            onValueChange = { registerDate = it },
                            label = { Text("Tanggal Register", color = textSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showRegisterDatePicker = true }) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = "Pilih Tanggal", tint = neonCyan)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tanggal Tagihan
                        OutlinedTextField(
                            value = billingDate,
                            onValueChange = { billingDate = it },
                            label = { Text("Tanggal Tagihan", color = textSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showBillingDatePicker = true }) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = "Pilih Tanggal", tint = neonCyan)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Tanggal Isolir (Text Input Only as requested)
                        OutlinedTextField(
                            value = isolateDate,
                            onValueChange = { isolateDate = it },
                            label = { Text("Tanggal Isolir (Opsional)", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        ClickableField(title = "Area", subtitle = null, actionText = selectedArea?.name ?: "Pilih Area", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showAreaDialog = true })
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        ClickableField(title = "Paket", subtitle = null, actionText = selectedPackage?.name?.let { "$it (Rp ${selectedPackage?.price})" } ?: "Pilih Paket", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showPackageDialog = true })
                        HorizontalDivider(color = textSecondary.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = additionalCost1,
                            onValueChange = { additionalCost1 = it },
                            label = { Text("Biaya Tambahan 1", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = additionalCost2,
                            onValueChange = { additionalCost2 = it },
                            label = { Text("Biaya Tambahan 2", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                isNameError = name.isBlank()
                                isPhoneError = !isPhoneValid
                                if (isFormValid) selectedTabIndex = 1 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isFormValid) primaryPurple else primaryPurple.copy(alpha = 0.5f), contentColor = textMain),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("LANJUT", fontWeight = FontWeight.Bold)
                        }
                    } else if (selectedTabIndex == 1) {
                        Text("Pilih Secret PPPoE untuk binding pelanggan:", color = textMain, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box {
                            OutlinedTextField(
                                value = selectedSecret?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("PPPoE Secret", color = textSecondary) },
                                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                                modifier = Modifier.fillMaxWidth().clickable { showSecretDialog = true },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary)
                            )
                            // Invisible box over text field to handle click
                            Box(modifier = Modifier.matchParentSize().clickable { 
                                if (selectedArea == null) {
                                    Toast.makeText(context, "Pilih area di Tab Akun terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showSecretDialog = true 
                                }
                            })
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { 
                                if (selectedArea == null) {
                                    Toast.makeText(context, "Pilih area di Tab Akun terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showAddSecretDialog = true 
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Tambah Secret Baru", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { selectedTabIndex = 2 },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple, contentColor = textMain),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("LANJUT", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val newCust = Customer(
                                            id = "", name = name, phone = phone, area = selectedArea?.name ?: "Semua", address = address, username = selectedSecret?.name ?: name.lowercase().replace(" ", ""), billingDate = billingDate.ifEmpty { "1" }, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage?.name ?: "", status = "BELUM BAYAR", price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: "Rp. 0", discount = "- Dskn : Rp. 0", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret?.name ?: ""
                                        )
                                        ApiClient.apiService.addCustomer(newCust)
                                        Toast.makeText(context, "Pelanggan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } catch(e: retrofit2.HttpException) {
                                        val errBody = e.response()?.errorBody()?.string()
                                        android.util.Log.e("AddCust", "HTTP Error: $errBody", e)
                                        android.widget.Toast.makeText(context, "Error: $errBody", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AddCust", "Exception", e)
                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SIMPAN SEKARANG", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Pilih ODP untuk pelanggan ini:", color = textMain, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // ODP Details
                        if (selectedOdp != null) {
                            val totalPorts = selectedOdp!!.portCount
                            val usedPorts = customers.count { it.odpId == selectedOdp!!.id && !it.odpPort.isNullOrBlank() }
                            val emptyPorts = totalPorts - usedPorts
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Informasi ODP: ${selectedOdp!!.name}", color = neonCyan, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Total Port: $totalPorts", color = textMain)
                                    Text("Port Terpakai: $usedPorts", color = errorRed)
                                    Text("Port Kosong: $emptyPorts", color = successGreen)
                                }
                            }
                        }

                        Box {
                            OutlinedTextField(
                                value = selectedOdp?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih ODP", color = textSecondary) },
                                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showOdpDialog = true })
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box {
                            OutlinedTextField(
                                value = if (selectedPort.isNotBlank()) "Port $selectedPort" else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Port", color = textSecondary) },
                                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary)
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { 
                                if (selectedOdp == null) {
                                    Toast.makeText(context, "Pilih ODP terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showPortDialog = true
                                }
                            })
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val newCust = Customer(
                                            id = "", name = name, phone = phone, area = selectedArea?.name ?: "Semua", address = address, username = selectedSecret?.name ?: name.lowercase().replace(" ", ""), billingDate = billingDate.ifEmpty { "1" }, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage?.name ?: "", status = "BELUM BAYAR", price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: "Rp. 0", discount = "- Dskn : Rp. 0", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret?.name ?: "", odpId = selectedOdp?.id ?: "", odpPort = selectedPort
                                        )
                                        ApiClient.apiService.addCustomer(newCust)
                                        Toast.makeText(context, "Pelanggan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } catch(e: retrofit2.HttpException) {
                                        val errBody = e.response()?.errorBody()?.string()
                                        android.util.Log.e("AddCust", "HTTP Error: $errBody", e)
                                        android.widget.Toast.makeText(context, "Error: $errBody", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AddCust", "Exception", e)
                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
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
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp)) {
        Text(title, color = titleColor, fontSize = 12.sp)
        if (subtitle != null) { Text(subtitle, color = textSecondary, fontSize = 10.sp) }
        Spacer(modifier = Modifier.height(4.dp))
        Text(actionText, color = neonCyan, fontSize = 14.sp)
    }
}

private data class AddSelectableConnectionItem(
    val id: String,
    val name: String,
    val type: String,
    val originalId: String,
    val portCount: Int,
    val area: String,
    val details: String
)
