package com.example.ui.screens
import androidx.compose.ui.graphics.PathEffect




import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import android.provider.MediaStore
import android.content.ContentValues
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.ui.data.remote.ApiClient
import com.example.ui.screens.Customer
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentSuccessScreen(customerId: String, totalAmount: String, months: String, onFinish: () -> Unit) {
    val context = LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
        } catch(e: Exception) {
            Toast.makeText(context, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    val bgMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF0A0A0A) else androidx.compose.ui.graphics.Color(0xFFF4F7FA)
    val headerBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF1F0216) else androidx.compose.ui.graphics.Color(0xFFFFEBF5)
    val textMain = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val textSecondary = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)
    val cardBg = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val successGreen = Color(0xFF00FF00)
    val neonCyan = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)

    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
    val formattedAmount = "Rp. ${formatter.format(amountDouble)}"
    
    val sdf = SimpleDateFormat("dd MMM yyyy", java.util.Locale.forLanguageTag("id-ID"))
    val currentDate = sdf.format(Date())
    
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgMain)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Header
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(successGreen.copy(alpha = 0.2f))
                .border(2.dp, successGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = "Success", tint = successGreen, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Success", color = successGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Pembayaran Telah Tersimpan", color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFF1A1A1A), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Thermal Receipt Paper
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }, 
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.components.ThermalInvoiceView(
                    headerText = com.example.ui.data.SettingsManager.invoiceHeader,
                    footerText = com.example.ui.data.SettingsManager.invoiceFooterText,
                    customer = customer,
                    months = months,
                    totalAmount = formattedAmount
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionIconBtn(Icons.Default.Print, "Cetak", cardBg, neonCyan) {
                Toast.makeText(context, "Fitur cetak belum tersedia", Toast.LENGTH_SHORT).show()
            }
            ActionIconBtn(Icons.AutoMirrored.Filled.Message, "Kirim WA", cardBg, neonCyan) {
                val phone = customer?.phone
                if (!phone.isNullOrBlank()) {
                    try {
                        var formattedPhone = phone
                        if (formattedPhone.startsWith("0")) {
                            formattedPhone = "62" + formattedPhone.substring(1)
                        }
                        val rawTemplate = if (com.example.ui.data.SettingsManager.waGatewayEnabled && com.example.ui.data.SettingsManager.waNotifyPaymentSuccess) {
                            com.example.ui.data.SettingsManager.waTemplatePaymentSuccess
                        } else {
                            "Halo {nama},\nTerima kasih, pembayaran tagihan internet untuk bulan {bulan} sejumlah {nominal} telah kami terima dan lunas.\n\nSalam,\n{perusahaan}"
                        }
                        val text = rawTemplate
                            .replace("{nama}", customer?.name ?: "")
                            .replace("{bulan}", months)
                            .replace("{nominal}", formattedAmount)
                            .replace("{perusahaan}", com.example.ui.data.SettingsManager.companyName)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(text)}")
                        context.startActivity(intent)
                    } catch(e: Exception) {
                        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Nomor pelanggan tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {
                coroutineScope.launch {
                    try {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "Invoice_${customer?.name}_$months.jpg")
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        }
                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Invoice")
                            context.startActivity(shareIntent)
                        } else {
                            Toast.makeText(context, "Gagal menyiapkan gambar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal membagikan gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SELESAI", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            value, 
            color = valueColor, 
            fontSize = 12.sp, 
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DividerLine() {
    val color = if (androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF333333) else Color(0xFFCCCCCC)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

@Composable
fun ActionIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, contentColor: Color, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {
        Icon(icon, contentDescription = label, tint = contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = contentColor, fontSize = 10.sp)
    }
}
