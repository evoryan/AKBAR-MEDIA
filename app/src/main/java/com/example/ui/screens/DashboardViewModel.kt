package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DashboardSummaryResponse
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.OfflinePppoeUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardSummaryResponse, val offlinePppoe: List<OfflinePppoeUser> = emptyList()) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        fetchDashboardSummary()
    }

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading
            try {
                var result = ApiClient.apiService.getDashboardSummary()
                try {
                    val customers = ApiClient.apiService.getCustomers().filter { it.status != "TERHAPUS" }
                    val paidCount = customers.count { it.status == "LUNAS CASH" }
                    val unpaidCount = customers.size - paidCount
                    
                    val monthlyRevenue = result.monthlyRevenue
                    result = result.copy(
                        totalCustomers = customers.size,
                        monthlyRevenue = monthlyRevenue,
                        paidCustomers = paidCount,
                        unpaidCustomers = unpaidCount
                    )
                } catch (e: Exception) {
                    // Use original result if customer fetch fails
                }
                var offlineCount: List<OfflinePppoeUser> = emptyList()
                try {
                    val offlineRes = ApiClient.apiService.getPppoeOffline()
                    offlineCount = offlineRes
                } catch (e: Exception) {
                    // Ignore offline PPPoE errors
                }
                _uiState.value = DashboardState.Success(result, offlineCount)
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
