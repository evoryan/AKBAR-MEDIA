import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

# Fix 1: Wrap OutlinedTextField and Box in a Box for Mikrotik Secret
old_secret_box = """                        OutlinedTextField(
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
                        })"""

new_secret_box = """                        Box {
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
content = content.replace(old_secret_box, new_secret_box)

# Fix 2: it.odpPort.isNotBlank()
content = content.replace("it.odpPort.isNotBlank()", "!it.odpPort.isNullOrBlank()")

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
