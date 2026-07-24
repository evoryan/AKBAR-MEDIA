package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinkronisasiScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF11111A) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)

    var areaList by remember { mutableStateOf<List<Area>>(emptyList()) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }

    // States for Results
    var hasSynced by remember { mutableStateOf(false) }
    var totalMikrotikSecrets by remember { mutableStateOf(0) }
    var unusedSecretsList by remember { mutableStateOf<List<PPPoESecret>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Load Areas
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getAreas().filter { UserSession.isAreaIdAllowed(it.id) }
            areaList = res
            if (res.isNotEmpty()) {
                selectedArea = res.first()
            }
        } catch (e: Exception) {
            errorMsg = "Gagal memuat daftar area: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Run Sync Function
    val performSync: () -> Unit = {
        val area = selectedArea
        if (area != null) {
            isLoading = true
            errorMsg = null
            scope.launch {
                try {
                    // 1. Fetch live customers
                    val allCustomers = ApiClient.apiService.getCustomers()
                    
                    // 2. Map all used secret names (case-insensitive & trimmed)
                    val usedSecretNames = allCustomers.flatMap { 
                        listOfNotNull(
                            it.pppoeSecret?.lowercase()?.trim(),
                            it.username.lowercase().trim()
                        )
                    }.toSet()

                    // 3. Fetch Mikrotik Secrets for selected area
                    val mikrotikSecrets = ApiClient.apiService.getMikrotikSecrets(area.id)
                    totalMikrotikSecrets = mikrotikSecrets.size

                    // 4. Filter out used secrets
                    unusedSecretsList = mikrotikSecrets.filter { 
                        it.name.lowercase().trim() !in usedSecretNames 
                    }

                    hasSynced = true
                } catch (e: Exception) {
                    errorMsg = "Gagal melakukan sinkronisasi: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        } else {
            Toast.makeText(context, "Silakan pilih area terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Sinkronisasi Secret", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
            Spacer(modifier = Modifier.height(16.dp))

            // DROPDOWN AREA SELECTION
            Text(
                text = "PILIH AREA",
                color = primaryBg,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryBg, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedArea?.name ?: "Pilih Area...",
                            color = textMain,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    }
                    Icon(
                        imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = textSecondary
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(cardBg)
                ) {
                    if (areaList.isEmpty() && !isLoading) {
                        DropdownMenuItem(
                            text = { Text("Tidak ada area tersedia", color = textSecondary) },
                            onClick = { dropdownExpanded = false }
                        )
                    } else {
                        areaList.forEach { area ->
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryBg, modifier = Modifier.size(18.dp)) },
                                text = { Text(area.name, color = textMain, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedArea = area
                                    dropdownExpanded = false
                                    hasSynced = false // Reset sync on area change
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SYNC BUTTON
            Button(
                onClick = performSync,
                enabled = !isLoading && selectedArea != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBg,
                    contentColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color.Black else Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cek Secret & Sinkronisasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ERROR DISPLAY
            if (errorMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(errorMsg ?: "", color = Color(0xFFC62828), fontSize = 14.sp)
                    }
                }
            }

            // RESULTS DISPLAY
            if (hasSynced) {
                val filteredSecrets = unusedSecretsList.filter {
                    it.name.contains(searchKeyword, ignoreCase = true) ||
                    it.profile.contains(searchKeyword, ignoreCase = true)
                }

                Text(
                    text = "HASIL SINKRONISASI",
                    color = primaryBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Secret Router", color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalMikrotikSecrets", color = textMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Belum Digunakan", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${unusedSecretsList.size}", color = Color(0xFFFF9800), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar for unused list
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    placeholder = { Text("Cari secret atau profile...", color = textSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                    trailingIcon = {
                        if (searchKeyword.isNotEmpty()) {
                            IconButton(onClick = { searchKeyword = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = textSecondary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBg,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Secrets List
                if (filteredSecrets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Semua secret router sudah dipakai pelanggan!", color = textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredSecrets) { secret ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = secret.name,
                                            color = textMain,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(secret.profile, fontSize = 11.sp) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = primaryBg.copy(alpha = 0.1f),
                                                    labelColor = primaryBg
                                                ),
                                                border = null,
                                                modifier = Modifier.height(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val statusColor = when (secret.status) {
                                                "Online" -> Color(0xFF00FF4D)
                                                "Disabled" -> Color.Red
                                                else -> textSecondary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(statusColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(secret.status, color = textSecondary, fontSize = 11.sp)
                                        }
                                        if (secret.ipAddress.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("IP: ${secret.ipAddress}", color = textSecondary, fontSize = 12.sp)
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(secret.name))
                                            Toast.makeText(context, "Username secret disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Salin Nama Secret",
                                            tint = primaryBg,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (!isLoading) {
                // If not synced yet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ketuk tombol di atas untuk mulai membandingkan data secret Mikrotik dengan database pelanggan.",
                            color = textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
