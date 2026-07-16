import re

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "r") as f:
    content = f.read()

target_dummy = """    val dummyData = listOf(
        PembayaranData("Boseri", "B", Color(0xFFD1C4E9), "09896321543", "09 Jul 2026 - 07:07:00", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Taplikan", "T", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:47", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Kandab", "K", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 19:09:42", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Mahkun", "M", Color(0xFFD1C4E9), "6.28134E12", "08 Jul 2026 - 18:09:17", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Wahyudin", "W", Color(0xFFD1C4E9), "081234567890", "08 Jul 2026 - 07:08:47", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Sutris rt 01", "S", Color(0xFFD1C4E9), "085012345678", "08 Jul 2026 - 07:08:36", "Jul 2026", "Rp. 100.000", "Area Kluwihan Jimbaran", "Admin By Purwoto"),
        PembayaranData("Toin", "T", Color(0xFFD1C4E9), "6289525686652", "07 Jul 2026 - 19:16:47", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun"),
        PembayaranData("Turmi", "T", Color(0xFFD1C4E9), "089870339635", "07 Jul 2026 - 18:23:45", "Jul 2026", "Rp. 100.000", "Area Talun", "Admin By Budi Talun")
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
                    initialBg = Color(0xFFD1C4E9),
                    phone = item.phone ?: "-",
                    payDate = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    payMonth = "${item.bulan} ${item.tahun}",
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

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "w") as f:
    f.write(content)
