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
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import com.example.ui.data.CategoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KategoriScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF333333)

    var searchQuery by remember { mutableStateOf("") }
    
    var categoriesList by remember { mutableStateOf<List<com.example.ui.data.CategoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            categoriesList = ApiClient.apiService.getCategories()
        } catch (e: Exception) {
        }
    }


    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<CategoryItem?>(null) }

    val filteredList = categoriesList.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Kategori Barang", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Cari kategori...", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain,
                    cursorColor = primaryBg,
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList) { item ->
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
                            Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
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
                    ApiClient.apiService.deleteCategory(item.id)
                    categoriesList = categoriesList.filter { it.id != item.id }
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

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah Kategori" else "Edit Kategori") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBg,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = CategoryItem(
                                    id = "",
                                    name = name
                                )
                                ApiClient.apiService.addCategory(newItem)
                                categoriesList = ApiClient.apiService.getCategories()
                            } catch(e: Exception) {}
                        }
                    } else {
                        categoriesList = categoriesList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name) else it
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
