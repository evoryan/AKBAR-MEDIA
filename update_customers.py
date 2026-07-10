import re

filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.remote.ApiClient\nimport androidx.compose.runtime.LaunchedEffect\nimport kotlinx.coroutines.launch")

# Replace var customers = MockData.customers
target = """var customers = remember { mutableStateOf(listOf(
        Customer("1", "Agus Sutanto", "081234567890", "Talun", "agus.talun", "5", "Aktif", "Rp. 150.000", "0"),
        Customer("2", "Budi Santoso", "081987654321", "Kedung", "budi.kedung", "10", "Isolir", "Rp. 150.000", "0"),
        Customer("3", "Citra Lestari", "085678901234", "Talun", "citra.talun", "15", "Aktif", "Rp. 200.000", "0")
    )) }"""

replacement = """var customers = remember { mutableStateOf<List<Customer>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getCustomers()
            customers.value = res
        } catch (e: Exception) {
            // Handle error
        }
    }"""

content = content.replace(target, replacement)

# Another way in case it's different
target2 = """var customers = remember { mutableStateOf(listOf<Customer>(
        Customer("1", "Agus Sutanto", "081234567890", "Talun", "agus.talun", "5", "Aktif", "Rp. 150.000", "0"),
        Customer("2", "Budi Santoso", "081987654321", "Kedung", "budi.kedung", "10", "Isolir", "Rp. 150.000", "0"),
        Customer("3", "Citra Lestari", "085678901234", "Talun", "citra.talun", "15", "Aktif", "Rp. 200.000", "0")
    )) }"""
content = content.replace(target2, replacement)

# Wait, let's just use regex to replace `var customers = remember { mutableStateOf(listOf(...))`
pattern = re.compile(r"var\s+customers\s*=\s*remember\s*\{\s*mutableStateOf\(.*?\)\s*\}", re.DOTALL)
content = pattern.sub(replacement, content, count=1)

with open(filepath, 'w') as f:
    f.write(content)

