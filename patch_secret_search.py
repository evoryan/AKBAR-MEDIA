import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

# Add a state for searchQuery
state_str = "var showSecretDialog by remember { mutableStateOf(false) }"
if "var secretSearchQuery by remember" not in content:
    content = content.replace(state_str, state_str + "\n    var secretSearchQuery by remember { mutableStateOf(\"\") }")

old_dialog = """    if (showSecretDialog) {
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

new_dialog = """    if (showSecretDialog) {
        AlertDialog(
            onDismissRequest = { showSecretDialog = false },
            title = { Text("Pilih PPPoE Secret") },
            text = {
                if (isLoadingSecrets) {
                    CircularProgressIndicator(color = neonCyan)
                } else if (secrets.isEmpty()) {
                    Text("Tidak ada secret di area ini atau gagal memuat.")
                } else {
                    Column {
                        OutlinedTextField(
                            value = secretSearchQuery,
                            onValueChange = { secretSearchQuery = it },
                            label = { Text("Cari Secret") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            singleLine = true
                        )
                        val filteredSecrets = secrets.filter { it.name.contains(secretSearchQuery, ignoreCase = true) }
                        if (filteredSecrets.isEmpty()) {
                            Text("Tidak ada secret yang cocok.")
                        } else {
                            LazyColumn {
                                items(filteredSecrets.size) { index ->
                                    val secret = filteredSecrets[index]
                                    Text(
                                        text = secret.name,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            selectedSecret = secret
                                            showSecretDialog = false
                                            secretSearchQuery = ""
                                        }.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSecretDialog = false; secretSearchQuery = "" }) { Text("Tutup") } }
        )
    }"""

content = content.replace(old_dialog, new_dialog)

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
