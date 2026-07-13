import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

new_block = """    LaunchedEffect(selectedArea) {
        if (selectedArea != null) {
            selectedSecret = null
            isLoadingSecrets = true
            
            // Fetch Secrets
            try {
                secrets = ApiClient.apiService.getMikrotikSecrets(selectedArea!!.id)
            } catch (e: Exception) {
                secrets = emptyList()
            }
            
            // Fetch Profiles
            try {
                profiles = ApiClient.apiService.getMikrotikProfiles(selectedArea!!.id)
            } catch (e: Exception) {
                profiles = listOf(
                    com.example.ui.data.remote.MikrotikProfile("1", "default"),
                    com.example.ui.data.remote.MikrotikProfile("2", "1M"),
                    com.example.ui.data.remote.MikrotikProfile("3", "2M"),
                    com.example.ui.data.remote.MikrotikProfile("4", "3M"),
                    com.example.ui.data.remote.MikrotikProfile("5", "4M"),
                    com.example.ui.data.remote.MikrotikProfile("6", "5M"),
                    com.example.ui.data.remote.MikrotikProfile("7", "10M"),
                    com.example.ui.data.remote.MikrotikProfile("8", "20M")
                )
            }
            
            isLoadingSecrets = false
        }
    }"""

content = re.sub(r'    LaunchedEffect\(selectedArea\) \{[\s\S]*?isLoadingSecrets = false\n            \}\n        \}\n    \}', new_block, content)

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
