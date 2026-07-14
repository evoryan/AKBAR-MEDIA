import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target_import = "import androidx.compose.material3.*"
rep_import = "import androidx.compose.material3.*\nimport androidx.compose.foundation.ExperimentalFoundationApi\nimport androidx.compose.foundation.combinedClickable\nimport com.example.ui.data.remote.LoginRequest\nimport com.example.ui.data.UserRole\nimport androidx.compose.ui.text.input.PasswordVisualTransformation"

if "import androidx.compose.foundation.ExperimentalFoundationApi" not in content:
    content = content.replace(target_import, rep_import)

# Modifying BillingCustomerItem to support long press
target_item_def = """fun BillingCustomerItem(
    customer: Customer,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    neonPink: Color,
    onPayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {"""

rep_item_def = """@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillingCustomerItem(
    customer: Customer,
    cardBg: Color,
    cardBorder: Color,
    textMain: Color,
    textSecondary: Color,
    neonCyan: Color,
    neonPink: Color,
    onPayClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {"""

content = content.replace(target_item_def, rep_item_def)

target_box = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {"""

rep_box = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )
            .padding(12.dp)
    ) {"""

content = content.replace(target_box, rep_box)

# Now updating where BillingCustomerItem is called for paid customers
target_paid_call = """                            BillingCustomerItem(
                                customer = customer, 
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = {},
                                onDetailClick = {}
                            )"""

rep_paid_call = """                            BillingCustomerItem(
                                customer = customer, 
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = {},
                                onDetailClick = {},
                                onLongPress = {
                                    customerToCancel = customer
                                    showCancelDialog = true
                                }
                            )"""

content = content.replace(target_paid_call, rep_paid_call)

# Injecting dialog state in BillingScreen
target_vars = """    var showDeleteConfirm by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }"""

rep_vars = """    var showDeleteConfirm by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var customerToCancel by remember { mutableStateOf<Customer?>(null) }
    var cancelPassword by remember { mutableStateOf("") }
    var isCanceling by remember { mutableStateOf(false) }"""

content = content.replace(target_vars, rep_vars)

# Injecting dialog UI at the end of BillingScreen
target_end = """        if (showDeleteConfirm && customerToDelete != null) {
            AlertDialog("""

dialog_ui = """        if (showCancelDialog && customerToCancel != null) {
            AlertDialog(
                onDismissRequest = { 
                    showCancelDialog = false 
                    cancelPassword = ""
                },
                title = { Text("Batalkan Transaksi") },
                text = {
                    Column {
                        Text("Masukkan password login super admin untuk membatalkan transaksi ini (kembalikan ke Belum Bayar).")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cancelPassword,
                            onValueChange = { cancelPassword = it },
                            label = { Text("Password Super Admin") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (cancelPassword.isNotBlank()) {
                                isCanceling = true
                                coroutineScope.launch {
                                    try {
                                        // Cari super admin username dulu
                                        val admins = ApiClient.apiService.getAdmins()
                                        val superAdmin = admins.find { it.role == UserRole.SUPER_ADMIN }
                                        if (superAdmin != null) {
                                            // Coba login
                                            ApiClient.apiService.login(LoginRequest(superAdmin.username, cancelPassword))
                                            // Jika berhasil, lakukan pembatalan
                                            ApiClient.apiService.deleteBilling(com.example.ui.data.remote.DeleteBillingRequest(customerToCancel!!.id))
                                            
                                            // Refresh data
                                            val updatedCustomers = ApiClient.apiService.getCustomers()
                                            onCustomersUpdated(updatedCustomers)
                                            
                                            Toast.makeText(context, "Transaksi dibatalkan", Toast.LENGTH_SHORT).show()
                                            showCancelDialog = false
                                            cancelPassword = ""
                                        } else {
                                            Toast.makeText(context, "Akun Super Admin tidak ditemukan", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Password salah atau gagal membatalkan", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCanceling = false
                                    }
                                }
                            }
                        },
                        enabled = !isCanceling
                    ) {
                        Text(if (isCanceling) "Memproses..." else "Batalkan")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showCancelDialog = false
                            cancelPassword = ""
                        },
                        enabled = !isCanceling
                    ) {
                        Text("Tutup")
                    }
                }
            )
        }

        if (showDeleteConfirm && customerToDelete != null) {"""

content = content.replace(target_end, dialog_ui)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
print("Patched BillingScreen.kt")
