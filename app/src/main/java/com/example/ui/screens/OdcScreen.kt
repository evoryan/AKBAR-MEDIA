package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.unit.sp
import com.example.ui.data.remote.ApiClient
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import com.example.ui.data.OdcItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdcScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdcList()
            odcList = res
        } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
        }
    }
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<OdcItem?>(null) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kelola ODC", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editItem = null
                    showDialog = true 
                },
                containerColor = primaryBg,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah ODC")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                items(odcList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeviceHub, contentDescription = "ODC", tint = primaryBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.location, color = textSecondary, fontSize = 14.sp)
                                    if (item.portCount > 0) {
                                        Text("Port: ${item.portCount} | Input: ${item.portInput}", color = primaryBg, fontSize = 12.sp)
                                    }
                                }
                            }
                            Row {
                                if (currentUser?.role == UserRole.SUPER_ADMIN) {
                                    IconButton(onClick = {
                                    editItem = item
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryBg)
                                }
                                IconButton(onClick = {
                                    
            coroutineScope.launch {
                try {
                    ApiClient.apiService.deleteOdc(item.id)
                    odcList = odcList.filter { it.id != item.id }
                } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                }
            }
        
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var location by remember { mutableStateOf(editItem?.location ?: "") }
        var portCount by remember { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var portInput by remember { mutableStateOf(editItem?.portInput ?: "") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah ODC" else "Edit ODC") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama ODC") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = portCount,
                        onValueChange = { portCount = it },
                        label = { Text("Jumlah Port") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = portInput,
                        onValueChange = { portInput = it },
                        label = { Text("Sumber Port Input") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdcItem(
                                    id = "",
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput
                                )
                                ApiClient.apiService.addOdc(newItem)
                                odcList = ApiClient.apiService.getOdcList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch(e: Exception) {
                                Log.e("OdcScreen", "Error adding ODC", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedItem = OdcItem(
                                    id = editItem!!.id,
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput
                                )
                                ApiClient.apiService.updateOdc(updatedItem.id, updatedItem)
                                odcList = ApiClient.apiService.getOdcList()
                                showDialog = false
                            } catch(e: retrofit2.HttpException) {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("OdcScreen", "HTTP Error: $errBody", e)
                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("OdcScreen", "Error updating ODC", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text("Simpan", color = primaryBg)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
}
