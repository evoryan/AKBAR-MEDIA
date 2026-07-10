filepath = 'app/src/main/java/com/example/ui/screens/UpdateEmailScreen.kt'
content = """package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmailScreen(onBack: () -> Unit) {
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBorder = Color(0xFF333333)

    var currentEmail by remember { mutableStateOf("admin@example.com") }
    var newEmail by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Update Email", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Gunakan email aktif untuk mendaftarkan akun. Email ini digunakan untuk verifikasi keamanan dan notifikasi.",
                color = textSecondary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = currentEmail,
                onValueChange = {},
                enabled = false,
                label = { Text("Email Saat Ini") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = textSecondary,
                    disabledBorderColor = cardBorder,
                    disabledLabelColor = textSecondary
                )
            )

            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("Email Baru") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                singleLine = true
            )

            if (!isCodeSent) {
                Button(
                    onClick = { isCodeSent = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBg, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kirim Kode Verifikasi", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Kode verifikasi telah dikirim ke email baru Anda.", color = primaryBg, fontSize = 14.sp)
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Kode Verifikasi (OTP)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBg,
                        unfocusedBorderColor = cardBorder,
                        focusedTextColor = textMain,
                        unfocusedTextColor = textMain
                    ),
                    singleLine = true
                )
                Button(
                    onClick = { /* TODO Verify and Save */ onBack() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBg, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Email Baru", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
"""
with open(filepath, 'w') as f:
    f.write(content)
