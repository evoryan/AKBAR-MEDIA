import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

target = """                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; isPhoneError = it.length < 10 || !it.all { char -> char.isDigit() } },
                            label = { Text("Phone", color = if (isPhoneError) errorRed else textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = isPhoneError,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )"""

rep = """                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; isPhoneError = it.length < 10 || !it.all { char -> char.isDigit() } },
                            label = { Text("Phone", color = if (isPhoneError) errorRed else textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            isError = isPhoneError,
                            trailingIcon = {
                                IconButton(onClick = { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Contacts, contentDescription = "Pilih Kontak", tint = neonCyan)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary, focusedTextColor = textMain, unfocusedTextColor = textMain),
                            singleLine = true
                        )"""

content = content.replace(target, rep)

target2 = """                        ClickableField(title = "Area", subtitle = selectedArea?.name, actionText = "Pilih Area", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showAreaDialog = true })"""

rep2 = """                        ClickableField(title = "Area", subtitle = null, actionText = selectedArea?.name ?: "Pilih Area", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showAreaDialog = true })"""

target3 = """                        ClickableField(title = "Paket", subtitle = selectedPackage?.name?.let { "$it (Rp ${selectedPackage?.price})" }, actionText = "Pilih Paket", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showPackageDialog = true })"""

rep3 = """                        ClickableField(title = "Paket", subtitle = null, actionText = selectedPackage?.name?.let { "$it (Rp ${selectedPackage?.price})" } ?: "Pilih Paket", neonCyan = neonCyan, textSecondary = textSecondary, onClick = { showPackageDialog = true })"""

content = content.replace(target2, rep2)
content = content.replace(target3, rep3)

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(content)
