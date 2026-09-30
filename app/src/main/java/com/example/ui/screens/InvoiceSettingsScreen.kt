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

    var companyName by remember { mutableStateOf(SettingsManager.companyName) }
    var companySlogan by remember { mutableStateOf(SettingsManager.companySlogan) }
    var companyAddress by remember { mutableStateOf(SettingsManager.companyAddress) }
    var companyContact by remember { mutableStateOf(SettingsManager.companyContact) }
    var footerText by remember { mutableStateOf(SettingsManager.invoiceFooterText) }
    var logoPath by remember { mutableStateOf(SettingsManager.invoiceLogoPath) }

    // Toggle for live preview between Reguler & Dedicated package types
    var previewPackageType by remember { mutableStateOf("Reguler") }
    var previewStatus by remember { mutableStateOf("BELUM BAYAR") }
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
                            "Kop Berlogo & Identitas Perusahaan",
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
            Spacer(modifier = Modifier.height(8.dp))

            // Default Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F1E36) else Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF1E3A8A) else Color(0xFF93C5FD))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = if (isDark) neonCyan else Color(0xFF1D4ED8),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Model Invoice: Faktur Resmi (Default)",
                            color = textMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "Format faktur resmi dengan kop logo aktif sebagai default menggantikan struk model awal/thermal untuk seluruh tagihan (Belum Bayar & Lunas).",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 1: Upload Logo Kop Surat
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Logo Kop Surat",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Upload logo perusahaan untuk ditampilkan di sisi kiri kop invoice dengan alignment presisi sejajar teks identitas.",
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
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoBitmap != null) {
                                    Image(
                                        bitmap = logoBitmap.asImageBitmap(),
                                        contentDescription = "Logo Kop",
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
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
                                    "Siap dicetak pada kop surat faktur",
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
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Logo Kop",
                                    tint = neonCyan,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Upload Logo Perusahaan (.png / .jpg)",
                                    color = textMain,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Akan disejajarkan di sisi kiri kop surat faktur",
                                    color = textSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: Kustomisasi Identitas Perusahaan
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Kustomisasi Kop & Identitas Perusahaan",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Informasi ini akan tercetak rapi berdampingan dengan logo pada kop surat atas faktur.",
                        color = textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Nama Perusahaan
                    Text("Nama Perusahaan", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = {
                            companyName = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = neonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: Akbar Media", color = textSecondary) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Slogan Perusahaan
                    Text("Slogan Perusahaan", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = companySlogan,
                        onValueChange = {
                            companySlogan = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Stars, contentDescription = null, tint = neonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: Penyedia Layanan Internet Broadband & RT/RW Net Berkualitas", color = textSecondary) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Alamat Perusahaan
                    Text("Alamat Perusahaan", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = companyAddress,
                        onValueChange = {
                            companyAddress = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = neonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: Jln. Raya Akbar Media, Indonesia", color = textSecondary) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Kontak Perusahaan
                    Text("Kontak Perusahaan (Telepon / WhatsApp / Email)", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = companyContact,
                        onValueChange = {
                            companyContact = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = neonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: WhatsApp: 0812-3456-7890 • Email: cs@akbarmedia.my.id", color = textSecondary) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Catatan Pembayaran / Footer
                    Text("Catatan Pembayaran / Footer", color = textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = footerText,
                        onValueChange = {
                            footerText = it
                            previewRefreshKey++
                        },
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = neonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        ),
                        placeholder = { Text("Contoh: Pembayaran melalui transfer bank atau cash...", color = textSecondary) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 3: Pratinjau Invoice Live
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
                        Column {
                            Text(
                                "Pratinjau Faktur Invoice",
                                color = textMain,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Live preview kop & form jenis layanan",
                                color = textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            color = neonCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Live Preview",
                                color = neonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Button to test both "Reguler" and "Dedicated"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1F1F1F) else Color(0xFFF1F5F9))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Option 1: Paket Reguler
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    previewPackageType = "Reguler"
                                    previewRefreshKey++
                                },
                            color = if (previewPackageType == "Reguler") neonCyan else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (previewPackageType == "Reguler") Color.Black else textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Paket Reguler",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewPackageType == "Reguler") Color.Black else textSecondary
                                )
                            }
                        }

                        // Option 2: Paket Dedicated
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    previewPackageType = "Dedicated"
                                    previewRefreshKey++
                                },
                            color = if (previewPackageType == "Dedicated") Color(0xFFFF9800) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (previewPackageType == "Dedicated") Color.Black else textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Paket Dedicated",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewPackageType == "Dedicated") Color.Black else textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Segmented Button to test status "BELUM BAYAR" and "LUNAS"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1F1F1F) else Color(0xFFF1F5F9))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Option 1: Status Belum Bayar
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    previewStatus = "BELUM BAYAR"
                                    previewRefreshKey++
                                },
                            color = if (previewStatus == "BELUM BAYAR") Color(0xFFDC2626) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (previewStatus == "BELUM BAYAR") Color.White else textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Status: Belum Bayar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewStatus == "BELUM BAYAR") Color.White else textSecondary
                                )
                            }
                        }

                        // Option 2: Status Lunas
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    previewStatus = "LUNAS"
                                    previewRefreshKey++
                                },
                            color = if (previewStatus == "LUNAS") Color(0xFF059669) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (previewStatus == "LUNAS") Color.White else textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Status: Lunas",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (previewStatus == "LUNAS") Color.White else textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val currentMonthName = remember {
                        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
                    }

                    // Generate preview bitmap
                    val isDedicatedPreview = previewPackageType == "Dedicated"
                    val samplePkgName = if (isDedicatedPreview) "Dedicated 1:1 - 20 Mbps" else "Paket Family 20 Mbps"
                    val sampleAmount = if (isDedicatedPreview) "Rp 3.000.000" else "Rp 150.000"
                    val sampleCustName = if (isDedicatedPreview) "PT. Maju Bersama" else "Budi Santoso"
                    val sampleCustArea = if (isDedicatedPreview) "Kawasan Industri" else "Area Timur"

                    val previewBitmap = remember(
                        logoPath,
                        companyName,
                        companySlogan,
                        companyAddress,
                        companyContact,
                        footerText,
                        previewPackageType,
                        previewStatus,
                        previewRefreshKey
                    ) {
                        try {
                            SettingsManager.companyName = companyName
                            SettingsManager.companySlogan = companySlogan
                            SettingsManager.companyAddress = companyAddress
                            SettingsManager.companyContact = companyContact
                            SettingsManager.invoiceHeader = "$companyName\n$companySlogan\n$companyAddress\n$companyContact"
                            SettingsManager.invoiceFooterText = footerText
                            SettingsManager.invoiceLogoPath = logoPath

                            InvoiceGenerator.generateInvoiceBitmap(
                                context = context,
                                customerName = sampleCustName,
                                customerPhone = "0812-3456-7890",
                                customerArea = sampleCustArea,
                                packageName = samplePkgName,
                                months = currentMonthName,
                                totalAmount = sampleAmount,
                                status = previewStatus,
                                invoiceNo = "INV-SAMPLE",
                                isDedicated = isDedicatedPreview,
                                serviceType = if (isDedicatedPreview) "Dedicated" else "Reguler"
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (previewBitmap != null) {
                            Image(
                                bitmap = previewBitmap.asImageBitmap(),
                                contentDescription = "Invoice Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(200f / 140f),
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    SettingsManager.companyName = companyName
                    SettingsManager.companySlogan = companySlogan
                    SettingsManager.companyAddress = companyAddress
                    SettingsManager.companyContact = companyContact
                    SettingsManager.invoiceHeader = "$companyName\n$companySlogan\n$companyAddress\n$companyContact"
                    SettingsManager.invoiceFooterText = footerText
                    SettingsManager.invoiceLogoPath = logoPath
                    SettingsManager.useInvoiceLogo = true
                    Toast.makeText(context, "Pengaturan invoice berhasil disimpan", Toast.LENGTH_SHORT).show()
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
