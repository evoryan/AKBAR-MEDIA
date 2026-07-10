package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentSuccessScreen(customerId: String, totalAmount: String, months: String, onFinish: () -> Unit) {
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF00FFFF).copy(alpha = 0.3f)
    val successGreen = Color(0xFF00FF00)
    val neonCyan = Color(0xFF00FFFF)

    val formatter = NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val amountDouble = totalAmount.toDoubleOrNull() ?: 0.0
    val formattedAmount = "Rp. ${formatter.format(amountDouble)}"
    
    val sdf = SimpleDateFormat("dd MMM yyyy", java.util.Locale.forLanguageTag("id-ID"))
    val currentDate = sdf.format(Date())

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
        Text("Pembayaran Telah Tersimpan", color = textMain, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Thermal Receipt Paper
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Logo placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AMG", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("PT.Akbar Media Group", color = neonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Total Pembayaran", color = textSecondary, fontSize = 12.sp)
                    Text("Reguler", color = neonCyan, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(formattedAmount, color = textMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Rincian", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            ReceiptRow("Nama Kepada", "Customer $customerId", neonCyan)
            DividerLine()
            ReceiptRow("Alamat", "Pakisan", textMain)
            DividerLine()
            ReceiptRow("Area", "Pakisan", textMain)
            DividerLine()
            ReceiptRow("Iuran", formattedAmount, textMain, isBold = true)
            DividerLine()
            ReceiptRow("Biaya Tambahan", "- Rp. 0\n- Rp. 0", textMain)
            DividerLine()
            ReceiptRow("PPN 0%", "Rp. 0", textMain)
            DividerLine()
            ReceiptRow("Diskon", "Rp. 0", textMain)
            DividerLine()
            ReceiptRow("Bulan", months, textMain)
            DividerLine()
            ReceiptRow("Tanggal", currentDate, textMain)
            DividerLine()
            ReceiptRow("Keterangan", "L U N A S", successGreen, isBold = true)
            DividerLine()
            ReceiptRow("Admin By", "PT.Akbar Media Group", textMain)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Support By:\nToko Ana, PT.Telkom, PT.Citra Selaras Terabit,",
                color = textSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionIconBtn(Icons.Default.Print, "Cetak", cardBg, neonCyan)
            ActionIconBtn(Icons.Default.Message, "Kirim WA", cardBg, neonCyan)
            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan)
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
        Text(label, color = Color(0xFFAAAAAA), fontSize = 12.sp, modifier = Modifier.weight(1f))
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
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = Color(0xFF333333)
    )
}

@Composable
fun ActionIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, contentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {
        Icon(icon, contentDescription = label, tint = contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = contentColor, fontSize = 10.sp)
    }
}
