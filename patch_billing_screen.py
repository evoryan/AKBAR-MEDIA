import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

# 1. Update BillingCustomerItem signature
content = content.replace(
    "fun BillingCustomerItem(",
    "fun BillingCustomerItem("
) # Let's just do a direct replace

old_sig = """fun BillingCustomerItem(
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
)"""

new_sig = """fun BillingCustomerItem(
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
    onLongPress: () -> Unit = {},
    onIsolirClick: () -> Unit = {}
)"""
content = content.replace(old_sig, new_sig)

# 2. Add Isolir button for unpaid customers
old_buttons = """                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    }"""

new_buttons = """                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onIsolirClick, modifier = Modifier.size(36.dp)) {
                            Icon(androidx.compose.material.icons.Icons.Default.Block, contentDescription = "Isolir", tint = Color(0xFFD4AF37))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    }"""
content = content.replace(old_buttons, new_buttons)

# 3. Handle onIsolirClick in unpaid list
old_item = """                            BillingCustomerItem(
                                customer = customer, 
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = {}
                            )"""

new_item = """                            BillingCustomerItem(
                                customer = customer, 
                                cardBg = cardBg, 
                                cardBorder = cardBorder, 
                                textMain = textMain, 
                                textSecondary = textSecondary, 
                                neonCyan = neonCyan, 
                                neonPink = neonPink, 
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = {},
                                onIsolirClick = {
                                    coroutineScope.launch {
                                        try {
                                            com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customer.id)
                                            android.widget.Toast.makeText(context, "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Gagal mengisolir pelanggan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )"""
content = content.replace(old_item, new_item)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)

