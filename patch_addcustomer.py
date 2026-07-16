import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

# Add address variable
var_pattern = r"    var name by remember \{ mutableStateOf\(\"\"\) \}\n    var phone by remember \{ mutableStateOf\(\"\"\) \}"
var_replacement = "    var name by remember { mutableStateOf(\"\") }\n    var phone by remember { mutableStateOf(\"\") }\n    var address by remember { mutableStateOf(\"\") }"
content = re.sub(var_pattern, var_replacement, content)

# Add address text field in UI (after Phone field)
field_pattern = r"""                        OutlinedTextField\(
                            value = phone,
                            onValueChange = \{ phone = it; isPhoneError = false \},
                            label = \{ Text\("No\. WhatsApp \(Opsional\)"\) \},
                            modifier = Modifier\.fillMaxWidth\(\),
                            shape = RoundedCornerShape\(12\.dp\),
                            colors = OutlinedTextFieldDefaults\.colors\(
                                focusedBorderColor = MaterialTheme\.colorScheme\.primary,
                                unfocusedBorderColor = MaterialTheme\.colorScheme\.outline
                            \),
                            keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Phone\),
                            isError = isPhoneError,
                            trailingIcon = \{
                                IconButton\(onClick = \{
                                    contactPickerLauncher\.launch\(null\)
                                \}\) \{
                                    Icon\(imageVector = Icons\.Default\.Contacts, contentDescription = "Pilih dari Kontak", tint = MaterialTheme\.colorScheme\.primary\)
                                \}
                            \}
                        \)"""
field_replacement = """                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; isPhoneError = false },
                            label = { Text("No. WhatsApp (Opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = isPhoneError,
                            trailingIcon = {
                                IconButton(onClick = {
                                    contactPickerLauncher.launch(null)
                                }) {
                                    Icon(imageVector = Icons.Default.Contacts, contentDescription = "Pilih dari Kontak", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Alamat (Opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )"""
content = re.sub(field_pattern, field_replacement, content)

# Add address to AddCustomerRequest in AddCustomerScreen
req_pattern = r"""                                            val request = com\.example\.ui\.data\.remote\.AddCustomerRequest\(
                                                name = name,
                                                phone = phone,
                                                area = selectedArea\?\.name \?: "",
                                                username = selectedSecret\?\.name \?: "",
                                                billingDate = billingDate,
                                                status = "AKTIF",
                                                price = selectedPackage\?\.price\?\.toString\(\) \?: "0",
                                                discount = "0",
                                                registerDate = registerDate,
                                                isolateDate = isolateDate,
                                                packageName = selectedPackage\?\.name \?: "",
                                                pppoeSecret = selectedSecret\?\.name \?: "",
                                                odpId = selectedOdp\?\.id\?\.toString\(\),
                                                odpPort = selectedPort,
                                                additionalCost1 = additionalCost1,
                                                additionalCost2 = additionalCost2
                                            \)"""
req_replacement = """                                            val request = com.example.ui.data.remote.AddCustomerRequest(
                                                name = name,
                                                phone = phone,
                                                area = selectedArea?.name ?: "",
                                                address = address,
                                                username = selectedSecret?.name ?: "",
                                                billingDate = billingDate,
                                                status = "AKTIF",
                                                price = selectedPackage?.price?.toString() ?: "0",
                                                discount = "0",
                                                registerDate = registerDate,
                                                isolateDate = isolateDate,
                                                packageName = selectedPackage?.name ?: "",
                                                pppoeSecret = selectedSecret?.name ?: "",
                                                odpId = selectedOdp?.id?.toString(),
                                                odpPort = selectedPort,
                                                additionalCost1 = additionalCost1,
                                                additionalCost2 = additionalCost2
                                            )"""
content = re.sub(req_pattern, req_replacement, content)

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(content)
