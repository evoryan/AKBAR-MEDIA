package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import com.opencsv.CSVReader
import com.opencsv.CSVWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val bgMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val textMain = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val primaryBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val cardBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF) else Color(0xFFFFFFFF)
    val cardBorder = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFE0E0E0)
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                try {
                    val customers = ApiClient.apiService.getCustomers()
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        val writer = CSVWriter(OutputStreamWriter(os))
                        writer.writeNext(arrayOf("id", "name", "phone", "area", "username", "billingDate", "status", "price", "discount", "registerDate", "isolateDate", "packageName", "additionalCost1", "additionalCost2", "pppoeSecret", "odpId", "odpPort"))
                        for (c in customers) {
                            writer.writeNext(arrayOf(c.id, c.name, c.phone, c.area, c.username, c.billingDate, c.status, c.price, c.discount, c.registerDate ?: "", c.isolateDate ?: "", c.packageName ?: "", c.additionalCost1 ?: "", c.additionalCost2 ?: "", c.pppoeSecret ?: "", c.odpId ?: "", c.odpPort ?: ""))
                        }
                        writer.close()
                    }
                    Toast.makeText(context, "Backup berhasil disimpan", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal backup: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.use { isStream ->
                        val reader = CSVReader(InputStreamReader(isStream))
                        val rows = reader.readAll()
                        if (rows.isNotEmpty() && rows.size > 1) {
                            // skip header
                            for (i in 1 until rows.size) {
                                val row = rows[i]
                                if (row.size >= 17) {
                                    val req = com.example.ui.screens.Customer(
                                        name = row[1],
                                        phone = row[2],
                                        area = row[3],
                                        username = row[4],
                                        billingDate = row[5],
                                        status = row[6],
                                        price = row[7],
                                        discount = row[8],
                                        registerDate = row[9],
                                        isolateDate = row[10],
                                        packageName = row[11],
                                        additionalCost1 = row[12],
                                        additionalCost2 = row[13],
                                        pppoeSecret = row[14],
                                        odpId = row[15],
                                        odpPort = row[16]
                                    )
                                    ApiClient.apiService.addCustomer(req)
                                }
                            }
                            Toast.makeText(context, "Restore berhasil", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "File kosong atau format salah", Toast.LENGTH_SHORT).show()
                        }
                        reader.close()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal restore: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .clickable { 
                        if (!isLoading) {
                            exportLauncher.launch("backup_pelanggan.csv")
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = primaryBg)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Backup Data Pelanggan (.csv)", color = textMain, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .clickable { 
                        if (!isLoading) {
                            importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values"))
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = primaryBg)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Restore Data Pelanggan (.csv)", color = textMain, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(color = primaryBg)
            }
        }
    }
}
