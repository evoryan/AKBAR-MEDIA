filepath = 'app/src/main/java/com/example/ui/screens/InventoryScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                    if (editItem == null) {
                        val newItem = InventoryItem(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            categoryId = selectedCategory,
                            stock = newStock
                        )
                        inventoryList = inventoryList + newItem
                        
                            } else {"""

replacement = """                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = InventoryItem(
                                    id = "",
                                    name = name,
                                    categoryId = selectedCategory,
                                    stock = newStock
                                )
                                ApiClient.apiService.addInventory(newItem)
                                inventoryList = ApiClient.apiService.getInventory()
                            } catch(e: Exception) {}
                        }
                    } else {"""

content = content.replace(target, replacement)
with open(filepath, 'w') as f:
    f.write(content)
