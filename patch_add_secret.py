import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

# Add states
states_str = """    var showSecretDialog by remember { mutableStateOf(false) }
    var showAddSecretDialog by remember { mutableStateOf(false) }
    var newSecretUsername by remember { mutableStateOf("") }
    var newSecretPassword by remember { mutableStateOf("") }
    var newSecretProfile by remember { mutableStateOf("") }
    var isAddingSecret by remember { mutableStateOf(false) }"""

content = content.replace("    var showSecretDialog by remember { mutableStateOf(false) }", states_str)

# Add dialog UI
dialog_ui = """    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            title = { Text("Pilih PPPoE Secret") },
            text = {
                if (isLoadingSecrets) {
                    CircularProgressIndicator(color = neonCyan)
                } else if (secrets.isEmpty()) {
                    Text("Tidak ada secret di area ini atau gagal memuat.")
                } else {
                    LazyColumn {
                        items(secrets.size) { index ->
                            val secret = secrets[index]
                            Text(
                                text = secret.name,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedSecret = secret
                                    showSecretDialog = false
                                }.padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSecretDialog = false }) { Text("Tutup") } }
        )
    }

    if (showAddSecretDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAddingSecret) showAddSecretDialog = false },
            title = { Text("Tambah Secret Baru", color = neonCyan) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSecretUsername,
                        onValueChange = { newSecretUsername = it },
                        label = { Text("Username Secret", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSecretPassword,
                        onValueChange = { newSecretPassword = it },
                        label = { Text("Password", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newSecretProfile,
                        onValueChange = { newSecretProfile = it },
                        label = { Text("Profile (opsional, default 'default')", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSecretUsername.isNotBlank() && newSecretPassword.isNotBlank() && selectedArea != null) {
                            isAddingSecret = true
                            coroutineScope.launch {
                                try {
                                    ApiClient.apiService.addMikrotikSecret(
                                        selectedArea!!.id,
                                        mapOf(
                                            "name" to newSecretUsername,
                                            "password" to newSecretPassword,
                                            "profile" to newSecretProfile
                                        )
                                    )
                                    // Refresh secrets
                                    secrets = ApiClient.apiService.getMikrotikSecrets(selectedArea!!.id)
                                    
                                    // Select the newly added secret
                                    selectedSecret = secrets.find { it.name == newSecretUsername } ?: selectedSecret
                                    
                                    Toast.makeText(context, "Secret berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    showAddSecretDialog = false
                                    newSecretUsername = ""
                                    newSecretPassword = ""
                                    newSecretProfile = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal menambah secret: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isAddingSecret = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isAddingSecret
                ) { 
                    if (isAddingSecret) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = neonCyan, strokeWidth = 2.dp)
                    } else {
                        Text("Simpan", color = neonCyan) 
                    }
                }
            },
            dismissButton = { 
                TextButton(
                    onClick = { showAddSecretDialog = false },
                    enabled = !isAddingSecret
                ) { Text("Batal", color = textSecondary) } 
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = neonCyan,
            textContentColor = textMain
        )
    }"""

old_dialog_ui = """    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            title = { Text("Pilih PPPoE Secret") },
            text = {
                if (isLoadingSecrets) {
                    CircularProgressIndicator(color = neonCyan)
                } else if (secrets.isEmpty()) {
                    Text("Tidak ada secret di area ini atau gagal memuat.")
                } else {
                    LazyColumn {
                        items(secrets.size) { index ->
                            val secret = secrets[index]
                            Text(
                                text = secret.name,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedSecret = secret
                                    showSecretDialog = false
                                }.padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSecretDialog = false }) { Text("Tutup") } }
        )
    }"""
content = content.replace(old_dialog_ui, dialog_ui)

# Add "Tambah Secret" Button in Tab
tab_ui = """                        Box {
                            OutlinedTextField(
                                value = selectedSecret?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("PPPoE Secret", color = textSecondary) },
                                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                                modifier = Modifier.fillMaxWidth().clickable { showSecretDialog = true },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary)
                            )
                            // Invisible box over text field to handle click
                            Box(modifier = Modifier.matchParentSize().clickable { 
                                if (selectedArea == null) {
                                    Toast.makeText(context, "Pilih area di Tab Akun terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showSecretDialog = true 
                                }
                            })
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { 
                                if (selectedArea == null) {
                                    Toast.makeText(context, "Pilih area di Tab Akun terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showAddSecretDialog = true 
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = neonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, neonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Tambah Secret Baru", fontWeight = FontWeight.Bold)
                        }"""
                        
old_tab_ui = """                        Box {
                            OutlinedTextField(
                                value = selectedSecret?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("PPPoE Secret", color = textSecondary) },
                                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                                modifier = Modifier.fillMaxWidth().clickable { showSecretDialog = true },
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary)
                            )
                            // Invisible box over text field to handle click
                            Box(modifier = Modifier.matchParentSize().clickable { 
                                if (selectedArea == null) {
                                    Toast.makeText(context, "Pilih area di Tab Akun terlebih dahulu", Toast.LENGTH_SHORT).show()
                                } else {
                                    showSecretDialog = true 
                                }
                            })
                        }"""
content = content.replace(old_tab_ui, tab_ui)

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
