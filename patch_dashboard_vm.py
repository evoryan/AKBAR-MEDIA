import re

with open('app/src/main/java/com/example/ui/screens/DashboardViewModel.kt', 'r') as f:
    content = f.read()

old_state = """    data class Success(val data: DashboardSummaryResponse) : DashboardState()"""
new_state = """    data class Success(val data: DashboardSummaryResponse, val offlinePppoe: Int = 0) : DashboardState()"""
content = content.replace(old_state, new_state)

old_try = """            try {
                val result = ApiClient.apiService.getDashboardSummary()
                _uiState.value = DashboardState.Success(result)
            } catch (e: Exception) {"""
new_try = """            try {
                val result = ApiClient.apiService.getDashboardSummary()
                var offlineCount = 0
                try {
                    val offlineRes = ApiClient.apiService.getPppoeOffline()
                    offlineCount = offlineRes.offlinePPPoE
                } catch (e: Exception) {
                    // Ignore offline PPPoE errors
                }
                _uiState.value = DashboardState.Success(result, offlineCount)
            } catch (e: Exception) {"""
content = content.replace(old_try, new_try)

with open('app/src/main/java/com/example/ui/screens/DashboardViewModel.kt', 'w') as f:
    f.write(content)

