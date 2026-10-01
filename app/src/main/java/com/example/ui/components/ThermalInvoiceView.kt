package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.Customer
import com.example.ui.util.InvoiceGenerator
import java.text.SimpleDateFormat
import java.util.*

/**
 * Invoice View default yang menggunakan Faktur Resmi (menggantikan invoice model awal / thermal).
 * Mendukung status dinamis (BELUM BAYAR atau LUNAS).
 */
@Composable
fun ThermalInvoiceView(
    headerText: String,
    footerText: String,
    customer: Customer?,
    months: String,
    totalAmount: String,
    status: String = "LUNAS",
    modifier: Modifier = Modifier,
    useOfficialInvoiceAsDefault: Boolean = true
) {
    val context = LocalContext.current
    val isLunas = status.equals("LUNAS", ignoreCase = true)
    val currentStatus = if (isLunas) "LUNAS" else "BELUM BAYAR"

    if (useOfficialInvoiceAsDefault) {
        val isDedicatedPkg = customer?.packageName?.contains("dedicated", ignoreCase = true) == true ||
                customer?.packageName?.contains("1:1") == true

        var invoiceBitmap by remember(customer, months, totalAmount, currentStatus) {
            mutableStateOf<Bitmap?>(null)
        }

        LaunchedEffect(customer, months, totalAmount, currentStatus) {
            try {
                val effectiveTotal = customer?.getFormattedTotalBill() ?: totalAmount
                invoiceBitmap = InvoiceGenerator.generateInvoiceBitmap(
                    context = context,
                    customerName = customer?.name ?: "-",
                    customerPhone = customer?.phone ?: "-",
                    customerArea = customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-",
                    packageName = customer?.packageName,
                    months = months,
                    totalAmount = effectiveTotal,
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
            } catch (_: Exception) {
                invoiceBitmap = null
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (invoiceBitmap != null) {
                Image(
                    bitmap = invoiceBitmap!!.asImageBitmap(),
                    contentDescription = "Faktur Invoice $currentStatus Resmi",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(200f / 140f),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0066FF))
                }
            }
        }
    } else {
        // Fallback Monospace Format with Dynamic Status
        val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID")).format(Date())
        val currencyFmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
        val add1 = customer?.getAdditionalCost1Amount() ?: 0L
        val add2 = customer?.getAdditionalCost2Amount() ?: 0L
        val disc = customer?.getDiscountAmount() ?: 0L
        val effectiveTotal = customer?.getFormattedTotalBill() ?: totalAmount

        Column(
            modifier = modifier
                .width(300.dp)
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = headerText,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            ThermalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            ThermalRow("Tanggal", currentDate)
            ThermalRow("Admin", "Akbar Media")
            ThermalRow("Pelanggan", customer?.name ?: "-")
            ThermalRow("Area/Alamat", customer?.let { if (it.getEffectiveAddress().isNotBlank()) "${it.area} / ${it.getEffectiveAddress()}" else it.area } ?: "-")
            ThermalRow("Bulan", months)
            
            Spacer(modifier = Modifier.height(8.dp))
            ThermalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Rincian Tagihan",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThermalRow("Iuran Internet", customer?.price ?: totalAmount)
            if (add1 > 0L) {
                val label1 = if (!customer?.additionalCostDesc1.isNullOrBlank()) "Biaya Tambahan (${customer.additionalCostDesc1})" else "Biaya Tambahan 1"
                ThermalRow(label1, "Rp. ${currencyFmt.format(add1)}")
            }
            if (add2 > 0L) {
                val label2 = if (!customer?.additionalCostDesc2.isNullOrBlank()) "Biaya Tambahan (${customer.additionalCostDesc2})" else "Biaya Tambahan 2"
                ThermalRow(label2, "Rp. ${currencyFmt.format(add2)}")
            }
            if (disc > 0L) {
                ThermalRow("Diskon", "- Rp. ${currencyFmt.format(disc)}")
            }
            if (add1 == 0L && add2 == 0L && disc == 0L) {
                ThermalRow("Biaya Tambahan", "Rp. 0")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            ThermalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            ThermalRow("TOTAL", effectiveTotal, isBold = true)
            ThermalRow("STATUS", currentStatus, isBold = true)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = footerText,
                color = Color.Black,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ThermalRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label, 
            color = Color.Black, 
            fontSize = 12.sp, 
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value, 
            color = Color.Black, 
            fontSize = 12.sp, 
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThermalDivider() {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color.Black,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
