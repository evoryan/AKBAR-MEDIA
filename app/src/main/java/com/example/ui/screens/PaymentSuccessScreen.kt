package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import com.example.ui.data.SettingsManager
import com.example.ui.data.remote.ApiClient
import com.example.ui.util.InvoiceGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

@Composable
fun PaymentSuccessScreen(
    customerId: String,
    totalAmount: String,
    months: String,
    status: String = "LUNAS",
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isBackgroundLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
        } finally {
            isBackgroundLoading = false
        }
    }

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgMain = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4F7FA)
    val cardBg = if (isDark) Color(0xFF11111A) else Color.White
    val cardBorder = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0)
    val textMain = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
    val successGreen = Color(0xFF00FF00)
    val neonCyan = if (isDark) Color(0xFF00FFFF) else Color(0xFF0066FF)
    val isLunas = status.equals("LUNAS", ignoreCase = true)
    val currentStatus = if (isLunas) "LUNAS" else "BELUM BAYAR"
    val statusColor = if (isLunas) Color(0xFF059669) else Color(0xFFDC2626)

    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
    val formattedAmount = "Rp. ${formatter.format(amountDouble)}"

    val isDedicatedPkg = customer?.packageName?.contains("dedicated", ignoreCase = true) == true ||
            customer?.packageName?.contains("1:1") == true

    // Generate official invoice bitmap with dynamic status (BELUM BAYAR / LUNAS)
    val invoiceBitmap = remember(customer, months, formattedAmount, currentStatus) {
        customer?.let { cust ->
            try {
                InvoiceGenerator.generateInvoiceBitmap(
                    context = context,
                    customerName = cust.name,
                    customerPhone = cust.phone,
                    customerArea = cust.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area },
                    packageName = cust.packageName,
                    months = months,
                    totalAmount = formattedAmount,
                    status = currentStatus,
                    isDedicated = isDedicatedPkg,
                    serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                    packagePrice = cust.price,
                    additionalCost1 = cust.additionalCost1,
                    additionalCostDesc1 = cust.additionalCostDesc1,
                    additionalCost2 = cust.additionalCost2,
                    additionalCostDesc2 = cust.additionalCostDesc2,
                    discount = cust.discount
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgMain)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Status Indicator
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isLunas) successGreen.copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f))
                .border(2.dp, if (isLunas) successGreen else Color(0xFFDC2626), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isLunas) Icons.Default.Check else Icons.Default.Receipt,
                contentDescription = currentStatus,
                tint = if (isLunas) successGreen else Color(0xFFDC2626),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            if (isLunas) "Pembayaran Berhasil" else "Faktur Tagihan",
            color = if (isLunas) successGreen else (if (isDark) Color(0xFFFF4D4D) else Color(0xFFDC2626)),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (isLunas) "Faktur resmi tagihan lunas telah diterbitkan" else "Faktur resmi rincian tagihan belum lunas",
            color = textSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Default Official Invoice Container (Replacing thermal receipt)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isBackgroundLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        color = neonCyan,
                        trackColor = Color.Transparent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Faktur Resmi Tagihan",
                        color = textMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "STATUS: $currentStatus",
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (invoiceBitmap != null) {
                    Image(
                        bitmap = invoiceBitmap.asImageBitmap(),
                        contentDescription = "Faktur Invoice $currentStatus Resmi",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(200f / 140f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = neonCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions: Cetak, Kirim WA, Bagikan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cetak (Print Official Invoice)
            ActionIconBtn(Icons.Default.Print, "Cetak", cardBg, neonCyan) {
                if (invoiceBitmap != null) {
                    try {
                        val printHelper = PrintHelper(context).apply {
                            scaleMode = PrintHelper.SCALE_MODE_FIT
                        }
                        printHelper.printBitmap("Invoice_${customer?.name ?: "Customer"}_${currentStatus}_$months", invoiceBitmap)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal mencetak invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Invoice sedang dipersiapkan...", Toast.LENGTH_SHORT).show()
                }
            }

            // Kirim WA
            ActionIconBtn(Icons.AutoMirrored.Filled.Message, "Kirim WA", cardBg, neonCyan) {
                val phone = customer?.phone
                if (!phone.isNullOrBlank()) {
                    val rawTemplate = if (isLunas) {
                        if (SettingsManager.waGatewayEnabled && SettingsManager.waNotifyPaymentSuccess) {
                            SettingsManager.waTemplatePaymentSuccess
                        } else {
                            "Halo {nama},\nTerima kasih, pembayaran tagihan internet untuk bulan {bulan} sejumlah {nominal} telah kami terima dan lunas.\n\nSalam,\n{perusahaan}"
                        }
                    } else {
                        if (SettingsManager.waGatewayEnabled && SettingsManager.waNotifyNewBilling) {
                            SettingsManager.waTemplateNewBilling
                        } else {
                            "Halo {nama},\nTagihan internet Anda untuk bulan {bulan} telah terbit sebesar {nominal}.\n\nMohon segera melakukan pembayaran. Terima kasih.\n\nSalam,\n{perusahaan}"
                        }
                    }
                    val text = rawTemplate
                        .replace("{nama}", customer?.name ?: "")
                        .replace("{bulan}", months)
                        .replace("{nominal}", formattedAmount)
                        .replace("{perusahaan}", SettingsManager.companyName)

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val pngFile = InvoiceGenerator.generateInvoicePngFile(
                                context = context,
                                customerName = customer?.name ?: "-",
                                customerPhone = phone,
                                customerArea = customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-",
                                packageName = customer?.packageName,
                                months = months,
                                totalAmount = formattedAmount,
                                status = currentStatus,
                                isDedicated = isDedicatedPkg,
                                serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                                packagePrice = customer?.price,
                                additionalCost1 = customer?.additionalCost1,
                                additionalCostDesc1 = customer?.additionalCostDesc1,
                                additionalCost2 = customer?.additionalCost2,
                                additionalCostDesc2 = customer?.additionalCostDesc2,
                                discount = customer?.discount
                            )
                            withContext(Dispatchers.Main) {
                                InvoiceGenerator.sendWhatsappInvoiceWithPng(
                                    context = context,
                                    phone = phone,
                                    message = text,
                                    pngFile = pngFile
                                )
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Gagal membuat invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Nomor pelanggan tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }

            // Bagikan (Share Official Invoice PNG)
            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {
                val phone = customer?.phone ?: "628"
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        val pngFile = InvoiceGenerator.generateInvoicePngFile(
                            context = context,
                            customerName = customer?.name ?: "-",
                            customerPhone = phone,
                            customerArea = customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-",
                            packageName = customer?.packageName,
                            months = months,
                            totalAmount = formattedAmount,
                            status = currentStatus,
                            isDedicated = isDedicatedPkg,
                            serviceType = if (isDedicatedPkg) "Dedicated" else "Reguler",
                            packagePrice = customer?.price,
                            additionalCost1 = customer?.additionalCost1,
                            additionalCostDesc1 = customer?.additionalCostDesc1,
                            additionalCost2 = customer?.additionalCost2,
                            additionalCostDesc2 = customer?.additionalCostDesc2,
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
                            putExtra(Intent.EXTRA_TEXT, "Faktur Tagihan ($currentStatus) - ${customer?.name ?: ""} ($months)")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = Intent.createChooser(shareIntent, "Bagikan Faktur Tagihan")
                        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        withContext(Dispatchers.Main) {
                            context.startActivity(chooser)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Gagal membagikan invoice: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SELESAI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun ActionIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bg: Color,
    contentColor: Color,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 24.dp)
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
