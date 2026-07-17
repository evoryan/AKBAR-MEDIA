package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val neonCyan = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFAAAAAA) else Color(0xFF666666)

    var headerText by remember { mutableStateOf(SettingsManager.invoiceHeader) }
    var footerText by remember { mutableStateOf(SettingsManager.invoiceFooterText) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Invoice", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Header Invoice", color = textSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = headerText,
                onValueChange = { headerText = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                placeholder = { Text("Masukkan text header invoice...", color = textSecondary) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Footer Invoice", color = textSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = footerText,
                onValueChange = { footerText = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                placeholder = { Text("Masukkan text footer invoice...", color = textSecondary) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Preview Invoice", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                val currentMonthName = remember { java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date()) }
                com.example.ui.components.ThermalInvoiceView(
                    headerText = headerText,
                    footerText = footerText,
                    customer = null,
                    months = currentMonthName,
                    totalAmount = "Rp 150.000"
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    SettingsManager.invoiceHeader = headerText
                    SettingsManager.invoiceFooterText = footerText
                    Toast.makeText(context, "Pengaturan invoice berhasil disimpan", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SIMPAN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
