package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.Customer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ThermalInvoiceView(
    headerText: String,
    footerText: String,
    customer: Customer?,
    months: String,
    totalAmount: String,
    modifier: Modifier = Modifier
) {
    val currentDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("id", "ID")).format(Date())
    
    Column(
        modifier = modifier
            .width(300.dp) // Fixed width to simulate thermal printer
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
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
        
        // Info
        ThermalRow("Tanggal", currentDate)
        ThermalRow("Admin", "Akbar Media") // Or pass companyName
        ThermalRow("Pelanggan", customer?.name ?: "-")
        ThermalRow("Area/Alamat", customer?.area ?: "-")
        ThermalRow("Bulan", months)
        
        Spacer(modifier = Modifier.height(8.dp))
        ThermalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        
        // Rincian
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
        ThermalRow("Iuran Reguler", totalAmount)
        ThermalRow("Biaya Tambahan", "Rp. 0")
        ThermalRow("Diskon", "Rp. 0")
        ThermalRow("PPN", "Rp. 0")
        
        Spacer(modifier = Modifier.height(8.dp))
        ThermalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        
        ThermalRow("TOTAL", totalAmount, isBold = true)
        ThermalRow("STATUS", "LUNAS", isBold = true)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer
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
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
