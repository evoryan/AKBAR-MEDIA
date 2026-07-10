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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.AdminUser
import com.example.ui.data.UserRole
import com.example.ui.data.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarAdminScreen(onBack: () -> Unit) {
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF333333)

    
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
        var selectedRole by remember { mutableStateOf(editItem?.role ?: UserRole.ADMIN) }
        var roleExpanded by remember { mutableStateOf(false) }

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
                                ApiClient.apiService.addAdmin(newItem)
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
                                adminList[index] = current.copy(name = name, username = username, role = selectedRole)
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
