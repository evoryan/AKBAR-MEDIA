import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

# I will regex replace the whole state block
pattern = r'    val monthsToPay = remember \{ mutableStateListOf\("Mei 2026", "Juli 2026"\) \}\n    var showConfirmDialog by remember \{ mutableStateOf\(false\) \}\n\s*val monthlyFee = 125000\n    val totalAmount = monthsToPay\.size \* monthlyFee'

new_state = """    val context = androidx.compose.ui.platform.LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val monthsToPay = remember { mutableStateListOf("Tagihan Bulan Ini") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val monthlyFee = customer?.price?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
    val totalAmount = monthsToPay.size * monthlyFee
    
    androidx.compose.runtime.LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
            if (customer == null) {
                android.widget.Toast.makeText(context, "Pelanggan tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            android.widget.Toast.makeText(context, "Gagal memuat pelanggan", android.widget.Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }"""

content = re.sub(pattern, new_state, content)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
