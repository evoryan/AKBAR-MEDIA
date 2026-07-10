filepath = 'app/src/main/java/com/example/ui/screens/OdcScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                    if (editItem == null) {
                        val newItem = OdcItem(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            location = location
                        )
                        odcList = odcList + newItem
                    } else {"""

replacement = """                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdcItem(
                                    id = "",
                                    name = name,
                                    location = location
                                )
                                ApiClient.apiService.addOdc(newItem)
                                odcList = ApiClient.apiService.getOdcList()
                            } catch(e: Exception) {}
                        }
                    } else {"""

content = content.replace(target, replacement)
with open(filepath, 'w') as f:
    f.write(content)
