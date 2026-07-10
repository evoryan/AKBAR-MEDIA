import re

filepath = 'app/src/main/java/com/example/ui/screens/PembukuanScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add imports for ApiClient
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import com.example.ui.data.UserRole", "import com.example.ui.data.UserRole\nimport com.example.ui.data.remote.ApiClient\nimport kotlinx.coroutines.launch\nimport androidx.compose.runtime.LaunchedEffect")

# Remove hardcoded values and replace with state
target = """val currentUser by UserSession.currentUser.collectAsState()"""
replacement = """val currentUser by UserSession.currentUser.collectAsState()
    var pemasukan by remember { mutableStateOf(0L) }
    var pengeluaran by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getPembukuan()
            pemasukan = res.pemasukan
            pengeluaran = res.pengeluaran
        } catch (e: Exception) {
            // Error handling ignored for now
        }
    }
"""

content = content.replace(target, replacement)

# Replace the text "Rp. 2.575.000" and "Rp. 0" with formatted pemasukan/pengeluaran
content = content.replace('Text("Rp. 2.575.000", color = successGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)', 'Text("Rp. ${String.format("%,d", pemasukan).replace(",", ".")}", color = successGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)')
content = content.replace('Text("Rp. 0", color = errorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)', 'Text("Rp. ${String.format("%,d", pengeluaran).replace(",", ".")}", color = errorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)')

with open(filepath, 'w') as f:
    f.write(content)
