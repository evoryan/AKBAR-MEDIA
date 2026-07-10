package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockBarangScreen(onBack: () -> Unit) {
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val cardBorder = Color(0xFF333333)

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBg,
                        contentColor = Color.Black
                    )
                ) {
                    Text("TRANSAKSI BARANG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Per-", color = textSecondary, fontSize = 16.sp)
                Text(
                    "STOCK BARANG",
                    color = primaryBg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text("-an", color = textSecondary, fontSize = 16.sp)
            }

            // Grid Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StockMenuCard(
                    title = "Barang",
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFF00FF4D), // Greenish
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )
                StockMenuCard(
                    title = "Kategori",
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFFFB300), // Yellowish
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )
                StockMenuCard(
                    title = "History",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF00BFFF), // Blueish
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Barang Terambil hari ini :", color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Tidak ada Data",
                        color = textMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StockMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF11111A))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
