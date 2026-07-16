import re

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "r") as f:
    content = f.read()

target = """                            onDisable = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Disabled")
                            },
                            onEnable = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline")
                            },
                            onRemoveActive = {
                                val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline", ipAddress = "", uptime = "")
                            }"""

rep = """                            onDisable = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(areaId, mapOf("secretId" to secret.name))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Disabled")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal disable secret", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onEnable = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.enableMikrotikSecret(areaId, mapOf("secretId" to secret.name))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal enable secret", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onRemoveActive = {
                                coroutineScope.launch {
                                    try {
                                        com.example.ui.data.remote.ApiClient.apiService.removeActiveMikrotikSecret(areaId, mapOf("secretName" to secret.name))
                                        val idx = allSecrets.indexOfFirst { it.id == secret.id }
                                        if (idx != -1) allSecrets[idx] = secret.copy(status = "Offline", ipAddress = "", uptime = "")
                                    } catch(e: Exception) {
                                        android.widget.Toast.makeText(context, "Gagal remove active", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }"""

if target in content:
    content = content.replace(target, rep)
else:
    print("Target not found")

target2 = """    val coroutineScope = rememberCoroutineScope()"""
rep2 = """    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current"""

if target2 in content:
    content = content.replace(target2, rep2)
else:
    print("Target 2 not found")

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "w") as f:
    f.write(content)

