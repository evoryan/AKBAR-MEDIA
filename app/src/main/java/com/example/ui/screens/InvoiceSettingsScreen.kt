package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.SettingsManager
import com.example.ui.util.InvoiceGenerator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgMain = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val cardBg = if (isDark) Color(0xFF141414) else Color.White
    val cardBorder = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val neonCyan = if (isDark) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)

    var headerText by remember { mutableStateOf(SettingsManager.invoiceHeader) }
    var footerText by remember { mutableStateOf(SettingsManager.invoiceFooterText) }
    var logoPath by remember { mutableStateOf(SettingsManager.invoiceLogoPath) }

    var previewRefreshKey by remember { mutableStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val destinationFile = File(context.filesDir, "invoice_kop_logo.png")
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                logoPath = destinationFile.absolutePath
                SettingsManager.invoiceLogoPath = destinationFile.absolutePath
                SettingsManager.useInvoiceLogo = true
                previewRefreshKey++
                Toast.makeText(context, "Logo kop invoice berhasil diunggah", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengunggah logo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Pengaturan Invoice",
                            color = textMain,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Ukuran 200mm x 140mm (Kop Berlogo)",
                            color = neonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
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
            Spacer(modifier = Modifier.height(12.dp))

            // Card 1: Upload Logo Kop Surat
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Logo Kop Surat Invoice",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Surface(
                            color = neonCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "200mm x 140mm",
                                color = neonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Upload logo perusahaan Anda untuk ditampilkan di sebelah kiri kop surat invoice resmi.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val currentLogoFile = logoPath?.let { File(it) }
                    val isLogoReady = currentLogoFile != null && currentLogoFile.exists()

                    if (isLogoReady) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, neonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val logoBitmap = remember(currentLogoFile, previewRefreshKey) {
                                try {
                                    BitmapFactory.decodeFile(currentLogoFile!!.absolutePath)
                                } catch (_: Exception) {
                                    null
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoBitmap != null) {
                                    Image(
                                        bitmap = logoBitmap.asImageBitmap(),
                                        contentDescription = "Logo Kop",
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Logo Terpasang pada Kop",
                                    color = Color(0xFF0F2C59),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Dimensi invoice: 200mm x 140mm",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ganti", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                currentLogoFile?.delete()
                                            } catch (_: Exception) {}
                                            logoPath = null
                                            SettingsManager.invoiceLogoPath = null
                                            previewRefreshKey++
                                            Toast.makeText(context, "Logo kop dihapus", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Hapus", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Upload Logo Action Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(neonCyan.copy(alpha = 0.05f))
                                .border(1.dp, neonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Logo Kop",
                                    tint = neonCyan,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Ketuk untuk Upload Logo Perusahaan (.png / .jpg)",
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Logo akan diposisikan di sisi kiri kop surat invoice 200mm x 140mm",
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Informasi Kop Surat & Faktur
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Informasi Kop Surat & Perusahaan",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Teks ini akan muncul berdampingan dengan logo pada kop surat atas faktur.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Header / Alamat Kop Surat", color = textSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = headerText,
                        onValueChange = {
                            headerText = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Nama Perusahaan\nAlamat Lengkap\nNo. Kontak / WA", color = textSecondary) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Catatan Pembayaran / Footer", color = textSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = {
                            footerText = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: Pembayaran melalui transfer bank atau cash...", color = textSecondary) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 3: Pratinjau Invoice Ukuran 200mm x 140mm
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Pratinjau Invoice (200mm x 140mm)",
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Kop Berlogo • Format Continuous / Faktur .PNG",
                        color = textSecondary,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = neonCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "200 x 140 mm",
                        color = neonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val currentMonthName = remember {
                SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Generate 200mm x 140mm preview bitmap
                    val previewBitmap = remember(logoPath, headerText, footerText, previewRefreshKey) {
                        try {
                            SettingsManager.invoiceHeader = headerText
                            SettingsManager.invoiceFooterText = footerText
                            SettingsManager.invoiceLogoPath = logoPath

                            InvoiceGenerator.generateInvoiceBitmap(
                                context = context,
                                customerName = "Budi Santoso",
                                customerPhone = "0812-3456-7890",
                                customerArea = "Area Timur",
                                packageName = "Paket Family 20 Mbps",
                                months = currentMonthName,
                                totalAmount = "Rp 150.000",
                                status = "BELUM BAYAR",
                                invoiceNo = "INV-SAMPLE"
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "Invoice 200mm x 140mm Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(200f / 140f) // Exact 200mm x 140mm ratio
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = neonCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    SettingsManager.invoiceHeader = headerText
                    SettingsManager.invoiceFooterText = footerText
                    SettingsManager.invoiceLogoPath = logoPath
                    SettingsManager.useInvoiceLogo = true
                    Toast.makeText(context, "Pengaturan invoice 200mm x 140mm berhasil disimpan", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIMPAN PENGATURAN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
