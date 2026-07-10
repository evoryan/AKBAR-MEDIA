import re

filepath = 'app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add ApiResponse class
if "data class ApiResponse" not in content:
    content = content.replace("interface ApiService {", "data class ApiResponse(val message: String, val id: String? = null)\n\ninterface ApiService {")

# Replace return types of all add endpoints
content = re.sub(r'suspend fun addCustomer\(@Body .*?\): .*', r'suspend fun addCustomer(@Body customer: com.example.ui.screens.Customer): ApiResponse', content)
content = re.sub(r'suspend fun addPackage\(@Body .*?\): .*', r'suspend fun addPackage(@Body pkg: com.example.ui.screens.InternetPackage): ApiResponse', content)
content = re.sub(r'suspend fun addArea\(@Body .*?\): .*', r'suspend fun addArea(@Body area: com.example.ui.screens.Area): ApiResponse', content)
content = re.sub(r'suspend fun addOdc\(@Body .*?\): .*', r'suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): ApiResponse', content)
content = re.sub(r'suspend fun addOdp\(@Body .*?\): .*', r'suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse', content)
content = re.sub(r'suspend fun addInventory\(@Body .*?\): .*', r'suspend fun addInventory(@Body item: com.example.ui.data.InventoryItem): ApiResponse', content)
content = re.sub(r'suspend fun addCategory\(@Body .*?\): .*', r'suspend fun addCategory(@Body item: com.example.ui.data.CategoryItem): ApiResponse', content)
content = re.sub(r'suspend fun addAdmin\(@Body .*?\): .*', r'suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse', content)

with open(filepath, 'w') as f:
    f.write(content)
