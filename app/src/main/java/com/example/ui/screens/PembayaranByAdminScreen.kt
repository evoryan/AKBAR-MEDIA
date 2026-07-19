package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.AdminUser
import com.example.ui.data.UserRole
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import com.example.ui.screens.Area
import kotlinx.coroutines.launch

data class PembayaranData(
    val id: String,
    val name: String,
    val initial: String,
    val initialBg: Color,
    val phone: String,
    val payDate: String,
    val payMonth: String,
    val amountVal: Double,
    val amountFormatted: String,
    val area: String,
    val adminName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranByAdminScreen(onBack: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgMain = if (isDark) Color(0xFF0A0A0C) else Color(0xFFF5F7FA)
    val cardBg = if (isDark) Color(0xFF16161F) else Color(0xFFFFFFFF)
    val headerBg = if (isDark) Color(0xFF1E1E2A) else Color(0xFFFFEBF5)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFA0A0C0) else Color(0xFF666666)
    val primaryColor = if (isDark) Color(0xFF00E5FF) else Color(0xFF0066FF)
    val accentYellow = Color(0xFFFFC107)
    val accentGreen = Color(0xFF4CAF50)

    val currentUser by UserSession.currentUser.collectAsState()

    var realData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }
    var registeredAdmins by remember { mutableStateOf<List<String>>(emptyList()) }
    var registeredAreas by remember { mutableStateOf<List<String>>(emptyList()) }
    
    var selectedAdmin by remember { mutableStateOf("Semua Admin") }
    var selectedBulan by remember { mutableStateOf("Bulan") }
    var selectedArea by remember { mutableStateOf("Semua Area") }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    fun loadData() {
        coroutineScope.launch {
            try {
                isLoading = true
                val payments = ApiClient.apiService.getPembayaranHistory()
                
                // Fetch registered admins and areas
                val admins = try { ApiClient.apiService.getAdmins() } catch (e: Exception) { emptyList() }
                val areas = try { ApiClient.apiService.getAreas() } catch (e: Exception) { emptyList() }
                
                registeredAdmins = admins.map { it.name }.distinct()
                registeredAreas = areas.map { it.name }.distinct()

                realData = payments.map { item ->
                    val cName = item.customer_name ?: "Unknown"
                    val initials = cName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.take(1) }.joinToString("").uppercase()
                    val rColor = when (initials.hashCode() % 5) {
                        0 -> Color(0xFFD1C4E9)
                        1 -> Color(0xFFB3E5FC)
                        2 -> Color(0xFFC8E6C9)
                        3 -> Color(0xFFFFCC80)
                        else -> Color(0xFFFFCDD2)
                    }
                    PembayaranData(
                        id = item.id,
                        name = cName,
                        initial = if (initials.isNotEmpty()) initials else "U",
                        initialBg = rColor,
                        phone = item.phone ?: "-",
                        payDate = item.created_at?.take(10) ?: "",
                        payMonth = item.bulan ?: "Unknown",
                        amountVal = item.amount ?: 0.0,
                        amountFormatted = "Rp. ${String.format("%,d", (item.amount ?: 0.0).toLong()).replace(",", ".")}",
                        area = item.area ?: "-",
                        adminName = item.admin_name ?: "Unknown"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    // Dynamic Filter Option Lists
    val filterOptionsAdmin by remember(registeredAdmins) {
        derivedStateOf {
            listOf("Semua Admin") + registeredAdmins
        }
    }
    val filterOptionsBulan = remember {
        listOf(
            "Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
    }
    val filterOptionsArea by remember(registeredAreas, realData) {
        derivedStateOf {
            listOf("Semua Area") + (registeredAreas + realData.map { it.area }).distinct()
        }
    }

    // Real-time Reactive Filtering
    val displayedData by remember(realData, selectedAdmin, selectedBulan, selectedArea) {
        derivedStateOf {
            realData.filter { item ->
                val matchAdmin = (selectedAdmin == "Semua Admin" || item.adminName == selectedAdmin)
                val matchBulan = (selectedBulan == "Bulan" || item.payMonth.equals(selectedBulan, ignoreCase = true))
                val matchArea = (selectedArea == "Semua Area" || item.area == selectedArea)
                matchAdmin && matchBulan && matchArea
            }
        }
    }

    // Real-time Reactive Total Calculation
    val totalAmount by remember(displayedData) {
        derivedStateOf {
            displayedData.sumOf { it.amountVal }
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Pembayaran by Admin", 
                        color = textMain, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Kembali", 
                            tint = textMain
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = "Segarkan", 
                            tint = textMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-fidelity Filter Card (With exact requested dropdowns)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Filter Laporan", 
                        color = textMain, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Admin Dropdown
                    CustomExposedDropdown(
                        label = "Admin Penerima",
                        selectedOption = selectedAdmin,
                        options = filterOptionsAdmin,
                        onOptionSelected = { selectedAdmin = it },
                        icon = Icons.Default.Person,
                        accentColor = primaryColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dropdown Bulan
                        CustomExposedDropdown(
                            label = "Bulan",
                            selectedOption = selectedBulan,
                            options = filterOptionsBulan,
                            onOptionSelected = { selectedBulan = it },
                            icon = Icons.Default.DateRange,
                            accentColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )

                        // Dropdown Area
                        CustomExposedDropdown(
                            label = "Area",
                            selectedOption = selectedArea,
                            options = filterOptionsArea,
                            onOptionSelected = { selectedArea = it },
                            icon = Icons.Default.Place,
                            accentColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Dynamic Total Indicator Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E1E2F) else Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Pembayaran Terfilter", 
                            color = textSecondary, 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp. ${String.format("%,d", totalAmount.toLong()).replace(",", ".")}", 
                            color = if (isDark) accentYellow else Color(0xFF0D47A1), 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(if (isDark) Color(0xFF33334D) else Color(0xFFBBDEFB), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${displayedData.size} Transaksi", 
                            color = if (isDark) Color.White else Color(0xFF0D47A1), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Contents (Loading, Empty, or Beautiful Payments List)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (displayedData.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null, 
                            tint = textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Data Pembayaran", 
                            color = textMain, 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tidak ditemukan data yang cocok dengan kriteria filter saat ini.", 
                            color = textSecondary, 
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedData, key = { it.id }) { data ->
                            PembayaranItemCard(data, isDark, textMain, textSecondary, accentGreen)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExposedDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option, 
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        ) 
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun PembayaranItemCard(
    data: PembayaranData,
    isDark: Boolean,
    textMain: Color,
    textSecondary: Color,
    accentGreen: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E1E2A) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(data.initialBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.initial, 
                    color = Color(0xFF1E1E2A), 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.name, 
                    color = textMain, 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = data.phone, 
                    color = textSecondary, 
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(accentGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = data.payMonth, 
                            color = accentGreen, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "• ${data.payDate}", 
                        color = textSecondary, 
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = data.amountFormatted, 
                    color = textMain, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.area, 
                    color = textSecondary, 
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(textSecondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "By: ${data.adminName}", 
                        color = textMain, 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
