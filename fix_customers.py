import re

filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make customers state-based and fetch from API
replacement = """
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            customers = ApiClient.apiService.getCustomers()
        } catch (e: Exception) {
        }
    }
"""

# Replace static val customers = listOf(...)
content = re.sub(r'val customers = listOf\([^)]*\)', replacement, content, flags=re.DOTALL)

# Add delete button to CustomerItem
customer_item_replacement = """
            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        // Call delete callback (we need to pass it)
                        onDeleteCustomer(customer)
                    }) {
                        Icon(androidx.compose.material.icons.filled.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                    Box(
"""
# We need to add onDeleteCustomer to CustomerItem
content = content.replace("fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit)", 
"fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit)")

# In LazyColumn, pass onDeleteCustomer
content = content.replace("CustomerItem(customer, onNavigateToCustomerDetail)", """
                    CustomerItem(customer, onNavigateToCustomerDetail, onDeleteCustomer = { customerToDelete ->
                        customerToDeleteState = customerToDelete
                        showDeleteConfirm = true
                    })
""")

# We need a state for showDeleteConfirm and customerToDeleteState in CustomersScreen
dialog_code = """
    if (showDeleteConfirm && customerToDeleteState != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Konfirmasi") },
            text = { Text("Yakin ingin menghapus pelanggan ${customerToDeleteState?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            ApiClient.apiService.deleteCustomer(customerToDeleteState!!.id)
                            customers = customers.filter { it.id != customerToDeleteState!!.id }
                            showDeleteConfirm = false
                            customerToDeleteState = null
                        } catch (e: Exception) {}
                    }
                }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
"""

content = content.replace("var expanded by remember { mutableStateOf(false) }", "var showDeleteConfirm by remember { mutableStateOf(false) }\n    var customerToDeleteState by remember { mutableStateOf<Customer?>(null) }\n    var expanded by remember { mutableStateOf(false) }")

content = content.replace("        LazyColumn(", dialog_code + "\n        LazyColumn(")

# Add errorRed to CustomerItem
content = content.replace("val greenText = Color(0xFF00FF4D)", "val greenText = Color(0xFF00FF4D)\n    val errorRed = Color(0xFFFF003C)")

# Ensure Icons.Default.Delete is imported
if "import androidx.compose.material.icons.filled.Delete" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Delete")

with open(filepath, 'w') as f:
    f.write(content)
