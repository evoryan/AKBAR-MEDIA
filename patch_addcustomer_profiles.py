import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

# 1. Add states for profiles
states = """    var showAddSecretDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf<List<com.example.ui.data.remote.MikrotikProfile>>(emptyList()) }"""
content = content.replace("    var showAddSecretDialog by remember { mutableStateOf(false) }", states)

# 2. Update LaunchedEffect to fetch profiles
effect = """    LaunchedEffect(selectedArea) {
        if (selectedArea != null) {
            selectedSecret = null
            isLoadingSecrets = true
            try {
                secrets = ApiClient.apiService.getMikrotikSecrets(selectedArea!!.id)
                profiles = ApiClient.apiService.getMikrotikProfiles(selectedArea!!.id)
            } catch (e: Exception) {
                secrets = emptyList()
                profiles = emptyList()
            } finally {
                isLoadingSecrets = false
            }
        }
    }"""
content = re.sub(r'    LaunchedEffect\(selectedArea\) \{[\s\S]*?isLoadingSecrets = false\n            \}\n        \}\n    \}', effect, content)

# 3. Add Profile Dialog
profile_dialog = """    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Pilih Profile", color = neonCyan) },
            text = {
                if (profiles.isEmpty()) {
                    Text("Tidak ada profile di area ini atau gagal memuat.")
                } else {
                    LazyColumn {
                        items(profiles.size) { index ->
                            val profile = profiles[index]
                            Text(
                                text = profile.name,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    newSecretProfile = profile.name
                                    showProfileDialog = false
                                }.padding(16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Tutup") } },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = neonCyan,
            textContentColor = textMain
        )
    }

    if (showAddSecretDialog) {"""
content = content.replace("    if (showAddSecretDialog) {", profile_dialog)

# 4. Update the Add Secret Dialog to use dropdown for Profile
old_profile_input = """                    OutlinedTextField(
                        value = newSecretProfile,
                        onValueChange = { newSecretProfile = it },
                        label = { Text("Profile (opsional, default 'default')", color = textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )"""

new_profile_input = """                    Box {
                        OutlinedTextField(
                            value = newSecretProfile.ifBlank { "default" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Profile", color = textSecondary) },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Pilih", tint = neonCyan) },
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = textMain, disabledBorderColor = textSecondary, disabledLabelColor = textSecondary),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showProfileDialog = true })
                    }"""
content = content.replace(old_profile_input, new_profile_input)

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
