import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

old_code = """                    } else {
                        odcList = odcList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, location = location, portCount = portCount.toIntOrNull() ?: 0, portInput = portInput) else it
                        }
                    }"""

new_code = """                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedItem = OdcItem(
                                    id = editItem!!.id,
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput
                                )
                                ApiClient.apiService.updateOdc(updatedItem.id, updatedItem)
                                odcList = ApiClient.apiService.getOdcList()
                            } catch (e: Exception) {}
                        }
                    }"""

content = content.replace(old_code, new_code)
with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

old_code_odp = """                    } else {
                        odpList = odpList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, odcId = selectedOdc, portCount = port, portInput = portInput) else it
                        }
                    }"""

new_code_odp = """                    } else {
                        coroutineScope.launch {
                            try {
                                val updatedItem = OdpItem(
                                    id = editItem!!.id,
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port,
                                    portInput = portInput
                                )
                                ApiClient.apiService.updateOdp(updatedItem.id, updatedItem)
                                odpList = ApiClient.apiService.getOdpList()
                            } catch (e: Exception) {}
                        }
                    }"""

content = content.replace(old_code_odp, new_code_odp)
with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
