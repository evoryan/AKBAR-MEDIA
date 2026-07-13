import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

# Add PaymentHistory state
state_code = "var paymentHistory by remember { mutableStateOf<List<com.example.ui.data.remote.PaymentHistory>>(emptyList()) }"
if "paymentHistory by remember" not in content:
    content = content.replace("var showPaymentDialog by remember { mutableStateOf(false) }", state_code + "\n    var showPaymentDialog by remember { mutableStateOf(false) }")

fetch_code = """
            if (c != null) {
                try {
                    paymentHistory = ApiClient.apiService.getCustomerHistory(c.id)
                } catch(e: Exception) {}
"""
content = content.replace("if (c != null) {", fetch_code)

# Replace PaymentHistorySection
old_history = """@Composable
fun PaymentHistorySection(cardBg: Color, cardBorder: Color, textMain: Color, textSecondary: Color, neonCyan: Color, neonPink: Color) {
    val history = listOf(
        mapOf("date" to "10 Jun 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Transfer Bank"),
        mapOf("date" to "10 May 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Cash"),
        mapOf("date" to "10 Apr 2026", "amount" to "Rp. 100.000", "status" to "Lunas", "method" to "Transfer Bank")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Riwayat Transaksi", color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        history.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(item["date"] ?: "", color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item["method"] ?: "", color = textSecondary, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(item["amount"] ?: "", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item["status"] ?: "", color = neonPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}"""

new_history = """@Composable
fun PaymentHistorySection(history: List<com.example.ui.data.remote.PaymentHistory>, cardBg: Color, cardBorder: Color, textMain: Color, textSecondary: Color, neonCyan: Color, neonPink: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Riwayat Transaksi", color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        if (history.isEmpty()) {
            Text("Tidak ada riwayat transaksi", color = textSecondary, fontSize = 14.sp)
        } else {
            history.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            val date = item.createdAt?.substringBefore("T") ?: ""
                            Text(date, color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.description, color = textSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rp. ${item.amount}", color = neonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lunas", color = neonPink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}"""

content = content.replace(old_history, new_history)

# Update the call to PaymentHistorySection
content = content.replace("PaymentHistorySection(cardBg = cardBg, cardBorder = cardBorder, textMain = textMain, textSecondary = textSecondary, neonCyan = neonCyan, neonPink = neonPink)", "PaymentHistorySection(paymentHistory, cardBg = cardBg, cardBorder = cardBorder, textMain = textMain, textSecondary = textSecondary, neonCyan = neonCyan, neonPink = neonPink)")

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
