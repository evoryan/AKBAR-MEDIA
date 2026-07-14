import re

with open("app/src/main/java/com/example/ui/screens/UpdateProfilScreen.kt", "r") as f:
    content = f.read()

target = """    var name by remember { mutableStateOf("Admin Satria") }
    var username by remember { mutableStateOf("admin123") }"""

rep = """    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = com.example.ui.data.UserSession.currentUser.collectAsState().value
    
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }"""

content = content.replace(target, rep)

target2 = """            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBg),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Simpan Perubahan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }"""

rep2 = """            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (kosongkan jika tidak diubah)") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBg,
                    unfocusedBorderColor = cardBorder,
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (currentUser == null) return@Button
                    isUpdating = true
                    coroutineScope.launch {
                        try {
                            val req = mutableMapOf("name" to name, "username" to username)
                            if (password.isNotEmpty()) {
                                req["password"] = password
                            }
                            com.example.ui.data.remote.ApiClient.apiService.updateAdmin(currentUser.id, req)
                            // Update session locally
                            val updatedUser = currentUser.copy(name = name, username = username)
                            com.example.ui.data.UserSession.saveSession(context, updatedUser)
                            android.widget.Toast.makeText(context, "Profil berhasil diupdate", android.widget.Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Gagal update profil: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBg),
                shape = RoundedCornerShape(25.dp),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }"""

content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/UpdateProfilScreen.kt", "w") as f:
    f.write(content)
print("Patched UpdateProfilScreen.kt")
