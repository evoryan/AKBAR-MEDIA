package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

data class PembayaranData(
    val name: String,
    val initial: String,
    val initialBg: Color,
    val phone: String,
    val payDate: String,
    val payMonth: String,
    val amount: String,
    val area: String,
    val admin: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PembayaranByAdminScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val primaryCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val currentUser by UserSession.currentUser.collectAsState()
    val warningYellow = Color(0xFFFFC107)

    var realData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }
    var filterDari by remember { mutableStateOf("hari ini") }
    var filterSampai by remember { mutableStateOf("hari ini") }
    var filterAdmin by remember { mutableStateOf(if (currentUser?.role == UserRole.COLLECTOR) "Admin By ${currentUser?.name}" else "All") }
    var filterArea by remember { mutableStateOf("All") }
    var displayedData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getPembayaranHistory()
            realData = res.map { item ->
                PembayaranData(
                    name = item.customer_name ?: "Unknown",
                    initial = (item.customer_name ?: "U").take(1).uppercase(),
                    initialBg = Color(0xFFD1C4E9),
                    phone = item.phone ?: "-",
                    payDate = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    payMonth = "${item.bulan} ${item.tahun}",
                    amount = "Rp. ${String.format("%,d", (item.amount ?: 0.0).toLong()).replace(",", ".")}",
                    area = item.area ?: "-",
                    admin = "Admin By ${item.admin_name ?: "Unknown"}"
                )
            }
            // Auto apply default filter on load
            displayedData = realData.filter { item ->
                (filterAdmin == "All" || item.admin == filterAdmin) &&
                (filterArea == "All" || item.area == filterArea)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    val filterOptionsDariSampai = listOf("hari ini", "kemarin", "minggu ini", "bulan ini")
    val filterOptionsAdmin by remember(realData, currentUser) {
        derivedStateOf {
            if (currentUser?.role == UserRole.COLLECTOR) listOf("Admin By ${currentUser?.name}") else listOf("All") + realData.map { it.admin }.distinct()
        }
    }
    val filterOptionsArea by remember(realData) {
        derivedStateOf {
            listOf("All") + realData.map { it.area }.distinct()
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF11111A))) {
                TopAppBar(
                    title = { Text("Pembyrn By Admin", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            FilterDropdownMenu(
                                label = "Dari",
                                selectedOption = filterDari,
                                options = filterOptionsDariSampai,
                                onOptionSelected = { filterDari = it },
                                primaryCyan = primaryCyan,
                                textSecondary = textSecondary
                            )
                            FilterDropdownMenu(
                                label = "Admin",
                                selectedOption = filterAdmin,
                                options = filterOptionsAdmin,
                                onOptionSelected = { filterAdmin = it },
                                primaryCyan = primaryCyan,
                                textSecondary = textSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            FilterDropdownMenu(
                                label = "Sampai",
                                selectedOption = filterSampai,
                                options = filterOptionsDariSampai,
                                onOptionSelected = { filterSampai = it },
                                primaryCyan = primaryCyan,
                                textSecondary = textSecondary
                            )
                            FilterDropdownMenu(
                                label = "Area",
                                selectedOption = filterArea,
                                options = filterOptionsArea,
                                onOptionSelected = { filterArea = it },
                                primaryCyan = primaryCyan,
                                textSecondary = textSecondary
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp).background(Color.White, CircleShape).padding(2.dp))
                        Button(
                            onClick = {
                                displayedData = realData.filter { item ->
                                    (filterAdmin == "All" || item.admin == filterAdmin) &&
                                    (filterArea == "All" || item.area == filterArea)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("FILTER", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF8B008B))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                // For demonstration, calculating total based on size
                val totalAmount = displayedData.size * 100000
                Text("Rp. ${String.format("%,d", totalAmount).replace(",", ".")}", color = warningYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain)
                .padding(innerPadding)
        ) {
            items(displayedData) { data ->
                PembayaranItemCard(data)
                Divider(color = Color(0xFF333333), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun FilterDropdownMenu(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    primaryCyan: Color,
    textSecondary: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Column(modifier = Modifier.clickable { expanded = true }) {
            Text(label, color = textSecondary, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedOption, color = primaryCyan, fontSize = 12.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryCyan, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF11111A)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PembayaranItemCard(data: PembayaranData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(data.initialBg),
            contentAlignment = Alignment.Center
        ) {
            Text(data.initial, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Normal)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(data.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(data.phone, color = Color(0xFFAAAAAA), fontSize = 12.sp)
            Text("Bayar Tgl : ${data.payDate}", color = Color(0xFFAAAAAA), fontSize = 12.sp)
            Row {
                Text("Bayar Bulan : ", color = Color(0xFF00FFFF), fontSize = 12.sp)
                Text(data.payMonth, color = Color(0xFF00FFFF), fontSize = 12.sp)
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(data.amount, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(data.area, color = Color(0xFFAAAAAA), fontSize = 12.sp)
            Text(data.admin, color = Color(0xFF00FFFF), fontSize = 12.sp)
        }
    }
}
