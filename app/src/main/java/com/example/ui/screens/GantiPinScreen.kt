package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GantiPinScreen(onBack: () -> Unit) {
    val bgMain = Color(0xFF0A0A0A)
    val textMain = Color(0xFFFFFFFF)
    val primaryBg = Color(0xFF00FFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBorder = Color(0xFF333333)

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    Scaffold(
        containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Ganti PIN", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Ubah PIN (6 digit) untuk verifikasi penghapusan transaksi dan tindakan sensitif lainnya.",
                color = textSecondary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = oldPin,
                onValueChange = { if (it.length <= 6) oldPin = it },
                label = { Text("PIN Lama") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = newPin,
                onValueChange = { if (it.length <= 6) newPin = it },
                label = { Text("PIN Baru (6 Digit)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                singleLine = true
            )
            
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 6) confirmPin = it },
                label = { Text("Konfirmasi PIN Baru") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* TODO Save PIN */ onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBg, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                enabled = newPin.length == 6 && newPin == confirmPin
            ) {
                Text("Simpan PIN", fontWeight = FontWeight.Bold)
            }
        }
    }
}
