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
import com.example.ui.data.InventoryItem
import com.example.ui.data.StockHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF333333)

    var searchQuery by remember { mutableStateOf("") }
    
    var inventoryList by remember { mutableStateOf<List<com.example.ui.data.InventoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            inventoryList = ApiClient.apiService.getInventory()
        } catch (e: Exception) {
        }
    }

    
    var categoriesList by remember { mutableStateOf<List<com.example.ui.data.CategoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            categoriesList = ApiClient.apiService.getCategories()
        } catch (e: Exception) {
        }
    }


    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<InventoryItem?>(null) }

    val filteredList = inventoryList.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Inventory Barang", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Barang")
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
                placeholder = { Text("Cari barang...", color = textSecondary) },
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
                    val category = categoriesList.find { it.id == item.categoryId }?.name ?: "Unknown"
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Kategori: $category", color = textSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Stock: ${item.stock}", color = primaryBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    ApiClient.apiService.deleteInventory(item.id)
                    inventoryList = inventoryList.filter { it.id != item.id }
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
        var stock by remember { mutableStateOf(editItem?.stock?.toString() ?: "") }
        var selectedCategory by remember { mutableStateOf(editItem?.categoryId ?: categoriesList.firstOrNull()?.id ?: "") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text(if (editItem == null) "Tambah Barang" else "Edit Barang") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Barang") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Jumlah Stock") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    // Category Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            val catName = categoriesList.find { it.id == selectedCategory }?.name ?: "Pilih Kategori"
                            Text(catName)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                        ) {
                            categoriesList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name, color = textMain) },
                                    onClick = {
                                        selectedCategory = cat.id
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
                    val newStock = stock.toIntOrNull() ?: 0
                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = InventoryItem(
                                    id = "",
                                    name = name,
                                    categoryId = selectedCategory,
                                    stock = newStock
                                )
                                ApiClient.apiService.addInventory(newItem)
                                inventoryList = ApiClient.apiService.getInventory()
                            } catch(e: Exception) {}
                        }
                    } else {
                        val diff = newStock - (editItem?.stock ?: 0)
                        if(diff != 0) {
                             
                                }
                        inventoryList = inventoryList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, categoryId = selectedCategory, stock = newStock) else it
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
