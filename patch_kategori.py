filepath = 'app/src/main/java/com/example/ui/screens/KategoriScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                    if (editItem == null) {
                        val newItem = CategoryItem(
                            id = System.currentTimeMillis().toString(),
                            name = name
                        )
                        categoriesList = categoriesList + newItem
                    } else {"""

replacement = """                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = CategoryItem(
                                    id = "",
                                    name = name
                                )
                                ApiClient.apiService.addCategory(newItem)
                                categoriesList = ApiClient.apiService.getCategories()
                            } catch(e: Exception) {}
                        }
                    } else {"""

content = content.replace(target, replacement)
with open(filepath, 'w') as f:
    f.write(content)
