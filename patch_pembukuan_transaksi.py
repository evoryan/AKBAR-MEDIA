import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

# Add states for transaksiCash and transaksiOnline
if "var transaksiCash by remember" not in content:
    content = content.replace("var pengeluaran by remember { mutableStateOf(0.0) }",
    "var pengeluaran by remember { mutableStateOf(0.0) }\n    var transaksiCash by remember { mutableStateOf(0.0) }\n    var transaksiOnline by remember { mutableStateOf(0.0) }")

# Add customer fetch logic
fetch_logic = """
                try {
                    val pengeluaranDetails = ApiClient.apiService.getPengeluaranDetail()
                } catch (e: Exception) {}
"""
new_fetch_logic = """
                try {
                    val customersRes = ApiClient.apiService.getCustomers()
                    var cashTotal = 0.0
                    var onlineTotal = 0.0
                    customersRes.forEach { c ->
                        val priceVal = c.price.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
                        val discVal = c.discount.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
                        val finalPrice = if (priceVal > discVal) priceVal - discVal else priceVal
                        
                        if (c.status.contains("LUNAS CASH", ignoreCase = true)) {
                            cashTotal += finalPrice
                        } else if (c.status.contains("LUNAS TRANSFER", ignoreCase = true) || c.status.contains("LUNAS ONLINE", ignoreCase = true)) {
                            onlineTotal += finalPrice
                        }
                    }
                    transaksiCash = cashTotal
                    transaksiOnline = onlineTotal
                } catch (e: Exception) {}
"""
content = content.replace(fetch_logic, fetch_logic + new_fetch_logic)
if new_fetch_logic not in content:
    # try another way to replace
    content = content.replace("pengeluaranDetails = ApiClient.apiService.getPengeluaranDetail()", "pengeluaranDetails = ApiClient.apiService.getPengeluaranDetail()")
    # Just append after categories = res.categories
    content = content.replace("categories = res.categories", "categories = res.categories\n" + new_fetch_logic)

# Replace the UI for Transaksi Cash
target_cash = 'PembukuanItem("Transaksi Cash", "Rp. ${ (categories["Transaksi Cash"] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f).clickable { onNavigateToBilling(1) })'
target_online = 'PembukuanItem("Transaksi Online", "Rp. ${ (categories["Transaksi Online"] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f))'

rep_cash = 'PembukuanItem("Transaksi Cash", "Rp. ${ transaksiCash.let { String.format("%,d", it.toLong()).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f).clickable { onNavigateToBilling(1) })'
rep_online = 'PembukuanItem("Transaksi Online", "Rp. ${ transaksiOnline.let { String.format("%,d", it.toLong()).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f))'

content = content.replace(target_cash, rep_cash)
content = content.replace(target_online, rep_online)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)

print("Patched PembukuanScreen for Transaksi Cash")
