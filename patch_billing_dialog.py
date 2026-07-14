import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target_vars = """    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current"""

rep_vars = """    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var customerToCancel by remember { mutableStateOf<Customer?>(null) }
    var cancelPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current"""

content = content.replace(target_vars, rep_vars)

target_long_press = """                                onLongPress = {
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.deleteBilling(DeleteBillingRequest(customer.id))
                                            fetchCustomers()
                                            Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal membatalkan pembayaran", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }"""

rep_long_press = """                                onLongPress = {
                                    customerToCancel = customer
                                    showCancelDialog = true
                                }"""

content = content.replace(target_long_press, rep_long_press)

target_dialog = """    Scaffold(containerColor = bgMain,"""

rep_dialog = """    if (showCancelDialog && customerToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                showCancelDialog = false
                cancelPassword = ""
            },
            title = { Text("Batalkan Pembayaran", color = textMain) },
            text = { 
                Column {
                    Text("Masukkan password superadmin untuk membatalkan pembayaran ${customerToCancel?.name}:", color = textSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelPassword,
                        onValueChange = { cancelPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = cardBorder,
                            textColor = textMain
                        )
                    )
                }
            },
            containerColor = cardBg,
            confirmButton = {
                TextButton(onClick = {
                    if (cancelPassword == "superadmin") { // Assume superadmin password is "superadmin" or needs to check session?
                        coroutineScope.launch {
                            try {
                                ApiClient.apiService.deleteBilling(DeleteBillingRequest(customerToCancel!!.id))
                                customers = customers.map { if (it.id == customerToCancel!!.id) it.copy(status = "BELUM BAYAR") else it }
                                Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membatalkan pembayaran", Toast.LENGTH_SHORT).show()
                            }
                            showCancelDialog = false
                            cancelPassword = ""
                            customerToCancel = null
                        }
                    } else {
                        Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Batalkan", color = errorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    cancelPassword = ""
                }) {
                    Text("Tutup", color = textSecondary)
                }
            }
        )
    }

    Scaffold(containerColor = bgMain,"""

content = content.replace(target_dialog, rep_dialog)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
