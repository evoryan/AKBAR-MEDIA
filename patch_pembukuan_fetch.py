with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

import re

# Add state variable
content = content.replace("var categories by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }", "var categories by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }\n    var pengeluaranDetails by remember { mutableStateOf<List<com.example.ui.data.remote.PengeluaranItem>>(emptyList()) }")

# Update fetchData
fetch_data_new = """    fun fetchData() {
        coroutineScope.launch {
            try {
                val res = ApiClient.apiService.getPembukuan()
                pemasukan = res.pemasukan
                pengeluaran = res.pengeluaran
                categories = res.categories
                
                try {
                    pengeluaranDetails = ApiClient.apiService.getPengeluaranDetail()
                } catch (e: Exception) {}
            } catch (e: Exception) {
            }
        }
    }"""
content = re.sub(r"    fun fetchData\(\) \{.*?(?=\n    LaunchedEffect)", fetch_data_new, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
