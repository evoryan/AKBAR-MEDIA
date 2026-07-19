import re

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'r') as f:
    content = f.read()

# Revert dialogPayments state
content = content.replace(
    "var dialogPayments by remember { mutableStateOf<List<com.example.ui.data.remote.PembukuanItem>>(emptyList()) }",
    "var dialogPayments by remember { mutableStateOf<List<com.example.ui.data.remote.PembayaranHistoryItem>>(emptyList()) }"
)

# Revert API call
content = content.replace(
    "val allPayments = com.example.ui.data.remote.ApiClient.apiService.getAllPembukuan(); dialogPayments = allPayments.filter { it.admin_name == admin.name && it.type == \"pemasukan\" }",
    "val allPayments = com.example.ui.data.remote.ApiClient.apiService.getPembayaranHistory(); dialogPayments = allPayments.filter { it.admin_name == admin.name }"
)

# Revert UI rendering
content = content.replace(
    "Text(payment.description ?: \"-\", color = textMain, fontWeight = FontWeight.Bold)",
    "Text(\"${payment.customer_name ?: \"-\"} (${payment.bulan} ${payment.tahun})\", color = textMain, fontWeight = FontWeight.Bold)"
)

content = content.replace(
    "Text(\"Rp. ${String.format(\"%,d\", payment.amount.toLong()).replace(\",\", \".\")}\"",
    "Text(\"Rp. ${String.format(\"%,d\", payment.amount?.toLong() ?: 0).replace(\",\", \".\")}\""
)

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'w') as f:
    f.write(content)

print("Reverted UI successfully")
