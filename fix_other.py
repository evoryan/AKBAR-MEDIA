import re

filepath = '/app/applet/app/src/main/java/com/example/ui/screens/InventoryScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = re.sub(r'MockStockData\.history\.value.*?\n.*?\}\)\s*\+\s*MockStockData\.history\.value', '', content, flags=re.DOTALL)
content = re.sub(r'MockStockData\.history\.value\s*=\s*listOf\([^)]*\)\s*\+\s*MockStockData\.history\.value', '', content, flags=re.DOTALL)
content = content.replace("MockStockData.history.value = listOf(StockHistory(", "")
content = content.replace(")) + MockStockData.history.value", "")

with open(filepath, 'w') as f:
    f.write(content)

# Fix MikrotikScreen.kt
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
content = content.replace('import com.example.ui.data.MockData\n', '')
content = content.replace('val areas = MockData.areas.filter { it.mikrotikApiAddress.isNotEmpty() }', '''
    var areas by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
            areas = res.filter { it.routerIp.isNotEmpty() }
        } catch(e: Exception) {
        }
    }
''')
content = content.replace('mikrotikApiAddress', 'routerIp')
with open(filepath, 'w') as f:
    f.write(content)


# Fix AcsScreen.kt
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/AcsScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
content = content.replace('val areas = com.example.ui.data.MockData.areas', '''
    var areas by remember { mutableStateOf<List<com.example.ui.screens.Area>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            areas = com.example.ui.data.remote.ApiClient.apiService.getAreas()
        } catch(e: Exception) {
        }
    }
''')
with open(filepath, 'w') as f:
    f.write(content)

# Fix DashboardScreen.kt
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
content = content.replace('import com.example.ui.data.MockData\n', '')
with open(filepath, 'w') as f:
    f.write(content)

# Fix AreaScreen.kt
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
content = content.replace('import com.example.ui.data.MockData\n', '')
with open(filepath, 'w') as f:
    f.write(content)

