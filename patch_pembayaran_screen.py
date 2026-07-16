import re

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "r") as f:
    content = f.read()

target_dummy = """    val dummyData = listOf(
        PembayaranData("Boseri", "B", Color(0xFFD1C4E9), "09896321543", "09 Jul 2026 - 07:07:00", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Taplikan", "T", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:47", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Kandab", "K", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:42", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Mahkun", "M", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 18:09:17", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Wahyudin", "W", Color(0xFFD1C4E9), "081234567890", "08 Jul 2026 - 07:08:47", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Kasmui", "K", Color(0xFFD1C4E9), "081234567890", "08 Jul 2026 - 06:08:42", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Pardani", "P", Color(0xFFD1C4E9), "081234567890", "08 Jul 2026 - 06:08:33", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun")
    )"""

rep_dummy = """    var realData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getPembayaranHistory()
            realData = res.map { item ->
                PembayaranData(
                    name = item.customer_name ?: "Unknown",
                    initial = (item.customer_name ?: "U").take(1).uppercase(),
                    color = Color(0xFFD1C4E9),
                    phone = item.phone ?: "-",
                    dateTime = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    bulan = "${item.bulan} ${item.tahun}",
                    amount = "Rp. ${String.format("%,d", (item.amount ?: 0.0).toLong()).replace(",", ".")}",
                    area = item.area ?: "-",
                    admin = "Admin By ${item.admin_name ?: "Unknown"}"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

content = content.replace(target_dummy, rep_dummy)

content = content.replace("var displayedData by remember { mutableStateOf(dummyData) }", "var displayedData by remember { mutableStateOf(realData) }")

content = content.replace("val filterOptionsAdmin = if (currentUser?.role == UserRole.COLLECTOR) listOf(currentUser?.name ?: \"All\") else listOf(\"All\") + dummyData.map { it.admin }.distinct()", "val filterOptionsAdmin = if (currentUser?.role == UserRole.COLLECTOR) listOf(\"Admin By ${currentUser?.name}\") else listOf(\"All\") + realData.map { it.admin }.distinct()")
content = content.replace("val filterOptionsArea = listOf(\"All\") + dummyData.map { it.area }.distinct()", "val filterOptionsArea = listOf(\"All\") + realData.map { it.area }.distinct()")

content = content.replace("displayedData = dummyData.filter { item ->", "displayedData = realData.filter { item ->")

content = content.replace("items(dummyData)", "items(displayedData)")
content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport com.example.ui.data.remote.ApiClient\nimport kotlinx.coroutines.launch")

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "w") as f:
    f.write(content)
