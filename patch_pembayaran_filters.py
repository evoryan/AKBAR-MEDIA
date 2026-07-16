import re

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "r") as f:
    content = f.read()

target = """    val filterOptionsDariSampai = listOf("hari ini", "kemarin", "minggu ini", "bulan ini")
    val filterOptionsAdmin = if (currentUser?.role == UserRole.COLLECTOR) listOf("Admin By ${currentUser?.name}") else listOf("All") + realData.map { it.admin }.distinct()
    val filterOptionsArea = listOf("All") + realData.map { it.area }.distinct()"""

rep = """    val filterOptionsDariSampai = listOf("hari ini", "kemarin", "minggu ini", "bulan ini")
    val filterOptionsAdmin by remember(realData, currentUser) {
        derivedStateOf {
            if (currentUser?.role == UserRole.COLLECTOR) listOf("Admin By ${currentUser?.name}") else listOf("All") + realData.map { it.admin }.distinct()
        }
    }
    val filterOptionsArea by remember(realData) {
        derivedStateOf {
            listOf("All") + realData.map { it.area }.distinct()
        }
    }"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "w") as f:
    f.write(content)
