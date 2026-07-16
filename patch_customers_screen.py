import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

# 1. Update CustomerItem signature
content = content.replace(
    "fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit) {",
    "fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit, onIsolirCustomer: (Customer) -> Unit = {}) {"
)

# 2. Add Block icon for BELUM BAYAR
icon_row = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customer.status.contains("BELUM BAYAR", ignoreCase = true)) {
                        IconButton(onClick = { onIsolirCustomer(customer) }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Block, contentDescription = "Isolir", tint = errorRed)
                        }
                    }
                    IconButton"""
content = content.replace(
    """                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton""",
    icon_row
)

# 3. Handle onIsolirCustomer in the items list
items_replacement = """                    CustomerItem(customer, onNavigateToCustomerDetail, onDeleteCustomer = { customerToDelete ->
                        customerToDeleteState = customerToDelete
                        showDeleteConfirm = true
                    }, onIsolirCustomer = { customerToIsolir ->
                        coroutineScope.launch {
                            try {
                                com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customerToIsolir.id)
                                android.widget.Toast.makeText(context, "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Gagal mengisolir pelanggan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    })"""
content = content.replace(
    """                    CustomerItem(customer, onNavigateToCustomerDetail, onDeleteCustomer = { customerToDelete ->
                        customerToDeleteState = customerToDelete
                        showDeleteConfirm = true
                    })""",
    items_replacement
)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
