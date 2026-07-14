package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.data.remote.ApiClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryStockScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    val iconSuccess = Color(0xFF00FF4D)
    val iconError = Color(0xFFFF003C)

    var searchQuery by remember { mutableStateOf("") }
    
    var historyList by remember { mutableStateOf<List<com.example.ui.data.StockHistory>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            historyList = ApiClient.apiService.getStockHistory()
        } catch (e: Exception) {
        }
    }


    var sortByNewest by remember { mutableStateOf(true) }
    var sortExpanded by remember { mutableStateOf(false) }

    val filteredList = historyList
        .filter { it.itemName.contains(searchQuery, ignoreCase = true) || it.adminName.contains(searchQuery, ignoreCase = true) }
        .let { list ->
            if (sortByNewest) list.sortedByDescending { it.timestamp } else list.sortedBy { it.timestamp }
        }

    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("History Stock", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search & Sort Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari history...", color = textSecondary) },
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

                // Sort Dropdown
                Box {
                    OutlinedButton(
                        onClick = { sortExpanded = true },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (sortByNewest) "Terbaru" else "Terlama")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false },
                        modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Terbaru", color = textMain) },
                            onClick = {
                                sortByNewest = true
                                sortExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Terlama", color = textMain) },
                            onClick = {
                                sortByNewest = false
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Oleh: ${item.adminName}", color = textSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(sdf.format(item.timestamp), color = textSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val isAdd = item.type == "IN"
                                val color = if (isAdd) iconSuccess else iconError
                                val prefix = if (isAdd) "+" else "-"
                                Text("$prefix${item.quantity}", color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(if (isAdd) "Masuk" else "Keluar", color = color, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
