import re

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'r') as f:
    content = f.read()

# Add ApiClient if not present
if "ApiClient" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport com.example.ui.data.remote.ApiClient\nimport com.example.ui.data.remote.InternetPackageRequest")

# Modify PackagesScreen to fetch correctly and save correctly
# Look at onSave inside PackagesScreen
on_save_old = """                onSave = { newPkg ->
                    if (packageToEdit != null) {
                        val index = packages.indexOfFirst { it.id == packageToEdit!!.id }
                        if (index != -1) {
                            packages[index] = newPkg.copy(id = packageToEdit!!.id)
                        }
                    } else {
                        packages.add(newPkg.copy(id = UUID.randomUUID().toString()))
                    }
                    showAddDialog = false
                    packageToEdit = null
                },"""
on_save_new = """                onSave = { newPkg ->
                    coroutineScope.launch {
                        try {
                            if (packageToEdit != null) {
                                ApiClient.apiService.updatePackage(packageToEdit!!.id, newPkg)
                            } else {
                                ApiClient.apiService.addPackage(newPkg)
                            }
                            packages.clear()
                            packages.addAll(ApiClient.apiService.getPackages())
                        } catch (e: Exception) {}
                    }
                    showAddDialog = false
                    packageToEdit = null
                },"""
content = content.replace(on_save_old, on_save_new)

# Fix PackageFormDialog
content = content.replace("var pppoeProfile by remember { mutableStateOf(initialPackage?.pppoeProfile ?: \"default\") }", "")
content = content.replace("val pppoeProfile: String = \"default\",", "")
# Wait, pppoeProfile is in InternetPackage data class, so we shouldn't remove it from data class completely if we just pass default. But wait, in the data class:
# InternetPackage(id, name, speed, price, taxRate, pppoeProfile, description)
# Let's just remove the pppoeProfile parameter from the Dialog state, and when calling InternetPackage, just don't pass pppoeProfile (it has a default) or pass "default".

content = content.replace("pppoeProfile = pppoeProfile", "pppoeProfile = \"default\"")

pppoe_ui = """                    Row(
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

content = content.replace(pppoe_ui, "")

# Ensure keyboard doesn't overlap
# Change DialogProperties
content = content.replace("DialogProperties(usePlatformDefaultWidth = false)", "DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)")
# Add imePadding to Surface
content = content.replace("modifier = Modifier\n                .fillMaxSize()\n                .background(bgMain),", "modifier = Modifier\n                .fillMaxSize()\n                .background(bgMain)\n                .imePadding(),")

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'w') as f:
    f.write(content)

