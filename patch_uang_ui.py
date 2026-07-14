import re

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport com.example.ui.data.remote.ApiClient")

# Replace dummy data with dynamic fetching
target_list = """    val adminList = listOf(
        AdminData(
            name = com.example.ui.data.SettingsManager.companyName,
            totalDiterima = "Rp. 125.000",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "1"
        ),
        AdminData(
            name = "Fitri",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        ),
        AdminData(
            name = "Al-Mufit",
            totalDiterima = "Rp. 2.450.000",
            setor = "Rp. 0",
            sisa = "Rp. 2.450.000",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "21"
        ),
        AdminData(
            name = "kholis",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        ),
        AdminData(
            name = "admin",
            totalDiterima = "Rp. 0",
            setor = "Rp. 0",
            sisa = "Rp. 0",
            sisaColor = warningYellow,
            persentase = "0%",
            pengeluaran = "Rp. 0",
            persentaseSisa = "0%",
            jmlPlggn = "0"
        )
    )"""

new_list = """    var adminList by remember { mutableStateOf<List<AdminData>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val admins = ApiClient.apiService.getAdmins()
            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList() }
            
            val mapped = admins.map { admin ->
                val amount = uangAdmin.find { it.adminName == admin.name }?.totalAmount ?: 0.0
                val formattedAmount = "Rp. ${String.format("%,d", amount.toLong()).replace(",", ".")}"
                AdminData(
                    name = admin.name,
                    totalDiterima = formattedAmount,
                    setor = "Rp. 0",
                    sisa = formattedAmount,
                    sisaColor = warningYellow,
                    persentase = "100%",
                    pengeluaran = "Rp. 0",
                    persentaseSisa = "100%",
                    jmlPlggn = "-" // count not available yet
                )
            }
            adminList = mapped
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

content = content.replace(target_list, new_list)

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
print("Patched UangDiAdminScreen")
