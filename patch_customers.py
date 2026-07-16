import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

old_isolate = """                                com.example.ui.data.remote.ApiClient.apiService.isolateCustomer(customerToIsolir.id)
                                android.widget.Toast.makeText(context, "Berhasil mengisolir pelanggan", android.widget.Toast.LENGTH_SHORT).show()"""

new_isolate = """                                val areas = com.example.ui.data.remote.ApiClient.apiService.getAreas()
                                val targetArea = areas.find { it.name == customerToIsolir.area } ?: areas.firstOrNull()
                                if (targetArea != null) {
                                    val identifier = customerToIsolir.username.ifEmpty { customerToIsolir.id }
                                    com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(
                                        targetArea.id.toString(),
                                        mapOf("secretId" to identifier)
                                    )
                                    android.widget.Toast.makeText(context, "Berhasil men-disable secret pelanggan", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Area tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show()
                                }"""

content = content.replace(old_isolate, new_isolate)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
