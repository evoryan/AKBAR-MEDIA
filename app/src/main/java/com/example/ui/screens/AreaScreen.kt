package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.remote.ApiClient
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class Area(
    val id: String,
    val name: String,
    val description: String = "",
    val customerCount: Int = 0,
    val routerIp: String = "",
    val apiDomain: String = "",
    val mikrotikUser: String = "",
    val mikrotikPassword: String = "",
    val acsUser: String = "",
    val acsPassword: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val neonCyan = Color(0xFF00FFFF)
    val primaryPurple = Color(0xFF2B0B3F)
    val errorRed = Color(0xFFFF003C)
    
    var showAddDialog by remember { mutableStateOf(false) }
    var areaToEdit by remember { mutableStateOf<Area?>(null) }

    val areas = remember { mutableStateListOf<Area>() }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAreas()
            areas.clear()
            areas.addAll(res)
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kelola Area", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
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
                Icon(Icons.Default.Add, contentDescription = "Add Area")
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
            Text("Daftar Area", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(areas) { area ->
                    AreaItem(
                        area = area, 
                        cardBg = cardBg, 
                        cardBorder = cardBorder, 
                        neonCyan = neonCyan, 
                        errorRed = errorRed,
                        textMain = textMain, 
                        textSecondary = textSecondary,
                        onEdit = { areaToEdit = area },
                        onDelete = { 
                            coroutineScope.launch {
                                try {
                                    ApiClient.apiService.deleteArea(area.id)
                                    areas.remove(area)
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // FAB padding
            }
        }
        
        if (showAddDialog || areaToEdit != null) {
            AreaFormDialog(
                initialArea = areaToEdit,
                onDismiss = { 
                    showAddDialog = false
                    areaToEdit = null
                },
                onSave = { newArea ->
                    if (areaToEdit != null) {
                        val index = areas.indexOfFirst { it.id == areaToEdit!!.id }
                        if (index != -1) {
                            areas[index] = newArea.copy(id = areaToEdit!!.id)
                        }
                    } else {
                        coroutineScope.launch {
                            try {
                                com.example.ui.data.remote.ApiClient.apiService.addArea(newArea)
                                val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
                                areas.clear()
                                areas.addAll(res)
                            } catch(e: Exception) {}
                        }
                    }
                    showAddDialog = false
                    areaToEdit = null
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
fun AreaItem(
    area: Area,
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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(neonCyan.copy(alpha = 0.1f))
                            .border(1.dp, neonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = neonCyan)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (area.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(area.description, color = textSecondary, fontSize = 14.sp)
                        }
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
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pelanggan Terintegrasi", color = textSecondary, fontSize = 12.sp)
                        Text("${area.customerCount} Pelanggan", color = textMain, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("API Mikrotik", color = textSecondary, fontSize = 12.sp)
                        Text(if(area.routerIp.isEmpty()) "-" else area.routerIp, color = textMain, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF00FFFF),
            title = { Text("Hapus Area", color = textMain, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus area '${area.name}'?", color = textSecondary) },
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
fun AreaFormDialog(
    initialArea: Area?,
    onDismiss: () -> Unit,
    onSave: (Area) -> Unit,
    bgMain: Color,
    textMain: Color,
    textSecondary: Color,
    cardBg: Color,
    neonCyan: Color,
    primaryPurple: Color
) {
    var name by remember { mutableStateOf(initialArea?.name ?: "") }
    var description by remember { mutableStateOf(initialArea?.description ?: "") }
    var mikrotikApi by remember { mutableStateOf(initialArea?.routerIp ?: "") }
    var mikrotikUser by remember { mutableStateOf(initialArea?.mikrotikUser ?: "") }
    var mikrotikPassword by remember { mutableStateOf(initialArea?.mikrotikPassword ?: "") }
    var acsApi by remember { mutableStateOf(initialArea?.apiDomain ?: "") }
    var acsUser by remember { mutableStateOf(initialArea?.acsUser ?: "") }
    var acsPassword by remember { mutableStateOf(initialArea?.acsPassword ?: "") }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val title = if (initialArea == null) "Tambah Area Baru" else "Edit Area"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain),
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
                        label = { Text("Nama Area", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = mikrotikApi,
                        onValueChange = { mikrotikApi = it },
                        label = { Text("Alamat API Mikrotik (IP:Port)", color = textSecondary) },
                        placeholder = { Text("Contoh: 192.168.1.1:8728", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mikrotikUser,
                        onValueChange = { mikrotikUser = it },
                        label = { Text("Username API Mikrotik", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mikrotikPassword,
                        onValueChange = { mikrotikPassword = it },
                        label = { Text("Password API Mikrotik", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                isTesting = true
                                testResult = null
                                coroutineScope.launch {
                                    delay(1500) // Simulate network call
                                    if (mikrotikApi.isNotEmpty() && mikrotikApi.contains(":")) {
                                        testResult = "Koneksi ke $mikrotikApi berhasil!"
                                    } else {
                                        testResult = "Koneksi gagal. Periksa kembali alamat API."
                                    }
                                    isTesting = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = neonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = neonCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Menguji...", fontSize = 12.sp)
                            } else {
                                Text("Test Mikrotik", fontSize = 12.sp)
                            }
                        }
                    }
                    
                    if (testResult != null) {
                        val isSuccess = testResult!!.contains("berhasil")
                        Text(
                            text = testResult!!,
                            color = if (isSuccess) Color(0xFF00FF00) else Color(0xFFFF003C),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = acsApi,
                        onValueChange = { acsApi = it },
                        label = { Text("Alamat ACS (URL)", color = textSecondary) },
                        placeholder = { Text("Contoh: http://192.168.1.1:7557", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acsUser,
                        onValueChange = { acsUser = it },
                        label = { Text("Username ACS", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acsPassword,
                        onValueChange = { acsPassword = it },
                        label = { Text("Password ACS", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
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
                                onSave(Area(
                                    id = "",
                                    name = name,
                                    description = description,
                                    routerIp = mikrotikApi,
                                    apiDomain = acsApi,
                                    customerCount = initialArea?.customerCount ?: 0,
                                    mikrotikUser = mikrotikUser,
                                    mikrotikPassword = mikrotikPassword,
                                    acsUser = acsUser,
                                    acsPassword = acsPassword
                                ))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
