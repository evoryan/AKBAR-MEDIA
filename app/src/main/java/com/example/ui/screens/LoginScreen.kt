package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.LoginRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val bgMain = Color(0xFF05050A)
    val cardBg = Color(0xFF11111A)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val primaryCyan = Color(0xFF00FFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgMain),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "LOGIN",
                color = textMain,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Silakan masuk ke akun Anda",
                color = textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryCyan,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryCyan) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = textSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryCyan,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val user = ApiClient.apiService.login(LoginRequest(username, password))
                                UserSession.saveSession(context, user)
                                onLoginSuccess()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Username atau password salah: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Isi username dan password", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("MASUK", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
