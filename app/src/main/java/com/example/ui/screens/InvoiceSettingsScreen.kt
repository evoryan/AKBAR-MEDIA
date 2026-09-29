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
    var useCustomTemplate by remember { mutableStateOf(SettingsManager.useCustomInvoiceTemplate) }
    var customTemplatePath by remember { mutableStateOf(SettingsManager.customInvoiceTemplatePath) }
    var overlayData by remember { mutableStateOf(SettingsManager.customInvoiceOverlayData) }

    var previewRefreshKey by remember { mutableStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val destinationFile = File(context.filesDir, "custom_invoice_template.png")
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                customTemplatePath = destinationFile.absolutePath
                useCustomTemplate = true
                SettingsManager.customInvoiceTemplatePath = destinationFile.absolutePath
                SettingsManager.useCustomInvoiceTemplate = true
                previewRefreshKey++
                Toast.makeText(context, "Template kustom (.png) berhasil diunggah", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            Spacer(modifier = Modifier.height(12.dp))

            // Template Mode Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Tipe Desain Invoice",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Pilih format invoice yang dikirim saat kirim WA tagihan (.png) dan struk",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Standar Thermal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!useCustomTemplate) neonCyan.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (!useCustomTemplate) neonCyan else cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                useCustomTemplate = false
                                previewRefreshKey++
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !useCustomTemplate,
                            onClick = {
                                useCustomTemplate = false
                                previewRefreshKey++
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = neonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Template Standar (Thermal)",
                                color = textMain,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Format struk kasir/thermal digital otomatis",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: Custom Template Upload
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (useCustomTemplate) neonCyan.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (useCustomTemplate) neonCyan else cardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                useCustomTemplate = true
                                previewRefreshKey++
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = useCustomTemplate,
                            onClick = {
                                useCustomTemplate = true
                                previewRefreshKey++
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = neonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Template Kustom Sendiri",
                                    color = textMain,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = neonCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "UPLOAD PNG",
                                        color = neonCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Upload gambar kop surat/desain invoice sendiri",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (useCustomTemplate) {
                // Custom Template Upload Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Upload File Template (.png / .jpg)",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Gambar ini akan digunakan sebagai background/desain invoice saat dikirim via WhatsApp (.png)",
                            color = textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val templateFile = customTemplatePath?.let { File(it) }
                        val fileExists = templateFile != null && templateFile.exists()

                        if (fileExists) {
                            // File is uploaded
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .border(1.dp, neonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = remember(templateFile, previewRefreshKey) {
                                    try {
                                        BitmapFactory.decodeFile(templateFile!!.absolutePath)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Custom Template Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text("Gagal memuat pratinjau gambar", color = textSecondary, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ganti File", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            templateFile?.delete()
                                        } catch (_: Exception) {}
                                        customTemplatePath = null
                                        SettingsManager.customInvoiceTemplatePath = null
                                        useCustomTemplate = false
                                        SettingsManager.useCustomInvoiceTemplate = false
                                        previewRefreshKey++
                                        Toast.makeText(context, "Template kustom dihapus", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hapus", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Switch to overlay invoice data automatically
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Tuliskan Data Tagihan di Atas Gambar",
                                        color = textMain,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Otomatis menambahkan nama, paket, rincian bulan, total & status pada gambar template",
                                        color = textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = overlayData,
                                    onCheckedChange = {
                                        overlayData = it
                                        previewRefreshKey++
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = neonCyan
                                    )
                                )
                            }
                        } else {
                            // No file uploaded yet, show upload banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(neonCyan.copy(alpha = 0.05f))
                                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Upload Template",
                                        tint = neonCyan,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Ketuk untuk Upload Template Invoice (.png / .jpg)",
                                        color = textMain,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Mendukung format gambar PNG atau JPG dari penyimpanan perangkat",
                                        color = textSecondary,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Standard Thermal Settings (Header and Footer)
                Text("Header Invoice (Thermal)", color = textSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = headerText,
                    onValueChange = { headerText = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan,
                        unfocusedBorderColor = textSecondary,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    ),
                    placeholder = { Text("Masukkan text header invoice...", color = textSecondary) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Footer Invoice (Thermal)", color = textSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = footerText,
                    onValueChange = { footerText = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan,
                        unfocusedBorderColor = textSecondary,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    ),
                    placeholder = { Text("Masukkan text footer invoice...", color = textSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pratinjau Invoice Saat Dikirim WA",
                    color = textMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = neonCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "FORMAT .PNG",
                        color = neonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val currentMonthName = remember {
                SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Generate and display bitmap preview matching current selection
                val previewBitmap = remember(useCustomTemplate, customTemplatePath, overlayData, headerText, footerText, previewRefreshKey) {
                    try {
                        // Temporarily mock settings for preview rendering
                        SettingsManager.invoiceHeader = headerText
                        SettingsManager.invoiceFooterText = footerText
                        SettingsManager.useCustomInvoiceTemplate = useCustomTemplate
                        SettingsManager.customInvoiceOverlayData = overlayData
                        InvoiceGenerator.generateInvoiceBitmap(
                            context = context,
                            customerName = "Budi Santoso",
                            customerPhone = "081234567890",
                            customerArea = "Area Timur",
                            packageName = "Paket Family 20M",
                            months = currentMonthName,
                            totalAmount = "Rp 150.000",
                            status = "BELUM BAYAR",
                            invoiceNo = "INV-SAMPLE"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Invoice Preview",
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .heightIn(max = 480.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    com.example.ui.components.ThermalInvoiceView(
                        headerText = headerText,
                        footerText = footerText,
                        customer = null,
                        months = currentMonthName,
                        totalAmount = "Rp 150.000"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    SettingsManager.invoiceHeader = headerText
                    SettingsManager.invoiceFooterText = footerText
                    SettingsManager.useCustomInvoiceTemplate = useCustomTemplate
                    SettingsManager.customInvoiceTemplatePath = customTemplatePath
                    SettingsManager.customInvoiceOverlayData = overlayData
                    Toast.makeText(context, "Pengaturan invoice berhasil disimpan", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIMPAN PENGATURAN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
