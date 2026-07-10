filepath = 'app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

endpoints = """
    @POST("api/odc")
    suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): com.example.ui.data.OdcItem

    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): com.example.ui.data.OdpItem

    @POST("api/inventory")
    suspend fun addInventory(@Body item: com.example.ui.data.InventoryItem): com.example.ui.data.InventoryItem

    @POST("api/categories")
    suspend fun addCategory(@Body item: com.example.ui.data.CategoryItem): com.example.ui.data.CategoryItem

    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): com.example.ui.data.AdminUser
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + endpoints + "\n}"

with open(filepath, 'w') as f:
    f.write(content)
