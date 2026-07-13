import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

old_state = """    val monthsToPay = remember { mutableStateListOf("Mei 2026", "Juli 2026") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val monthlyFee = 125000
    val totalAmount = monthsToPay.size * monthlyFee"""

new_state = """    val context = LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val monthsToPay = remember { mutableStateListOf("Tagihan Bulan Ini") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    val monthlyFee = customer?.price?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
    val totalAmount = monthsToPay.size * monthlyFee
    
    LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
            if (customer == null) {
                Toast.makeText(context, "Pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            Toast.makeText(context, "Gagal memuat pelanggan", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }"""

content = content.replace(old_state, new_state)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
