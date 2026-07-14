package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

data class InternetPackage(
    val id: String,
    val name: String,
    val speed: String,
    val price: Double,
    val taxRate: Double = 11.0,
    
    val description: String = ""
) {
    val finalPrice: Double
        get() = price * (1 + taxRate / 100)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val primaryContainer = Color(0xFF11111A)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val primaryPurple = Color(0xFF2B0B3F)
    val errorRed = Color(0xFFFF003C)
    
    var showAddDialog by remember { mutableStateOf(false) }
    var packageToEdit by remember { mutableStateOf<InternetPackage?>(null) }

    var packages = remember { mutableStateListOf<InternetPackage>() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
            packages.clear()
            packages.addAll(res)
        } catch (e: Exception) {
            // handle
        }
    }

    Scaffold(
        containerColor = Color(0xFF00FFFF),
        topBar = {
            TopAppBar(
                title = { Text("Kelola Paket Internet", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00FFFF),
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Package")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgMain)
                .padding(16.dp)
        ) {
            Text("Daftar Paket", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(packages) { pkg ->
                    PackageItem(
                        pkg = pkg, 
                        cardBg = cardBg, 
                        cardBorder = cardBorder, 
                        neonCyan = neonCyan, 
                        errorRed = errorRed,
                        textMain = textMain, 
                        textSecondary = textSecondary,
                        onEdit = { packageToEdit = pkg },
                        onDelete = { 
                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.deletePackage(pkg.id)
                                    val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
                                    packages.clear()
                                    packages.addAll(res)
                                } catch(e: Exception) {}
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB padding
            }
        }
        
        if (showAddDialog || packageToEdit != null) {
            PackageFormDialog(
                initialPackage = packageToEdit,
                onDismiss = { 
                    showAddDialog = false
                    packageToEdit = null
                },
                onSave = { newPkg ->
                    coroutineScope.launch {
                        try {
                            if (packageToEdit != null) {
                                ApiClient.apiService.updatePackage(packageToEdit!!.id, newPkg)
                            } else {
                                ApiClient.apiService.addPackage(newPkg)
                            }
                            packages.clear()
                            packages.addAll(ApiClient.apiService.getPackages())
                        } catch (e: Exception) {}
                    }
                    showAddDialog = false
                    packageToEdit = null
                },
                bgMain = bgMain,
                textMain = textMain,
                textSecondary = textSecondary,
                cardBg = cardBg,
                neonCyan = neonCyan,
                primaryPurple = primaryPurple
            )
        }
    }
}

@Composable
fun PackageItem(
    pkg: InternetPackage,
    cardBg: Color,
    cardBorder: Color,
    neonCyan: Color,
    errorRed: Color,
    textMain: Color,
    textSecondary: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currentUser by UserSession.currentUser.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val isNoTax = pkg.taxRate == 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(pkg.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = neonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(pkg.speed, color = neonCyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = neonCyan)
                    }
                    if (currentUser?.role == UserRole.SUPER_ADMIN) {
                                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                                }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Rp ${formatter.format(pkg.finalPrice)}", color = textMain, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (isNoTax) {
                Text("Termasuk PPN 0%", color = textSecondary, fontSize = 12.sp)
            } else {
                Text("Termasuk PPN ${pkg.taxRate}%", color = textSecondary, fontSize = 12.sp)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Harga (Sebelum PPN)", color = textSecondary, fontSize = 12.sp)
                        Text("Rp ${formatter.format(pkg.price)}", color = textMain, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    

                    
                    if (pkg.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Deskripsi", color = textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pkg.description, color = textMain, fontSize = 14.sp)
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF00FFFF),
            title = { Text("Hapus Paket", color = textMain, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus paket '${pkg.name}'?", color = textSecondary) },
            confirmButton = {
                Button(
                    onClick = { 
                        showDeleteConfirm = false 
                        onDelete() 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.White)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = textMain)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageFormDialog(
    initialPackage: InternetPackage?,
    onDismiss: () -> Unit,
    onSave: (InternetPackage) -> Unit,
    bgMain: Color,
    textMain: Color,
    textSecondary: Color,
    cardBg: Color,
    neonCyan: Color,
    primaryPurple: Color
) {
    var name by remember { mutableStateOf(initialPackage?.name ?: "") }
    var speed by remember { mutableStateOf(initialPackage?.speed ?: "") }
    var priceText by remember { mutableStateOf(initialPackage?.price?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var enableTax by remember { mutableStateOf((initialPackage?.taxRate ?: 0.0) > 0.0) }
    var taxRateText by remember { mutableStateOf(initialPackage?.taxRate?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "11") }
    var description by remember { mutableStateOf(initialPackage?.description ?: "") }
    

    val price = priceText.toDoubleOrNull() ?: 0.0
    val taxRate = if (enableTax) taxRateText.toDoubleOrNull() ?: 0.0 else 0.0
    val finalPrice = price * (1 + taxRate / 100)
    
    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val title = if (initialPackage == null) "Tambah Paket Baru" else "Edit Paket"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain)
                .imePadding(),
            color = bgMain
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryPurple)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textMain)
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Paket", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = speed,
                        onValueChange = { speed = it },
                        label = { Text("Kecepatan", color = textSecondary) },
                        placeholder = { Text("Contoh: 10 Mbps", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Harga (Sebelum PPN)", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { enableTax = !enableTax }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enableTax,
                            onCheckedChange = { enableTax = it },
                            colors = CheckboxDefaults.colors(checkedColor = neonCyan, checkmarkColor = Color.Black, uncheckedColor = textSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Aktifkan PPN", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Centang untuk mengaktifkan PPN, kosongkan untuk tidak ada PPN", color = textSecondary, fontSize = 12.sp)
                        }
                    }

                    if (enableTax) {
                        OutlinedTextField(
                            value = taxRateText,
                            onValueChange = { taxRateText = it },
                            label = { Text("PPN (%)", color = textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            singleLine = true,
                            supportingText = { Text("Persentase PPN yang akan ditambahkan ke harga paket.", color = textSecondary, fontSize = 12.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = "Rp ${formatter.format(finalPrice)}",
                        onValueChange = {},
                        label = { Text("Harga Setelah PPN", color = textSecondary) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = textSecondary.copy(alpha = 0.5f),
                            disabledTextColor = neonCyan,
                            disabledLabelColor = textSecondary,
                            disabledContainerColor = cardBg
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        maxLines = 5
                    )
                    

                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Batal", color = textSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                onSave(InternetPackage(
                                    id = "",
                                    name = name,
                                    speed = speed,
                                    price = price,
                                    taxRate = taxRate,
                                    description = description
                                    
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simpan Paket", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
