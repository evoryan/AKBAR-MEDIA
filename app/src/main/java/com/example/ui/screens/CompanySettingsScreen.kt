package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySettingsScreen(onBack: () -> Unit) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val primaryBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF333333) else androidx.compose.ui.graphics.Color(0xFFE0E0E0)

    var companyName by remember { mutableStateOf(SettingsManager.companyName) }
    var info1 by remember { mutableStateOf(SettingsManager.dashboardInfo1) }
    var info2 by remember { mutableStateOf(SettingsManager.dashboardInfo2) }
    var invoiceFooter by remember { mutableStateOf(SettingsManager.invoiceFooterText) }
    var supportBy by remember { mutableStateOf(SettingsManager.supportByText) }
    var showSuccess by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Tampilan", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Nama Perusahaan (Header)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = info1,
                onValueChange = { info1 = it },
                label = { Text("Informasi Sistem 1") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = info2,
                onValueChange = { info2 = it },
                label = { Text("Informasi Sistem 2") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = invoiceFooter,
                onValueChange = { invoiceFooter = it },
                label = { Text("Teks Footer Invoice") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = supportBy,
                onValueChange = { supportBy = it },
                label = { Text("Support By (Invoice)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    SettingsManager.companyName = companyName
                    SettingsManager.dashboardInfo1 = info1
                    SettingsManager.dashboardInfo2 = info2
                    SettingsManager.invoiceFooterText = invoiceFooter
                    SettingsManager.supportByText = supportBy
                    showSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBg, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
            }

            if (showSuccess) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pengaturan berhasil disimpan", color = primaryBg, fontSize = 14.sp)
            }
        }
    }
}
