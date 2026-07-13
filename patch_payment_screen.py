import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp\n", "import androidx.compose.ui.unit.sp\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\n")

if "import com.example.ui.screens.Customer" not in content:
    content = content.replace("import com.example.ui.data.remote.ApiClient\n", "import com.example.ui.data.remote.ApiClient\nimport com.example.ui.screens.Customer\n")

# State
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

# Replace Customer Info Card
old_info_card = """                        Text("Customer $customerId", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("628787965", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)"""
new_info_card = """                        Text(customer?.name ?: "Loading...", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(customer?.phone ?: "-", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)"""
content = content.replace(old_info_card, new_info_card)

# Details Card
old_details = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Reguler", color = textMain, fontSize = 14.sp)
                    Text("Rp. 125.000", color = textMain, fontSize = 14.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PPN 0%", color = textMain, fontSize = 14.sp)
                    Text("Rp. 0", color = textMain, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Perbulannya", color = textSecondary, fontSize = 14.sp)
                    Text("Rp. 125.000", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }"""

new_details = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(customer?.packageName ?: "Reguler", color = textMain, fontSize = 14.sp)
                    Text(customer?.price ?: "Rp. 0", color = textMain, fontSize = 14.sp)
                }
                if (!customer?.discount.isNullOrEmpty() && customer?.discount != "0") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon", color = textMain, fontSize = 14.sp)
                        Text("- ${customer?.discount}", color = Color.Green, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Biaya Perbulannya", color = textSecondary, fontSize = 14.sp)
                    Text(customer?.price ?: "Rp. 0", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }"""
content = content.replace(old_details, new_details)

# Wrap Scaffold with isLoading check
old_scaffold_inner = """    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgMain)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {"""

new_scaffold_inner = """    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = neonCyan)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(bgMain)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {"""
content = content.replace(old_scaffold_inner, new_scaffold_inner)
content = content.replace("                Button(\n                    onClick = { showConfirmDialog = true },", "                Button(\n                    onClick = { showConfirmDialog = true },")

content = content + "\n}\n" # close else block for isLoading

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
