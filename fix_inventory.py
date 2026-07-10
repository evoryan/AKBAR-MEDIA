import re

# Fix InventoryScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/InventoryScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('import com.example.ui.data.MockStockData\n', '')
content = content.replace('val inventoryList by MockStockData.inventory.collectAsState()', '''
    var inventoryList by remember { mutableStateOf<List<com.example.ui.data.InventoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            inventoryList = ApiClient.apiService.getInventory()
        } catch (e: Exception) {
        }
    }
''')

content = content.replace('val categoriesList by MockStockData.categories.collectAsState()', '''
    var categoriesList by remember { mutableStateOf<List<com.example.ui.data.CategoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            categoriesList = ApiClient.apiService.getCategories()
        } catch (e: Exception) {
        }
    }
''')

content = content.replace('MockStockData.inventory.value = MockStockData.inventory.value.filter { it.id != item.id }', 'inventoryList = inventoryList.filter { it.id != item.id }')
content = content.replace('MockStockData.inventory.value = MockStockData.inventory.value + newItem', 'inventoryList = inventoryList + newItem')
content = content.replace('MockStockData.inventory.value = MockStockData.inventory.value.map {', 'inventoryList = inventoryList.map {')

# For MockStockData.history we just remove the assignments to avoid errors since we aren't fully integrating history write API right now
content = re.sub(r'MockStockData\.history\.value\s*=\s*listOf\([^)]*\)\s*\+\s*MockStockData\.history\.value', '', content, flags=re.DOTALL)

with open(filepath, 'w') as f:
    f.write(content)


# Fix KategoriScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/KategoriScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('import com.example.ui.data.MockStockData\n', '')
content = content.replace('val categoriesList by MockStockData.categories.collectAsState()', '''
    var categoriesList by remember { mutableStateOf<List<com.example.ui.data.CategoryItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            categoriesList = ApiClient.apiService.getCategories()
        } catch (e: Exception) {
        }
    }
''')

content = content.replace('MockStockData.categories.value = MockStockData.categories.value.filter { it.id != item.id }', 'categoriesList = categoriesList.filter { it.id != item.id }')
content = content.replace('MockStockData.categories.value = MockStockData.categories.value + newItem', 'categoriesList = categoriesList + newItem')
content = content.replace('MockStockData.categories.value = MockStockData.categories.value.map {', 'categoriesList = categoriesList.map {')

with open(filepath, 'w') as f:
    f.write(content)


# Fix HistoryStockScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/HistoryStockScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('import com.example.ui.data.MockStockData\n', '')
content = content.replace('val historyList by MockStockData.history.collectAsState()', '''
    var historyList by remember { mutableStateOf<List<com.example.ui.data.StockHistory>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            historyList = ApiClient.apiService.getStockHistory()
        } catch (e: Exception) {
        }
    }
''')

with open(filepath, 'w') as f:
    f.write(content)

