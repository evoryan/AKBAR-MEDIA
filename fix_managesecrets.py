import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """    val allSecrets = remember {
        mutableStateListOf(
            PPPoESecret("1", "andi_talun", "paket_standar", "Online", "192.168.10.2", "2d 4h"),
            PPPoESecret("2", "budi_kedung", "paket_hemat", "Online", "192.168.10.3", "5h 12m"),
            PPPoESecret("3", "caca_bate", "paket_premium", "Offline"),
            PPPoESecret("4", "deni_talun", "paket_hemat", "Offline"),
            PPPoESecret("5", "eko_bate", "paket_standar", "Disabled")
        )
    }"""

replacement = """    val allSecrets = remember { mutableStateListOf<PPPoESecret>() }
    var networkError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(areaId) {
        try {
            isLoading = true
            networkError = null
            val secrets = com.example.ui.data.remote.ApiClient.apiService.getMikrotikSecrets(areaId)
            allSecrets.clear()
            allSecrets.addAll(secrets)
        } catch(e: Exception) {
            networkError = "Gagal memuat: ${e.message}"
        } finally {
            isLoading = false
        }
    }"""

content = content.replace("fun ManageSecretsScreen(onBack: () -> Unit) {", "fun ManageSecretsScreen(areaId: String, onBack: () -> Unit) {")
content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
