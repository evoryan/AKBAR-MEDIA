import re

with open("app/src/main/java/com/example/ui/screens/DashboardViewModel.kt", "r") as f:
    content = f.read()

pattern = r"""                try \{
                    val customers = ApiClient\.apiService\.getCustomers\(\)\.filter \{ it\.status != "TERHAPUS" \}
                    val paidCount = customers\.count \{ it\.status == "LUNAS CASH" \}
                    val unpaidCount = customers\.size - paidCount
                    
                    val monthlyRevenue = result\.monthlyRevenue
                    result = result\.copy\(
                        totalCustomers = customers\.size,
                        monthlyRevenue = monthlyRevenue,
                        paidCustomers = paidCount,
                        unpaidCustomers = unpaidCount
                    \)
                \} catch \(e: Exception\) \{"""

replacement = """                try {
                    val customers = ApiClient.apiService.getCustomers().filter { it.status != "TERHAPUS" }
                    val paidCount = customers.count { it.status == "LUNAS CASH" }
                    val unpaidCount = customers.size - paidCount
                    
                    val globalRev = customers.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }.toDouble()
                    
                    val monthlyRevenue = result.monthlyRevenue
                    result = result.copy(
                        totalCustomers = customers.size,
                        monthlyRevenue = monthlyRevenue,
                        totalGlobalRevenue = globalRev,
                        paidCustomers = paidCount,
                        unpaidCustomers = unpaidCount
                    )
                } catch (e: Exception) {"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/DashboardViewModel.kt", "w") as f:
    f.write(content)

