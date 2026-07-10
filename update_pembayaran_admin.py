import re

filepath = 'app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add UserSession imports
if "import com.example.ui.data.UserSession" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")

if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
    content = content.replace("val primaryCyan = Color(0xFF00FFFF)", "val primaryCyan = Color(0xFF00FFFF)\n    val currentUser by UserSession.currentUser.collectAsState()")

# Handle Collector filter logic
content = content.replace('var filterAdmin by remember { mutableStateOf("All") }', 'var filterAdmin by remember { mutableStateOf(if (currentUser?.role == UserRole.COLLECTOR) currentUser?.name ?: "All" else "All") }')

content = content.replace('val filterOptionsAdmin = listOf("All") + dummyData.map { it.admin }.distinct()', 'val filterOptionsAdmin = if (currentUser?.role == UserRole.COLLECTOR) listOf(currentUser?.name ?: "All") else listOf("All") + dummyData.map { it.admin }.distinct()')

with open(filepath, 'w') as f:
    f.write(content)

