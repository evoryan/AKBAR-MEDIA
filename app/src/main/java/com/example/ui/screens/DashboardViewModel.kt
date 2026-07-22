package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DashboardSummaryResponse
import com.example.ui.data.local.AppDatabase
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.OfflinePppoeUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val data: DashboardSummaryResponse, val offlinePppoe: List<OfflinePppoeUser> = emptyList()) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        // Reactive Flow subscription: immediately load from DB and update UI instantly upon any sync updates
        viewModelScope.launch {
            combine(
                db.pelangganDao().getAllPelanggan(),
                db.tagihanDao().getAllTagihan(),
                db.statusRouterDao().getAllStatusRouter()
            ) { pelangganList, tagihanList, routerStatusList ->
                val filteredPelanggan = pelangganList.filter { com.example.ui.data.UserSession.isAreaNameAllowed(it.area) && it.status != "TERHAPUS" }
                val paidCount = filteredPelanggan.count { it.status == "LUNAS CASH" }
                val unpaidCount = filteredPelanggan.size - paidCount
                val totalGlobalRevenue = filteredPelanggan.sumOf { it.price.replace(Regex("\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }.toDouble()

                val summaryResponse = DashboardSummaryResponse(
                    totalCustomers = filteredPelanggan.size,
                    monthlyRevenue = totalGlobalRevenue,
                    totalGlobalRevenue = totalGlobalRevenue,
                    activePPPoE = routerStatusList.sumOf { it.active_pppoe.toIntOrNull() ?: 0 },
                    activeHotspot = 0,
                    paidCustomers = paidCount,
                    unpaidCustomers = unpaidCount
                )

                // Populate offline PPPoE logs dynamically
                val offlinePppoeList = routerStatusList.flatMap { status ->
                    val count = status.offline_pppoe.toIntOrNull() ?: 0
                    if (count > 0 && status.status.equals("offline", ignoreCase = true)) {
                        listOf(OfflinePppoeUser(name = "Router Offline: ${status.area_name}", lastLogoff = "Status: ${status.status}", area = status.area_name))
                    } else {
                        emptyList()
                    }
                }

                DashboardState.Success(summaryResponse, offlinePppoeList)
            }.collect { state ->
                _uiState.value = state
            }
        }

        fetchDashboardSummary()
    }

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            try {
                com.example.ui.data.UserSession.getOrFetchAreas()
                val syncResponse = ApiClient.apiService.syncData()
                
                val pelangganList = syncResponse.customers.map { item ->
                    com.example.ui.data.local.PelangganEntity(
                        id = item.id.toIntOrNull() ?: 0,
                        name = item.name ?: "",
                        phone = item.phone ?: "",
                        area = item.area ?: "",
                        address = item.address,
                        username = item.username ?: "",
                        billingDate = item.billingDate ?: "",
                        status = item.status ?: "",
                        price = item.price ?: "",
                        discount = item.discount ?: "",
                        register_date = item.register_date,
                        isolate_date = item.isolate_date,
                        package_name = item.package_name,
                        pppoe_secret = item.pppoe_secret,
                        odp_id = item.odp_id?.toIntOrNull(),
                        odp_port = item.odp_port,
                        additionalCost1 = item.additionalCost1,
                        additionalCost2 = item.additionalCost2
                    )
                }

                val tagihanList = syncResponse.tagihan.map { item ->
                    com.example.ui.data.local.TagihanEntity(
                        id = item.id.toIntOrNull() ?: 0,
                        customer_id = item.customer_id?.toIntOrNull() ?: 0,
                        bulan = item.bulan ?: "",
                        tahun = item.tahun ?: 0,
                        amount = item.amount?.toDoubleOrNull() ?: 0.0,
                        status = item.status ?: "BELUM BAYAR",
                        admin_name = item.admin_name,
                        created_at = item.created_at
                    )
                }

                val routerStatusList = syncResponse.routerStatus.map { item ->
                    com.example.ui.data.local.StatusRouterTerakhirEntity(
                        area_id = item.area_id?.toIntOrNull() ?: 0,
                        area_name = item.area_name ?: "",
                        cpu_load = item.cpu_load ?: "-",
                        uptime = item.uptime ?: "-",
                        active_pppoe = item.active_pppoe ?: "-",
                        offline_pppoe = item.offline_pppoe ?: "-",
                        status = item.status ?: "Offline",
                        updated_at = item.updated_at
                    )
                }

                // Atomic cache update
                db.pelangganDao().deleteAll()
                db.pelangganDao().insertAll(pelangganList)

                db.tagihanDao().deleteAll()
                db.tagihanDao().insertAll(tagihanList)

                db.statusRouterDao().deleteAll()
                db.statusRouterDao().insertAll(routerStatusList)

            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Failed to sync data", e)
            }
        }
    }
}
