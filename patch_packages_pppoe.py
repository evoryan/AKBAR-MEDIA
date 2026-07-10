import re

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'r') as f:
    content = f.read()

# Remove PPPoE from PackageItem
pkg_item_pppoe = """                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PPPoE Profile", color = textSecondary, fontSize = 12.sp)
                        Text(pkg.pppoeProfile, color = textMain, fontSize = 12.sp)
                    }"""

content = content.replace(pkg_item_pppoe, "")

# Remove PPPoE field from PackageFormDialog
pkg_form_pppoe = """                                        Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pppoeProfile,
                            onValueChange = { pppoeProfile = it },
                            label = { Text("PPPoE Profile", color = textSecondary) },
                            modifier = Modifier.weight(1f),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            readOnly = true
                        )
                        Button(
                            onClick = { /* TODO: Reload Profiles */ },
                            modifier = Modifier.padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, textSecondary)
                        ) {
                            Text("Reload")
                        }
                    }
                    Text("Pilih profile PPPoE dari Mikrotik. Klik Reload bila daftar belum muncul.", color = textSecondary, fontSize = 12.sp)"""

content = content.replace(pkg_form_pppoe, "")

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'w') as f:
    f.write(content)

