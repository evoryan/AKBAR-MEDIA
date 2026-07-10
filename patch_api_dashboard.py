import re
with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

content = content.replace("data class PppoeOfflineResponse(val offlinePPPoE: Int)", "data class OfflinePppoeUser(val name: String, val lastLogoff: String, val area: String)")
content = content.replace("suspend fun getPppoeOffline(): PppoeOfflineResponse", "suspend fun getPppoeOffline(): List<OfflinePppoeUser>")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/DashboardViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.ui.data.remote.ApiClient", "import com.example.ui.data.remote.ApiClient\nimport com.example.ui.data.remote.OfflinePppoeUser")
content = content.replace("val offlinePppoe: Int = 0", "val offlinePppoe: List<OfflinePppoeUser> = emptyList()")
content = content.replace("var offlineCount = 0", "var offlineCount: List<OfflinePppoeUser> = emptyList()")
content = content.replace("offlineCount = offlineRes.offlinePPPoE", "offlineCount = offlineRes")

with open('app/src/main/java/com/example/ui/screens/DashboardViewModel.kt', 'w') as f:
    f.write(content)
