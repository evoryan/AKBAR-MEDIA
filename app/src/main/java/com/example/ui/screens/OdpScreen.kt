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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
import com.example.ui.data.OdpItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdpScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF333333)

    var odpList by remember { mutableStateOf<List<com.example.ui.data.OdpItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdpList()
            odpList = res
        } catch (e: Exception) {
        }
    }
    
    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdcList()
            odcList = res
        } catch (e: Exception) {
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<OdpItem?>(null) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kelola ODP", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah ODP")
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
                items(odpList) { item ->
                    val odc = odcList.find { it.id == item.odcId }
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Hub, contentDescription = "ODP", tint = primaryBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ODC: ${odc?.name ?: "Unknown"}", color = textSecondary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Port: ${item.portCount}", color = primaryBg, fontSize = 14.sp)
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
                    ApiClient.apiService.deleteOdp(item.id)
                    odpList = odpList.filter { it.id != item.id }
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
        var portCount by remember { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var selectedOdc by remember { mutableStateOf(editItem?.odcId ?: odcList.firstOrNull()?.id ?: "") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah ODP" else "Edit ODP") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama ODP") },
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
                    // ODC Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            val odcName = odcList.find { it.id == selectedOdc }?.name ?: "Pilih ODC"
                            Text(odcName)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                        ) {
                            odcList.forEach { odc ->
                                DropdownMenuItem(
                                    text = { Text(odc.name, color = textMain) },
                                    onClick = {
                                        selectedOdc = odc.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val port = portCount.toIntOrNull() ?: 0
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdpItem(
                                    id = "",
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port
                                )
                                ApiClient.apiService.addOdp(newItem)
                                odpList = ApiClient.apiService.getOdpList()
                            } catch(e: Exception) {}
                        }
                    } else {
                        odpList = odpList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, odcId = selectedOdc, portCount = port) else it
                        }
                    }
                    showDialog = false
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
