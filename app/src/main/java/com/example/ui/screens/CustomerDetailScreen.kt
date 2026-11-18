package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.OdpItem
import com.example.ui.data.remote.PaymentRequest
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import android.provider.MediaStore
import android.content.ContentValues
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import coil.compose.AsyncImage
import com.example.ui.util.InvoiceGenerator
import com.example.ui.data.SettingsManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class CustomerInvoicePreview(
    val title: String,
    val months: String,
    val amount: String,
    val status: String // "LUNAS" or "BELUM BAYAR"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    onBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    onNavigateToAcs: (String) -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF1F5F9)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFE2E8F0)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF0F172A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF475569)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF00FFFF).copy(alpha = 0.3f) else Color(0xFFCBD5E1)
    val primaryBlue = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0F0F2A) else androidx.compose.ui.graphics.Color(0xFFE0EDFF)
    val lightBlue = Color(0xFF00FFFF)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val neonPink = Color(0xFFFF00FF)
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var customer by remember { mutableStateOf<Customer?>(null) }
    var odpItem by remember { mutableStateOf<OdpItem?>(null) }
    var mikrotikStatus by remember { mutableStateOf<String>("-") }
    var mikrotikUptime by remember { mutableStateOf<String>("") }
    var acsDevice by remember { mutableStateOf<com.example.ui.screens.AcsDevice?>(null) }
    var isAcsLoading by remember { mutableStateOf(false) }
    var isAcsExpanded by remember { mutableStateOf(false) }
    var showAcsSsidDialog by remember { mutableStateOf(false) }
    var showAcsPasswordDialog by remember { mutableStateOf(false) }
    var showAcsRebootDialog by remember { mutableStateOf(false) }
    var isAcsActionLoading by remember { mutableStateOf(false) }
    var acsActionResult by remember { mutableStateOf<String?>(null) }
    var isMikrotikLoading by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    
    var paymentHistory by remember { mutableStateOf<List<com.example.ui.data.remote.PaymentHistory>>(emptyList()) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var isPaying by remember { mutableStateOf(false) }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val graphicsLayer = rememberGraphicsLayer()
    var activeInvoicePreview by remember { mutableStateOf<CustomerInvoicePreview?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var showImagePickerDialog by remember { mutableStateOf(false) }
    var isUploadingHeader by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun uploadHeaderBg(uri: Uri) {
        coroutineScope.launch {
            try {
                isUploadingHeader = true
                val bytes = com.example.ui.util.ImageCompressor.compressImageUri(context, uri)
                if (bytes != null) {
                    val mimeType = "image/jpeg"
                    val reqFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("image", "header_bg_${System.currentTimeMillis()}.jpg", reqFile)
                    val res = ApiClient.apiService.uploadCustomerHeaderBg(customerId, part)
                    val newHeaderUrl = res.header_bg_url ?: res.url
                    if (!newHeaderUrl.isNullOrBlank()) {
                        customer = customer?.copy(headerBgUrl = newHeaderUrl)
                        Toast.makeText(context, "Background header berhasil diubah!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal mengunggah background: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isUploadingHeader = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadHeaderBg(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            uploadHeaderBg(tempCameraUri!!)
        }
    }

    fun launchCamera() {
        try {
            val cameraFile = File(context.cacheDir, "camera_header_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cameraFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(customerId, refreshTrigger) {
        isBackgroundLoading = true
        try {
            val localCust = com.example.ui.data.local.AppDatabase.getDatabase(context).pelangganDao().getPelangganById(customerId.toIntOrNull() ?: 0)
            if (localCust != null) {
                customer = localCust.toCustomer()
            }
            val custs = ApiClient.apiService.getCustomers()
            val c = custs.find { it.id == customerId }
            if (c != null) {
                customer = c
            }
            isBackgroundLoading = false
            
            val activeCust = customer
            if (activeCust != null) {
                // Fetch payment history in background
                launch {
                    try {
                        paymentHistory = ApiClient.apiService.getCustomerHistory(activeCust.id)
                    } catch(e: Exception) {}
                }

                // Get ODP if exist in background
                launch {
                    try {
                        if (!activeCust.odpId.isNullOrEmpty()) {
                            val odps = ApiClient.apiService.getOdpList()
                            odpItem = odps.find { it.id.toString() == activeCust.odpId }
                        }
                    } catch(e: Exception) {}
                }
                
                // Get ACS Device in background
                launch {
                    val hasPppoeOrSn = !activeCust.pppoeSecret.isNullOrBlank() ||
                        activeCust.username.isNotBlank() ||
                        !activeCust.getEffectiveSnModem().isNullOrBlank()
                    if (hasPppoeOrSn) {
                        isAcsLoading = true
                        try {
                            val acsList = ApiClient.apiService.getAcsDevices()
                            acsDevice = findMatchingAcsDeviceForCustomer(acsList, activeCust)
                        } catch (_: Exception) {
                        } finally {
                            isAcsLoading = false
                        }
                    } else {
                        acsDevice = null
                    }
                }
            }
        } catch (e: Exception) {
            isBackgroundLoading = false
            if (customer == null) {
                Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun refreshCustomerAcs(summonFirst: Boolean = false) {
        val currentCust = customer ?: return
        val currentAcs = acsDevice
        coroutineScope.launch {
            isAcsLoading = true
            try {
                if (summonFirst && currentAcs?.id != null && !currentAcs.id.startsWith("error_")) {
                    ApiClient.apiService.acsAction(
                        currentAcs.id,
                        mapOf("action" to "summon", "areaName" to (currentAcs.areaName ?: currentCust.area))
                    )
                    Toast.makeText(context, "Perintah summon berhasil dikirim ke ONT", Toast.LENGTH_SHORT).show()
                }
                val acsList = ApiClient.apiService.getAcsDevices()
                acsDevice = findMatchingAcsDeviceForCustomer(acsList, currentCust)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memperbarui data GenieACS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isAcsLoading = false
            }
        }
    }

    LaunchedEffect(customer) {
        val c = customer
        if (c != null && !c.pppoeSecret.isNullOrEmpty() && c.area.isNotEmpty()) {
            isMikrotikLoading = true
            try {
                val areas = ApiClient.apiService.getAreas()
                val area = areas.find { it.name == c.area }
                if (area != null) {
                    val secrets = ApiClient.apiService.getMikrotikSecrets(area.id)
                    val sec = secrets.find { it.name == c.pppoeSecret }
                    if (sec != null) {
                        mikrotikStatus = sec.status
                        mikrotikUptime = sec.uptime
                    }
                }
            } catch (e: Exception) {
                mikrotikStatus = "Error"
            } finally {
                isMikrotikLoading = false
            }
        }
    }

    if (showPaymentDialog && customer != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Konfirmasi Pembayaran") },
            text = { Text("Apakah Anda yakin ingin membayar tagihan untuk pelanggan ${customer?.name} sejumlah ${customer?.getFormattedTotalBill()}?") },
            confirmButton = {
                Button(
                    onClick = {
                        isPaying = true
                        coroutineScope.launch {
                            try {
                                val amount = customer?.getTotalBillAmount()?.toDouble() ?: 0.0
                                ApiClient.apiService.payBilling(PaymentRequest(customerId, "Admin", amount))
                                val custIdInt = customerId.toIntOrNull()
                                if (custIdInt != null) {
                                    com.example.ui.data.local.AppDatabase.getDatabase(context).pelangganDao().updateStatus(custIdInt, "LUNAS CASH")
                                }
                                Toast.makeText(context, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
                                showPaymentDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal melakukan pembayaran: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isPaying = false
                            }
                        }
                    },
                    enabled = !isPaying
                ) {
                    if (isPaying) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    else Text("Ya, Bayar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }, enabled = !isPaying) {
                    Text("Batal")
                }
            }
        )
    }

    if (activeInvoicePreview != null) {
        val previewData = activeInvoicePreview!!
        val isLunas = previewData.status.equals("LUNAS", ignoreCase = true)
        val isDedicatedPkg = customer?.packageName?.contains("dedicated", ignoreCase = true) == true ||
                customer?.packageName?.contains("1:1") == true

        val invoiceBitmap = remember(previewData, customer) {
            customer?.let { cust ->
                try {
                    InvoiceGenerator.generateInvoiceBitmap(
                        context = context,
                        customerName = cust.name,
                        customerPhone = cust.phone,
                        customerArea = if (cust.getEffectiveAddress().isNotBlank()) "${cust.area} / ${cust.getEffectiveAddress()}" else cust.area,
                        packageName = cust.packageName,
                        months = previewData.months,
                        totalAmount = previewData.amount,
                        status = previewData.status,
                        isDedicated = isDedicatedPkg,
                        serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                        packagePrice = cust.price,
                        additionalCost1 = cust.additionalCost1,
                        additionalCostDesc1 = cust.getEffectiveCostDesc1() ?: cust.additionalCostDesc1,
                        additionalCost2 = cust.additionalCost2,
                        additionalCostDesc2 = cust.getEffectiveCostDesc2() ?: cust.additionalCostDesc2,
                        discount = cust.discount
                    )
                } catch (_: Exception) {
                    null
                }
            }
        }

        AlertDialog(
            onDismissRequest = { activeInvoicePreview = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        previewData.title,
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Surface(
                        color = if (isLunas) Color(0xFF059669).copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            previewData.status,
                            color = if (isLunas) Color(0xFF059669) else Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (invoiceBitmap != null) {
                        Image(
                            bitmap = invoiceBitmap.asImageBitmap(),
                            contentDescription = "Faktur Invoice Resmi",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(200f / 140f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = neonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionItemCol(Icons.Default.Print, "Cetak", neonCyan, textSecondary) {
                            if (invoiceBitmap != null) {
                                try {
                                    val printHelper = PrintHelper(context).apply {
                                        scaleMode = PrintHelper.SCALE_MODE_FIT
                                    }
                                    printHelper.printBitmap("Invoice_${customer?.name ?: "Customer"}_${previewData.status}", invoiceBitmap)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal mencetak invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Invoice sedang dipersiapkan...", Toast.LENGTH_SHORT).show()
                            }
                        }

                        ActionItemCol(Icons.AutoMirrored.Filled.Message, "Kirim WA", neonCyan, textSecondary) {
                            val phone = customer?.phone
                            if (!phone.isNullOrBlank()) {
                                val message = if (isLunas) {
                                    val rawTemplate = if (SettingsManager.waGatewayEnabled && SettingsManager.waNotifyPaymentSuccess) {
                                        SettingsManager.waTemplatePaymentSuccess
                                    } else {
                                        "Halo {nama},\nTerima kasih, pembayaran tagihan internet untuk bulan {bulan} sejumlah {nominal} telah kami terima dan lunas.\n\nSalam,\n{perusahaan}"
                                    }
                                    rawTemplate
                                        .replace("{nama}", customer?.name ?: "")
                                        .replace("{bulan}", previewData.months)
                                        .replace("{nominal}", previewData.amount)
                                        .replace("{perusahaan}", SettingsManager.companyName)
                                } else {
                                    val rawTemplate = if (SettingsManager.waGatewayEnabled && SettingsManager.waNotifyNewBilling) {
                                        SettingsManager.waTemplateNewBilling
                                    } else {
                                        "Halo {nama},\nTagihan internet Anda untuk bulan {bulan} telah terbit sebesar {nominal}.\n\nMohon segera melakukan pembayaran. Terima kasih.\n\nSalam,\n{perusahaan}"
                                    }
                                    rawTemplate
                                        .replace("{nama}", customer?.name ?: "")
                                        .replace("{bulan}", previewData.months)
                                        .replace("{nominal}", previewData.amount)
                                        .replace("{perusahaan}", SettingsManager.companyName)
                                }

                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        val pngFile = InvoiceGenerator.generateInvoicePngFile(
                                            context = context,
                                            customerName = customer?.name ?: "-",
                                            customerPhone = phone,
                                            customerArea = customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-",
                                            packageName = customer?.packageName,
                                            months = previewData.months,
                                            totalAmount = previewData.amount,
                                            status = previewData.status,
                                            isDedicated = isDedicatedPkg,
                                            serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                                            packagePrice = customer?.price,
                                            additionalCost1 = customer?.additionalCost1,
                                            additionalCostDesc1 = customer?.getEffectiveCostDesc1() ?: customer?.additionalCostDesc1,
                                            additionalCost2 = customer?.additionalCost2,
                                            additionalCostDesc2 = customer?.getEffectiveCostDesc2() ?: customer?.additionalCostDesc2,
                                            discount = customer?.discount
                                        )
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            InvoiceGenerator.sendWhatsappInvoiceWithPng(
                                                context = context,
                                                phone = phone,
                                                message = message,
                                                pngFile = pngFile
                                            )
                                        }
                                    } catch (e: Exception) {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            Toast.makeText(context, "Gagal membuat invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Nomor pelanggan tidak tersedia", Toast.LENGTH_SHORT).show()
                            }
                        }

                        ActionItemCol(Icons.Default.Share, "Bagikan", neonCyan, textSecondary) {
                            val phone = customer?.phone ?: "628"
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val pngFile = InvoiceGenerator.generateInvoicePngFile(
                                        context = context,
                                        customerName = customer?.name ?: "-",
                                        customerPhone = phone,
                                        customerArea = customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-",
                                        packageName = customer?.packageName,
                                        months = previewData.months,
                                        totalAmount = previewData.amount,
                                        status = previewData.status,
                                        isDedicated = isDedicatedPkg,
                                        serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                                        packagePrice = customer?.price,
                                        additionalCost1 = customer?.additionalCost1,
                                        additionalCostDesc1 = customer?.getEffectiveCostDesc1() ?: customer?.additionalCostDesc1,
                                        additionalCost2 = customer?.additionalCost2,
                                        additionalCostDesc2 = customer?.getEffectiveCostDesc2() ?: customer?.additionalCostDesc2,
                                        discount = customer?.discount
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pngFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TEXT, "Faktur Tagihan (${previewData.status}) - ${customer?.name ?: ""} (${previewData.months})")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(shareIntent, "Bagikan Faktur Invoice")
                                    chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        context.startActivity(chooser)
                                    }
                                } catch (e: Exception) {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        Toast.makeText(context, "Gagal membagikan invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeInvoicePreview = null }) {
                    Text("Tutup", color = neonCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = customer?.name ?: "Detail Pelanggan",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(customerId) },
                        modifier = Modifier.testTag("btn_edit_customer_top")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Pelanggan", tint = neonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateToEdit(customerId) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_edit_customer_bottom"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, neonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = neonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("EDIT", fontWeight = FontWeight.Bold, color = neonCyan)
                }

                Button(
                    onClick = { onNavigateToPayment(customerId) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("btn_bayar_tagihan"),
                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BAYAR TAGIHAN", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgMain)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (isBackgroundLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonCyan,
                    trackColor = cardBorder
                )
            }

            if (!isBackgroundLoading && customer == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pelanggan tidak ditemukan", color = textMain, fontSize = 16.sp)
                }
            } else {
                // Top Section (Profile Header with custom background)
                val formattedHeaderBgUrl = remember(customer?.header_bg_url) {
                    formatInvoiceUrl(customer?.header_bg_url)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(headerBg)
                ) {
                    if (!formattedHeaderBgUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = formattedHeaderBgUrl,
                            contentDescription = "Header Background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(primaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBackgroundLoading) {
                                CircularProgressIndicator(
                                    color = neonCyan,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    customer?.name?.take(1)?.uppercase() ?: "P",
                                    color = neonCyan,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (isBackgroundLoading) "Memuat..." else (customer?.name ?: "Nama Pelanggan"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            if (isBackgroundLoading) "" else (customer?.phone ?: "-"),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onNavigateToEdit(customerId) },
                                modifier = Modifier.testTag("btn_edit_customer_header"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = neonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Pelanggan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neonCyan)
                            }

                            Button(
                                onClick = { showImagePickerDialog = true },
                                modifier = Modifier.testTag("btn_change_header_bg"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue.copy(alpha = 0.85f), contentColor = neonCyan),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = neonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ubah Header", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = neonCyan)
                            }
                        }
                    }

                    if (isUploadingHeader) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = neonCyan)
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = headerBg,
                    contentColor = textMain,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = neonCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Rincian", color = if (selectedTabIndex == 0) neonCyan else textSecondary) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Riwayat", color = if (selectedTabIndex == 1) neonCyan else textSecondary) }
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (selectedTabIndex == 0) {
                        
                        // Rincian Jaringan
                        CardSection(title = "Rincian Jaringan", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Mikrotik Secret", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Sumber: ${customer?.area}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                    Text("User: ${customer?.pppoeSecret?.takeIf { it.isNotBlank() } ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                }
                                if (isMikrotikLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = neonCyan,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (mikrotikStatus.equals("online", true)) Color(0xFF00FF00).copy(alpha = 0.2f) else if (mikrotikStatus.equals("disabled", true)) Color.Gray.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(mikrotikStatus.uppercase(), color = if (mikrotikStatus.equals("online", true)) Color.Green else if (mikrotikStatus.equals("disabled", true)) Color.LightGray else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("ODP", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Nama: ${odpItem?.name ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                    Text("Port: ${customer?.odpPort?.takeIf { it.isNotBlank() } ?: "-"}", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("SN Modem", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(customer?.getEffectiveSnModem() ?: "-", color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 12.dp))
                            
                            Button(
                                onClick = {
                                    val searchTarget = customer?.pppoeSecret?.takeIf { it.isNotBlank() }
                                        ?: acsDevice?.username?.takeIf { it.isNotBlank() && it != "Unknown" }
                                        ?: customer?.getEffectiveSnModem()?.takeIf { it.isNotBlank() }
                                        ?: customer?.username?.takeIf { it.isNotBlank() }
                                    if (!searchTarget.isNullOrBlank()) {
                                        onNavigateToAcs(searchTarget)
                                    } else {
                                        Toast.makeText(context, "Data ACS tidak ditemukan untuk akun PPPoE ini", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_cek_modem_acs"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = neonCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cek Modem (ACS)")
                            }
                        }

                        // Data Lanjutan GenieACS (TR-069 ONT)
                        val showAcsCard = isAcsLoading ||
                            acsDevice != null ||
                            !customer?.pppoeSecret.isNullOrBlank() ||
                            !customer?.getEffectiveSnModem().isNullOrBlank()
                        if (showAcsCard) {
                            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                            val telemetryBg = if (isDarkTheme) Color(0xFF0A0E17) else Color(0xFFF8FAFC)
                            val telemetryBorder = if (isDarkTheme) cardBorder.copy(alpha = 0.7f) else Color(0xFFE2E8F0)
                            val successGreen = if (isDarkTheme) Color(0xFF10B981) else Color(0xFF059669)
                            val warningYellow = if (isDarkTheme) Color(0xFFF59E0B) else Color(0xFFD97706)
                            val errorRed = if (isDarkTheme) Color(0xFFEF4444) else Color(0xFFDC2626)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                                    .testTag("card_genieacs_detail")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { isAcsExpanded = !isAcsExpanded }
                                            .padding(vertical = 2.dp)
                                            .testTag("btn_toggle_genieacs_detail"),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Router,
                                                contentDescription = "GenieACS",
                                                tint = neonCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Data Lanjutan GenieACS",
                                                    color = textMain,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                val collapsedSubtitle = when {
                                                    isAcsExpanded -> "Telemetri TR-069 ONT"
                                                    isAcsLoading && acsDevice == null -> "Memuat telemetri ONT..."
                                                    acsDevice != null -> {
                                                        val rx = acsDevice?.rxPower?.takeIf { it.isNotBlank() && it != "-" }
                                                        if (rx != null) "Rx $rx dBm • Ketuk untuk lihat" else "Ketuk untuk lihat detail"
                                                    }
                                                    else -> "Telemetri TR-069 ONT • Ketuk untuk lihat"
                                                }
                                                Text(
                                                    text = collapsedSubtitle,
                                                    color = textSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val activeAcs = acsDevice
                                            if (activeAcs != null) {
                                                val isOnline = activeAcs.isOnline == true
                                                Surface(
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isOnline) successGreen.copy(alpha = 0.15f) else errorRed.copy(alpha = 0.15f),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isOnline) successGreen.copy(alpha = 0.4f) else errorRed.copy(alpha = 0.4f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = if (isOnline) "ONLINE" else "OFFLINE",
                                                        color = if (isOnline) successGreen else errorRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { refreshCustomerAcs(summonFirst = acsDevice != null) },
                                                enabled = !isAcsLoading,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("btn_refresh_genieacs_detail")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Muat Ulang Data GenieACS",
                                                    tint = neonCyan,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = if (isAcsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isAcsExpanded) "Tutup Data GenieACS" else "Buka Data GenieACS",
                                                tint = textSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = isAcsExpanded) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                    if (isAcsLoading && acsDevice == null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = neonCyan,
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Mengambil data ONT dari GenieACS...",
                                                color = textSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else if (acsDevice == null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = telemetryBg,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, telemetryBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Perangkat ONT untuk akun PPPoE ini belum terdaftar atau tidak ditemukan di GenieACS.",
                                                    color = textSecondary,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(onClick = { refreshCustomerAcs(summonFirst = false) }) {
                                                    Text("Cek Lagi", color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    } else {
                                        val acs = acsDevice!!
                                        val ontSn = getOntSerialNumber(acs)
                                        val vendorModel = listOfNotNull(
                                            acs.vendor?.takeIf { it.isNotBlank() && it != "-" },
                                            acs.model?.takeIf { it.isNotBlank() && it != "-" }
                                        ).joinToString(" ").ifBlank { "-" }
                                        val hwSw = listOfNotNull(
                                            acs.hardwareVersion?.takeIf { it.isNotBlank() && it != "-" },
                                            acs.softwareVersion?.takeIf { it.isNotBlank() && it != "-" }
                                        ).joinToString(" / ").ifBlank { "-" }
                                        val tagArea = acs.tag?.takeIf { it.isNotBlank() && it != "-" }
                                            ?: acs.areaName?.takeIf { it.isNotBlank() }
                                            ?: "-"
                                        val rxVal = acs.rxPower ?: "-"
                                        val rxBgColor = getRxPowerColor(rxVal)

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(telemetryBg)
                                                .border(1.dp, telemetryBorder, RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            DetailRow("Akun PPPoE (ACS)", acs.username?.takeIf { it.isNotBlank() && it != "Unknown" } ?: "-")
                                            DetailRow("SN ONT", ontSn)
                                            DetailRow("Tag / Area ACS", tagArea)

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Redaman (Rx Power)", color = textSecondary, fontSize = 12.sp)
                                                if (rxVal != "-" && rxVal.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = rxBgColor
                                                    ) {
                                                        Text(
                                                            text = "$rxVal dBm",
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else {
                                                    Text("-", color = textMain, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }

                                            HorizontalDivider(color = telemetryBorder)

                                            DetailRow("SSID Wi-Fi", acs.ssid?.takeIf { it.isNotBlank() && it != "Unknown" } ?: "-")
                                            DetailRow("Password Wi-Fi", acs.wifiPassword?.takeIf { it.isNotBlank() } ?: "-")
                                            DetailRow("Perangkat Terhubung", "${acs.connectedUsers ?: 0} Perangkat")

                                            HorizontalDivider(color = telemetryBorder)

                                            DetailRow("Vendor / Model", vendorModel)
                                            DetailRow("Hardware / Software", hwSw)
                                            DetailRow("IP Address (WAN/CPE)", acs.ipAddress?.takeIf { it.isNotBlank() && it != "-" } ?: "-")
                                            DetailRow("Uptime Perangkat", formatCustomerAcsUptime(acs.uptime))
                                            DetailRow("Last Inform", formatCustomerAcsLastInform(acs.lastInform))
                                        }

                                        // Quick Remote Actions
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showAcsSsidDialog = true },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_cust_acs_ssid"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = warningYellow),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, warningYellow.copy(alpha = 0.6f)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ganti SSID", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                            OutlinedButton(
                                                onClick = { showAcsPasswordDialog = true },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_cust_acs_password"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan.copy(alpha = 0.6f)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ganti Sandi", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                            OutlinedButton(
                                                onClick = { showAcsRebootDialog = true },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_cust_acs_reboot"),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = errorRed),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, errorRed.copy(alpha = 0.6f)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reboot", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                        }
                                    }
                                }
                            }
                        }

                        // Rincian Data
                        CardSection(title = "Rincian Data", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            DetailRow("Nama", customer?.name ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("No Telepon", customer?.phone ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Area", customer?.area ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Alamat", customer?.getEffectiveAddress()?.takeIf { it.isNotBlank() } ?: customer?.address?.takeIf { it.isNotBlank() } ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("SN Modem", customer?.getEffectiveSnModem() ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Tgl Registrasi", customer?.registerDate?.takeIf { it.isNotBlank() } ?: "-")
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("Tgl Isolir", customer?.isolateDate?.takeIf { it.isNotBlank() } ?: "-")
                        }

                        // Rincian Biaya
                        CardSection(title = "Rincian Biaya", cardBg = cardBg, cardBorder = cardBorder, textMain = textMain) {
                            val currencyFmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
                            val add1 = customer?.getAdditionalCost1Amount() ?: 0L
                            val add2 = customer?.getAdditionalCost2Amount() ?: 0L

                            DetailRow("Paket", customer?.packageName?.takeIf { it.isNotBlank() } ?: "-")
                            DetailRow("Harga Paket", customer?.price ?: "-")
                            if (!customer?.discount.isNullOrEmpty() && customer?.discount != "0") {
                                DetailRow("Diskon", customer?.discount ?: "-")
                            }
                            val desc1 = customer?.getEffectiveCostDesc1() ?: customer?.additionalCostDesc1
                            if (add1 > 0L || !desc1.isNullOrBlank()) {
                                val label1 = if (!desc1.isNullOrBlank()) "Biaya Tambahan 1 ($desc1)" else "Biaya Tambahan 1"
                                DetailRow(label1, "Rp. ${currencyFmt.format(add1)}")
                            }
                            val desc2 = customer?.getEffectiveCostDesc2() ?: customer?.additionalCostDesc2
                            if (add2 > 0L || !desc2.isNullOrBlank()) {
                                val label2 = if (!desc2.isNullOrBlank()) "Biaya Tambahan 2 ($desc2)" else "Biaya Tambahan 2"
                                DetailRow(label2, "Rp. ${currencyFmt.format(add2)}")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Biaya Perbulan", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 14.sp)
                                Text(customer?.getFormattedTotalBill() ?: "Rp. 0", color = lightBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            val isCustomerPaid = customer?.status?.contains("belum", ignoreCase = true) != true &&
                                    customer?.status?.contains("isolir", ignoreCase = true) != true
                            val currentBillStatus = if (isCustomerPaid) "LUNAS" else "BELUM BAYAR"

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    activeInvoicePreview = CustomerInvoicePreview(
                                        title = if (isCustomerPaid) "Faktur Tagihan (Lunas)" else "Faktur Tagihan (Belum Bayar)",
                                        months = "Bulan Berjalan",
                                        amount = customer?.getFormattedTotalBill() ?: customer?.price ?: "Rp. 0",
                                        status = currentBillStatus
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isCustomerPaid) Color(0xFF00FF00) else neonCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCustomerPaid) Color(0xFF00FF00).copy(alpha = 0.5f) else neonCyan.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isCustomerPaid) "Lihat Faktur Tagihan (Lunas)" else "Lihat Faktur Tagihan (Belum Bayar)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                    } else if (selectedTabIndex == 1) {
                        PaymentHistorySection(
                            paymentHistory,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            neonCyan = neonCyan,
                            neonPink = neonPink,
                            onItemClick = { historyItem ->
                                val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
                                val amountDouble = historyItem.amount.toDoubleOrNull() ?: 0.0
                                val formattedAmount = "Rp. ${formatter.format(amountDouble)}"
                                activeInvoicePreview = CustomerInvoicePreview(
                                    title = "Faktur Pembayaran (Lunas)",
                                    months = historyItem.description,
                                    amount = formattedAmount,
                                    status = "LUNAS"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // DIALOG: Pick Image Source (Gallery / Camera)
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            containerColor = cardBg,
            title = { Text("Ubah Background Header", color = textMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih sumber gambar background header pelanggan:", color = textSecondary, fontSize = 13.sp)

                    OutlinedButton(
                        onClick = {
                            showImagePickerDialog = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih dari Galeri")
                    }

                    OutlinedButton(
                        onClick = {
                            showImagePickerDialog = false
                            launchCamera()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil dari Kamera")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImagePickerDialog = false }) {
                    Text("Batal", color = textSecondary)
                }
            }
        )
    }
    // DIALOG: Remote ACS Actions from Customer Detail
    val activeAcsForDialog = acsDevice
    if (showAcsSsidDialog && activeAcsForDialog != null) {
        AcsActionDialog(
            title = "Ganti Nama WiFi (SSID)",
            description = "Masukkan SSID WiFi baru untuk ONT ${activeAcsForDialog.username ?: getOntSerialNumber(activeAcsForDialog)}",
            initialValue = activeAcsForDialog.ssid?.takeIf { it != "Unknown" } ?: "",
            label = "SSID WiFi Baru",
            isUpdating = isAcsActionLoading,
            updateResult = acsActionResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = neonCyan,
            onDismiss = {
                showAcsSsidDialog = false
                acsActionResult = null
            },
            onConfirm = { newSsid ->
                if (newSsid.isBlank()) {
                    acsActionResult = "SSID tidak boleh kosong"
                } else {
                    isAcsActionLoading = true
                    acsActionResult = null
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.acsAction(
                                activeAcsForDialog.id ?: "",
                                mapOf("action" to "set_ssid", "value" to newSsid.trim(), "areaName" to (activeAcsForDialog.areaName ?: ""))
                            )
                            Toast.makeText(context, "SSID berhasil diubah ke $newSsid", Toast.LENGTH_LONG).show()
                            acsDevice = activeAcsForDialog.copy(ssid = newSsid.trim())
                            showAcsSsidDialog = false
                            acsActionResult = null
                        } catch (e: Exception) {
                            acsActionResult = "Gagal: ${e.message}"
                        } finally {
                            isAcsActionLoading = false
                        }
                    }
                }
            }
        )
    }

    if (showAcsPasswordDialog && activeAcsForDialog != null) {
        AcsActionDialog(
            title = "Ganti Password WiFi",
            description = "Masukkan password WiFi baru (minimal 8 karakter) untuk ${activeAcsForDialog.username ?: getOntSerialNumber(activeAcsForDialog)}",
            initialValue = "",
            label = "Password WiFi Baru",
            isUpdating = isAcsActionLoading,
            updateResult = acsActionResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = neonCyan,
            onDismiss = {
                showAcsPasswordDialog = false
                acsActionResult = null
            },
            onConfirm = { newPass ->
                if (newPass.length < 8) {
                    acsActionResult = "Password minimal harus 8 karakter"
                } else {
                    isAcsActionLoading = true
                    acsActionResult = null
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.acsAction(
                                activeAcsForDialog.id ?: "",
                                mapOf("action" to "set_password", "value" to newPass, "areaName" to (activeAcsForDialog.areaName ?: ""))
                            )
                            Toast.makeText(context, "Password WiFi berhasil diubah!", Toast.LENGTH_LONG).show()
                            acsDevice = activeAcsForDialog.copy(wifiPassword = newPass)
                            showAcsPasswordDialog = false
                            acsActionResult = null
                        } catch (e: Exception) {
                            acsActionResult = "Gagal: ${e.message}"
                        } finally {
                            isAcsActionLoading = false
                        }
                    }
                }
            }
        )
    }

    if (showAcsRebootDialog && activeAcsForDialog != null) {
        AcsActionDialog(
            title = "Reboot Perangkat ONT",
            description = "Kirim perintah reboot ke ONT ${activeAcsForDialog.username ?: getOntSerialNumber(activeAcsForDialog)}?",
            showInput = false,
            isUpdating = isAcsActionLoading,
            updateResult = acsActionResult,
            bgMain = cardBg,
            textMain = textMain,
            textSecondary = textSecondary,
            cardBg = cardBg,
            primaryCyan = neonCyan,
            onDismiss = {
                showAcsRebootDialog = false
                acsActionResult = null
            },
            onConfirm = {
                isAcsActionLoading = true
                acsActionResult = null
                coroutineScope.launch {
                    try {
                        ApiClient.apiService.acsAction(
                            activeAcsForDialog.id ?: "",
                            mapOf("action" to "reboot", "areaName" to (activeAcsForDialog.areaName ?: ""))
                        )
                        Toast.makeText(context, "Perintah reboot berhasil dikirim!", Toast.LENGTH_LONG).show()
                        showAcsRebootDialog = false
                        acsActionResult = null
                    } catch (e: Exception) {
                        acsActionResult = "Gagal: ${e.message}"
                    } finally {
                        isAcsActionLoading = false
                    }
                }
            }
        )
    }
}

private fun findMatchingAcsDeviceForCustomer(
    acsList: List<com.example.ui.screens.AcsDevice>,
    customer: Customer
): com.example.ui.screens.AcsDevice? {
    val validDevices = acsList.filter { it.id?.startsWith("error_") != true }
    val secret = customer.pppoeSecret?.trim()?.takeIf { it.isNotBlank() }
    val username = customer.username.trim().takeIf { it.isNotBlank() }
    val snModem = customer.getEffectiveSnModem()?.trim()?.takeIf { it.isNotBlank() }

    if (secret != null) {
        validDevices.find { (it.username ?: "").trim().equals(secret, ignoreCase = true) }?.let { return it }
    }
    if (username != null) {
        validDevices.find { (it.username ?: "").trim().equals(username, ignoreCase = true) }?.let { return it }
    }
    if (snModem != null) {
        validDevices.find {
            getOntSerialNumber(it).trim().equals(snModem, ignoreCase = true) ||
                (it.serialNumber ?: "").trim().equals(snModem, ignoreCase = true)
        }?.let { return it }
    }
    if (secret != null) {
        validDevices.find {
            val u = (it.username ?: "").trim()
            val id = (it.id ?: "").trim()
            (u.isNotBlank() && u != "Unknown" && u.contains(secret, ignoreCase = true)) ||
                (id.isNotBlank() && id.contains(secret, ignoreCase = true))
        }?.let { return it }
    }
    return null
}

private fun formatCustomerAcsUptime(rawUptime: String?): String {
    if (rawUptime.isNullOrBlank() || rawUptime == "-") return "-"
    val totalSeconds = rawUptime.trim().toLongOrNull() ?: return rawUptime
    if (totalSeconds <= 0L) return "0 detik"
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}h")
    if (hours > 0) parts.add("${hours}j")
    if (minutes > 0) parts.add("${minutes}m")
    if (parts.isEmpty()) parts.add("${seconds}d")
    return parts.joinToString(" ")
}

private fun formatCustomerAcsLastInform(isoString: String?): String {
    if (isoString.isNullOrBlank() || isoString == "-") return "-"
    return try {
        val instant = java.time.Instant.parse(isoString.trim())
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd MMM yyyy, HH:mm", java.util.Locale.forLanguageTag("id-ID"))
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        isoString.replace("T", " ").substringBefore(".").take(16)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
        Text(value, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentHistorySection(
    history: List<com.example.ui.data.remote.PaymentHistory>,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    neonPink: Color,
    onItemClick: (com.example.ui.data.remote.PaymentHistory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Riwayat Transaksi", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        if (history.isEmpty()) {
            Text("Tidak ada riwayat transaksi", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 14.sp)
        } else {
            history.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .clickable { onItemClick(item) }
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            val date = item.createdAt?.substringBefore("T") ?: ""
                            Text(date, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.description, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rp. ${item.amount}", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lunas", color = neonPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Cetak",
                                tint = neonCyan.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItemCol(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = textColor, fontSize = 11.sp)
    }
}

@Composable
fun CardSection(title: String, cardBg: Color, cardBorder: Color, textMain: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
