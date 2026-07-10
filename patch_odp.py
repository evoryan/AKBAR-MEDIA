filepath = 'app/src/main/java/com/example/ui/screens/OdpScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                    if (editItem == null) {
                        val newItem = OdpItem(
                            id = System.currentTimeMillis().toString(),
                            name = name,
                            odcId = selectedOdc,
                            portCount = port
                        )
                        odpList = odpList + newItem
                    } else {"""

replacement = """                    if (editItem == null) {
                        coroutineScope.launch {
                            try {
                                val newItem = OdpItem(
                                    id = "",
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port
                                )
                                ApiClient.apiService.addOdp(newItem)
                                odpList = ApiClient.apiService.getOdpList()
                            } catch(e: Exception) {}
                        }
                    } else {"""

content = content.replace(target, replacement)
with open(filepath, 'w') as f:
    f.write(content)
