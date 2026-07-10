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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val primaryCyan = Color(0xFF00FFFF)
    val currentUser by UserSession.currentUser.collectAsState()
    val warningYellow = Color(0xFFFFC107)

    val dummyData = listOf(
        PembayaranData("Boseri", "B", Color(0xFFD1C4E9), "09896321543", "09 Jul 2026 - 07:07:00", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Taplikan", "T", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:47", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Kandab", "K", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:42", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Mahkun", "M", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 18:09:17", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Wahyudin", "W", Color(0xFFD1C4E9), "081234567890", "08 Jul 2026 - 07:08:47", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Sutris rt 01", "S", Color(0xFFD1C4E9), "085012345678", "08 Jul 2026 - 07:08:36", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Toin", "T", Color(0xFFD1C4E9), "6289525686652", "07 Jul 2026 - 19:16:47", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Turmi", "T", Color(0xFFD1C4E9), "089870339635", "07 Jul 2026 - 18:23:45", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun")
    )

    var filterDari by remember { mutableStateOf("hari ini") }
    var filterSampai by remember { mutableStateOf("hari ini") }
    var filterAdmin by remember { mutableStateOf(if (currentUser?.role == UserRole.COLLECTOR) currentUser?.name ?: "All" else "All") }
    var filterArea by remember { mutableStateOf("All") }

    var displayedData by remember { mutableStateOf(dummyData) }

    val filterOptionsDariSampai = listOf("hari ini", "kemarin", "minggu ini", "bulan ini")
    val filterOptionsAdmin = if (currentUser?.role == UserRole.COLLECTOR) listOf(currentUser?.name ?: "All") else listOf("All") + dummyData.map { it.admin }.distinct()
    val filterOptionsArea = listOf("All") + dummyData.map { it.area }.distinct()

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
                                displayedData = dummyData.filter { item ->
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
