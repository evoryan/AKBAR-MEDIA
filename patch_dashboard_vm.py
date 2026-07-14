import re

with open("app/src/main/java/com/example/ui/screens/DashboardViewModel.kt", "r") as f:
    content = f.read()

target = """                    val unpaidCount = customers.size - paidCount
                    val monthlyRevenue = customers.filter { it.status == "LUNAS CASH" }.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }.toDouble()
                    result = result.copy("""

rep = """                    val unpaidCount = customers.size - paidCount
                    
                    var additionalPemasukan = 0.0
                    try {
                        val pembukuan = ApiClient.apiService.getPembukuan()
                        additionalPemasukan = pembukuan.pemasukan
                    } catch (e: Exception) {}

                    val monthlyRevenue = customers.filter { it.status == "LUNAS CASH" }.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }.toDouble() + additionalPemasukan
                    
                    result = result.copy("""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/DashboardViewModel.kt", "w") as f:
    f.write(content)
