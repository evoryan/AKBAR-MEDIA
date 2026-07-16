import re

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "r") as f:
    content = f.read()

target_vars = """    var filterDari by remember { mutableStateOf("hari ini") }
    var filterSampai by remember { mutableStateOf("hari ini") }
    var filterAdmin by remember { mutableStateOf(if (currentUser?.role == UserRole.COLLECTOR) currentUser?.name ?: "All" else "All") }
    var filterArea by remember { mutableStateOf("All") }

    var displayedData by remember { mutableStateOf(realData) }"""

content = content.replace(target_vars, "")

target_insert = """    var realData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }"""

replacement_insert = """    var realData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }
    var filterDari by remember { mutableStateOf("hari ini") }
    var filterSampai by remember { mutableStateOf("hari ini") }
    var filterAdmin by remember { mutableStateOf(if (currentUser?.role == UserRole.COLLECTOR) "Admin By ${currentUser?.name}" else "All") }
    var filterArea by remember { mutableStateOf("All") }
    var displayedData by remember { mutableStateOf<List<PembayaranData>>(emptyList()) }"""

content = content.replace(target_insert, replacement_insert)

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "w") as f:
    f.write(content)
