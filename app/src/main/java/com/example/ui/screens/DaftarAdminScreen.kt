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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.AdminUser
import com.example.ui.data.UserRole
import com.example.ui.data.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarAdminScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    
    val adminList = remember { mutableStateListOf<AdminUser>() }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAdmins()
            adminList.clear()
            adminList.addAll(res)
        } catch (e: Exception) {
            // handle error
        }
    }

    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<AdminUser?>(null) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Daftar Admin", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (currentUser?.role == UserRole.SUPER_ADMIN) {
                FloatingActionButton(
                    onClick = { 
                        editItem = null
                        showDialog = true 
                    },
                    containerColor = primaryBg,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Akun")
                }
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
                items(adminList) { item ->
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
                                Icon(Icons.Default.Person, contentDescription = "Admin", tint = primaryBg, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Username: ${item.username}", color = textSecondary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Role: ${item.role.name}", color = primaryBg, fontSize = 14.sp)
                                    if (item.area_id != null && item.area_id != "semua") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Area ID: ${item.area_id}", color = textSecondary, fontSize = 12.sp)
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Area: Semua", color = textSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                            if (currentUser?.role == UserRole.SUPER_ADMIN && item.id != "1") {
                                Row {
                                    IconButton(onClick = {
                                        editItem = item
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryBg)
                                    }
                                    IconButton(onClick = {
                                        
            coroutineScope.launch {
                try {
                    ApiClient.apiService.deleteAdmin(item.id)
                    adminList.remove(item)
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
        var username by remember { mutableStateOf(editItem?.username ?: "") }
        var password by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(editItem?.role ?: UserRole.ADMIN) }
        var roleExpanded by remember { mutableStateOf(false) }
        var areas by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
        var selectedArea by remember { mutableStateOf(editItem?.area_id ?: "semua") }
        var areaExpanded by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            try {
                areas = com.example.ui.data.remote.ApiClient.apiService.getAreas()
            } catch (e: Exception) {}
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah Akun" else "Edit Akun") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (kosongkan jika tidak diubah)") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    
                    Box {
                        OutlinedButton(
                            onClick = { roleExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Text(selectedRole.name)
                        }
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                        ) {
                            UserRole.entries.forEach { role ->
                                if (role != UserRole.SUPER_ADMIN) {
                                    DropdownMenuItem(
                                        text = { Text(role.name, color = textMain) },
                                        onClick = {
                                            selectedRole = role
                                            roleExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Box {
                        OutlinedButton(
                            onClick = { areaExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Text(if (selectedArea == "semua") "Semua Area" else areas.find { it.id == selectedArea }?.name ?: selectedArea)
                        }
                        DropdownMenu(
                            expanded = areaExpanded,
                            onDismissRequest = { areaExpanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Area", color = textMain) },
                                onClick = {
                                    selectedArea = "semua"
                                    areaExpanded = false
                                }
                            )
                            areas.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area.name, color = textMain) },
                                    onClick = {
                                        selectedArea = area.id
                                        areaExpanded = false
                                    }
                                )
                            }
                        }
                    }

                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = AdminUser(
                                    id = "",
                                    name = name,
                                    username = username,
                                    role = selectedRole
                                )
                                // Send password as well if needed. Since AdminUser data class doesn't have password, we can create a temporary map or similar.
                                val adminMap = mapOf(
                                    "name" to name,
                                    "username" to username,
                                    "role" to selectedRole.name,
                                    "password" to password,
                                    "area_id" to selectedArea
                                )
                                ApiClient.apiService.addAdminMap(adminMap)
                                val res = ApiClient.apiService.getAdmins()
                                adminList.clear()
                                adminList.addAll(res)
                            } catch(e: Exception) {}
                        }
                    } else {
                        val index = adminList.indexOfFirst { it.id == editItem?.id }
                        if (index != -1) {
                            val current = editItem
                            if (current != null) {
                                val adminMap = mapOf(
                                    "name" to name,
                                    "username" to username,
                                    "role" to selectedRole.name,
                                    "password" to password,
                                    "area_id" to selectedArea
                                )
                                coroutineScope.launch {
                                    try {
                                        ApiClient.apiService.updateAdminMap(current.id, adminMap)
                                        adminList[index] = current.copy(name = name, username = username, role = selectedRole, area_id = selectedArea)
                                    } catch(e: Exception) {}
                                }
                            }
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
