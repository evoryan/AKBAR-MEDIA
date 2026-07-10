import re

filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make customers state-based and fetch from API
# Replace static val customers = listOf(...)
# We need a precise regex or string matching to cut it out.
start_idx = content.find("val customers = listOf(")
end_idx = content.find("val areas = listOf")
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + """    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            customers = ApiClient.apiService.getCustomers()
        } catch (e: Exception) {
        }
    }
    
    """ + content[end_idx:]

# Add delete button to CustomerItem
target_box = """            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Box("""

replacement_box = """            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDeleteCustomer(customer) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                Box("""
content = content.replace(target_box, replacement_box)

target_box_end = """                    Text(text = customer.id, color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }"""

replacement_box_end = """                    Text(text = customer.id, color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                }"""
content = content.replace(target_box_end, replacement_box_end)

# Add errorRed to CustomerItem
content = content.replace("val greenText = Color(0xFF00FF4D)", "val greenText = Color(0xFF00FF4D)\n    val errorRed = Color(0xFFFF003C)")

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
                    Text("Batal", color = textMain)
                }
            },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textMain
        )
    }
"""

content = content.replace("var expanded by remember { mutableStateOf(false) }", "var showDeleteConfirm by remember { mutableStateOf(false) }\n    var customerToDeleteState by remember { mutableStateOf<Customer?>(null) }\n    var expanded by remember { mutableStateOf(false) }")

content = content.replace("        LazyColumn(", dialog_code + "\n        LazyColumn(")

# Imports
if "import androidx.compose.material.icons.filled.Delete" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Delete")
if "import androidx.compose.runtime.rememberCoroutineScope" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.runtime.rememberCoroutineScope")

with open(filepath, 'w') as f:
    f.write(content)

