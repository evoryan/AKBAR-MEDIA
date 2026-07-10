with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

import re

# Add imports
imports = """import androidx.compose.runtime.LaunchedEffect
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.DeleteBillingRequest
import kotlinx.coroutines.launch
import com.example.ui.data.UserSession
"""
content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + imports)

# Remove hardcoded customers and use API
old_customers = """    val customers = listOf(
        Customer("23", "A.salim", "087963842123", "Talun", "dul salim", "10", "BELUM BAYAR", "Rp. 100.000", "- Dskn : Rp. 0"),
        Customer("24", "Adam", "085225380900", "Kedung", "adam", "10", "LUNAS CASH", "Rp. 100.000", "- Dskn : Rp. 0"),
        Customer("42", "Adit", "081753951426", "Talun", "adit", "10", "BELUM BAYAR", "Rp. 100.000", "- Dskn : Rp. 0"),
        Customer("25", "Agus rt06", "087896365085", "Bate", "agus", "10", "BELUM BAYAR", "Rp. 100.000", "- Dskn : Rp. 0"),
        Customer("74", "Agus.05", "085325987456", "Talun", "agus kuprit", "10", "BELUM BAYAR", "Rp. 100.000", "- Dskn : Rp. 0")
    )"""

new_customers = """    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    fun fetchCustomers() {
        coroutineScope.launch {
            try {
                customers = ApiClient.apiService.getCustomers().filter { it.status != "TERHAPUS" }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        fetchCustomers()
    }"""
content = content.replace(old_customers, new_customers)

# Since customers are now fetched, unpaid / paid need mapping.
# The server has status 'Aktif' etc. But the Payment flow sets it to 'LUNAS CASH'.
# Let's map 'LUNAS CASH' to paid, and anything else (like 'Aktif') to unpaid.
old_unpaid = """    val unpaidCustomers = filteredBySearch.filter { it.status == "BELUM BAYAR" }
    val paidCustomers = filteredBySearch.filter { it.status != "BELUM BAYAR" }"""
new_unpaid = """    val unpaidCustomers = filteredBySearch.filter { it.status != "LUNAS CASH" }
    val paidCustomers = filteredBySearch.filter { it.status == "LUNAS CASH" }"""
content = content.replace(old_unpaid, new_unpaid)

# Update BillingCard to have a delete button if status != LUNAS CASH
card_old = """            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button("""

card_new = """            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    }
                Button("""
# wait we need onDeleteClick parameter for BillingCard
content = content.replace("fun BillingCard(customer: Customer, textMain: Color, textSecondary: Color, cardBg: Color, neonCyan: Color, onPayClick: () -> Unit, onDetailClick: () -> Unit) {", "fun BillingCard(customer: Customer, textMain: Color, textSecondary: Color, cardBg: Color, neonCyan: Color, onPayClick: () -> Unit, onDetailClick: () -> Unit, onDeleteClick: () -> Unit = {}) {")
content = content.replace(card_old, card_new)

# Update BillingCard calls
old_card_call = """                            BillingCard(
                                customer = customer,
                                textMain = textMain,
                                textSecondary = textSecondary,
                                cardBg = cardBg,
                                neonCyan = neonCyan,
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = { /*TODO*/ }
                            )"""
new_card_call = """                            BillingCard(
                                customer = customer,
                                textMain = textMain,
                                textSecondary = textSecondary,
                                cardBg = cardBg,
                                neonCyan = neonCyan,
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = { /*TODO*/ },
                                onDeleteClick = {
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.deleteBilling(DeleteBillingRequest(customer.id))
                                            fetchCustomers()
                                        } catch (e: Exception) {}
                                    }
                                }
                            )"""
content = content.replace(old_card_call, new_card_call)

# Also fix the check inside BillingCard
old_card_status_check = """if (customer.status == "BELUM BAYAR")"""
new_card_status_check = """if (customer.status != "LUNAS CASH")"""
content = content.replace(old_card_status_check, new_card_status_check)

# and color logic
old_card_color = """background(if (customer.status == "BELUM BAYAR") Color(0xFFFF003C).copy(alpha = 0.2f) else neonCyan.copy(alpha = 0.2f))"""
new_card_color = """background(if (customer.status != "LUNAS CASH") Color(0xFFFF003C).copy(alpha = 0.2f) else neonCyan.copy(alpha = 0.2f))"""
content = content.replace(old_card_color, new_card_color)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
