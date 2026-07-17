import re

with open('app/src/main/java/com/example/ui/screens/JaringanScreen.kt', 'r') as f:
    content = f.read()

old_effect = """    LaunchedEffect(Unit) {
        try {
            odcList = ApiClient.apiService.getOdcList()
            odpList = ApiClient.apiService.getOdpList()
            rasioList = ApiClient.apiService.getRasioList()
            customerList = ApiClient.apiService.getCustomers()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data jaringan", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }"""

new_effect = """    LaunchedEffect(Unit) {
        try {
            odcList = ApiClient.apiService.getOdcList()
            odpList = ApiClient.apiService.getOdpList()
            customerList = ApiClient.apiService.getCustomers()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data jaringan", Toast.LENGTH_SHORT).show()
        }
        try {
            rasioList = ApiClient.apiService.getRasioList()
        } catch (e: Exception) {
            rasioList = emptyList()
        }
        isLoading = false
    }"""

content = content.replace(old_effect, new_effect)

with open('app/src/main/java/com/example/ui/screens/JaringanScreen.kt', 'w') as f:
    f.write(content)
