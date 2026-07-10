import os

screens = [
    ("UpdateEmail", "Update Email"),
    ("UpdateProfil", "Update Profil"),
    ("GantiPassword", "Ganti Password"),
    ("GantiPin", "Ganti PIN"),
    ("DaftarAdmin", "Daftar Admin"),
    ("Odc", "Kelola ODC"),
    ("Odp", "Kelola ODP"),
    ("GatewayPayment", "Pengaturan Gateway Payment")
]

template = """package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {name}Screen(onBack: () -> Unit) {
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("{title}", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Halaman {title}", color = Color.Gray)
        }
    }
}
"""

for name, title in screens:
    filepath = f"app/src/main/java/com/example/ui/screens/{name}Screen.kt"
    with open(filepath, "w") as f:
        f.write(template.replace("{name}", name).replace("{title}", title))
