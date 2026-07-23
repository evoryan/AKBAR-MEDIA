package com.example.ui.data.remote


import com.example.ui.data.AdminUser
import com.example.ui.screens.Customer
import com.example.ui.screens.AcsDevice
import com.example.ui.screens.Area
import com.example.ui.data.OdcItem
import com.example.ui.data.RasioItem
import com.example.ui.data.OdpItem
import com.example.ui.data.CategoryItem
import com.example.ui.data.InventoryItem
import com.example.ui.data.StockHistory
import retrofit2.http.*
import com.example.data.DashboardSummaryResponse


data class LoginRequest(val username: String, val password: String = "password")

data class PembukuanResponse(
    val pemasukan: Double,
    val pengeluaran: Double,
    val categories: Map<String, Double> = emptyMap()
)

data class PembukuanRequest(
    val type: String,
    val category: String,
    val amount: Double,
    val description: String
)

data class PembukuanItem(
    val id: String,
    val type: String,
    val category: String?,
    val amount: Double,
    val description: String?,
    val created_at: String?,
    val admin_name: String? = null
)

data class SetoranRequest(
    val adminName: String,
    val amount: Double
)

data class PembayaranHistoryItem(
    val id: String,
    val bulan: String?,
    val tahun: Int?,
    val amount: Double?,
    val admin_name: String?,
    val created_at: String?,
    val customer_name: String?,
    val phone: String?,
    val area: String?
)

data class ApiResponse(val message: String, val id: String? = null)

data class UangAdminResponse(
    val adminName: String,
    val totalDiterima: Double? = 0.0,
    val setor: Double? = 0.0,
    val pengeluaran: Double? = 0.0,
    val jmlPlggn: Int? = 0
)

data class MikrotikStatus(val cpuLoad: String, val uptime: String, val activePppoe: String, val offlinePppoe: String)


data class OfflinePppoeUser(val name: String, val lastLogoff: String, val area: String)
data class PaymentRequest(val customerId: String, val adminName: String, val totalAmount: Double)
data class DeleteBillingRequest(val customerId: String)

data class MikrotikProfile(
    val id: String? = null,
    val name: String
)


data class TrafficResponse(
    @com.squareup.moshi.Json(name = "rx-bits-per-second") val rxBits: String? = null,
    @com.squareup.moshi.Json(name = "tx-bits-per-second") val txBits: String? = null,
    val rx: Long? = null,
    val tx: Long? = null,
    @com.squareup.moshi.Json(name = "rx_byte") val rxByte: Long? = null,
    @com.squareup.moshi.Json(name = "tx_byte") val txByte: Long? = null,
    @com.squareup.moshi.Json(name = "rx_string") val rxString: String? = null,
    @com.squareup.moshi.Json(name = "tx_string") val txString: String? = null
)

data class WaHistoryItem(
    val id: Int,
    val customer_name: String?,
    val phone: String?,
    val message: String?,
    val media_url: String?,
    val status: String?,
    val created_at: String?
)

data class WaStatusResponse(
    val connected: Boolean,
    val status: String,
    val botNumber: String? = null
)

data class WaQrResponse(
    val connected: Boolean,
    val status: String,
    val qr: String? = null,
    val qrImage: String? = null
)

data class SendWaMessageRequest(
    val phone: String,
    val message: String
)

data class SendWaMessageResponse(
    val status: String,
    val info: String? = null,
    val error: String? = null
)

interface ApiService {
    
    @GET("api/tenant/whatsapp/status/{tenantId}")
    suspend fun getWaStatus(@Path("tenantId") tenantId: String): WaStatusResponse

    @GET("api/tenant/whatsapp/qr/{tenantId}")
    suspend fun getWaQr(@Path("tenantId") tenantId: String): WaQrResponse

    @POST("api/tenant/whatsapp/disconnect/{tenantId}")
    suspend fun disconnectWa(@Path("tenantId") tenantId: String): ApiResponse

    @POST("api/tenant/whatsapp/send/{tenantId}")
    suspend fun sendWaMessage(
        @Path("tenantId") tenantId: String,
        @Body request: SendWaMessageRequest
    ): SendWaMessageResponse

    @GET("api/wa/history")
    suspend fun getWaHistory(): List<WaHistoryItem>

    @Multipart
    @POST("api/packages/{id}/image")
    suspend fun uploadPackageImage(
        @Path("id") id: String,
        @Part image: okhttp3.MultipartBody.Part
    ): ApiResponse
    @GET("api/dashboard/pppoe-offline")
    suspend fun getPppoeOffline(): List<OfflinePppoeUser>

    @POST("api/billing/pay")
    suspend fun payBilling(@Body request: PaymentRequest): ApiResponse

    @POST("api/billing/delete")
    suspend fun deleteBilling(@Body request: DeleteBillingRequest): ApiResponse

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummaryResponse

    @GET("api/sync")
    suspend fun syncData(): SyncResponse

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): AdminUser

    @GET("api/customers")
    suspend fun getCustomers(): List<Customer>

    @GET("api/pembukuan")
    suspend fun getPembukuan(): PembukuanResponse

    @GET("api/pembukuan/all")
    suspend fun getAllPembukuan(): List<PembukuanItem>

    @GET("api/pembayaran")
    suspend fun getPembayaranHistory(): List<PembayaranHistoryItem>

    @POST("api/pembukuan")
    suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse
    
    @PUT("api/pembukuan/{id}")
    suspend fun updatePembukuan(@Path("id") id: String, @Body request: PembukuanRequest): ApiResponse
    
    @DELETE("api/pembukuan/{id}")
    suspend fun deletePembukuan(@Path("id") id: String): ApiResponse

    @POST("api/setoran")
    suspend fun addSetoran(@Body request: SetoranRequest): ApiResponse

    @GET("api/uang-di-admin")
    suspend fun getUangDiAdmin(): List<UangAdminResponse>

    @GET("api/pengeluaran")
    suspend fun getPengeluaranDetail(): List<PengeluaranItem>

    @POST("api/pengeluaran")
    suspend fun updatePengeluaranDetail(@Body request: PengeluaranRequest): ApiResponse


    @GET("api/areas")
    suspend fun getAreas(): List<Area>

    @DELETE("api/areas/{id}")
    suspend fun deleteArea(@Path("id") id: String)

    @GET("api/odc")
    suspend fun getOdcList(): List<OdcItem>
    @GET("api/rasio")
    suspend fun getRasioList(): List<RasioItem>

    @GET("api/odp")
    suspend fun getOdpList(): List<OdpItem>

    @GET("api/admins")
    suspend fun getAdmins(): List<AdminUser>

    @GET("api/categories")
    suspend fun getCategories(): List<CategoryItem>

    @GET("api/inventory")
    suspend fun getInventory(): List<InventoryItem>

    @GET("api/stock_history")
    suspend fun getStockHistory(): List<StockHistory>

    @DELETE("api/admins/{id}")
    suspend fun deleteAdmin(@Path("id") id: String)

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String)

    @DELETE("api/inventory/{id}")
    suspend fun deleteInventory(@Path("id") id: String)

    @DELETE("api/odc/{id}")
    suspend fun deleteOdc(@Path("id") id: String)
    @DELETE("api/rasio/{id}")
    suspend fun deleteRasio(@Path("id") id: String)

    @DELETE("api/odp/{id}")
    suspend fun deleteOdp(@Path("id") id: String)

    @POST("api/customers/{id}/isolir")
    suspend fun isolateCustomer(@Path("id") id: String): ApiResponse

    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String)

    
    @PUT("api/customers/{id}")
    suspend fun updateCustomer(@Path("id") id: String, @Body customer: com.example.ui.screens.Customer): ApiResponse

    @POST("api/customers")
    suspend fun addCustomer(@Body customer: com.example.ui.screens.Customer): ApiResponse

    @GET("api/customers/{id}/history")
    suspend fun getCustomerHistory(@Path("id") id: String): List<PaymentHistory>


    @GET("api/packages")
    suspend fun getPackages(): List<com.example.ui.screens.InternetPackage>

    @POST("api/packages")
    suspend fun addPackage(@Body pkg: com.example.ui.screens.InternetPackage): ApiResponse

    @DELETE("api/packages/{id}")
    suspend fun deletePackage(@Path("id") id: String)

    @retrofit2.http.PUT("api/packages/{id}")
    suspend fun updatePackage(@Path("id") id: String, @Body pkg: com.example.ui.screens.InternetPackage): ApiResponse

    @POST("api/areas")
    suspend fun addArea(@Body area: com.example.ui.screens.Area): ApiResponse

    @POST("api/odc")
    suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): ApiResponse
    @POST("api/rasio")
    suspend fun addRasio(@Body item: com.example.ui.data.RasioItem): ApiResponse

    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse

    @PUT("api/odc/{id}")
    suspend fun updateOdc(@Path("id") id: String, @Body item: com.example.ui.data.OdcItem): ApiResponse
    @PUT("api/rasio/{id}")
    suspend fun updateRasio(@Path("id") id: String, @Body item: com.example.ui.data.RasioItem): ApiResponse

    @PUT("api/odp/{id}")
    suspend fun updateOdp(@Path("id") id: String, @Body item: com.example.ui.data.OdpItem): ApiResponse

    @POST("api/inventory")
    suspend fun addInventory(@Body item: com.example.ui.data.InventoryItem): ApiResponse

    @PUT("api/inventory/{id}")
    suspend fun updateInventory(@Path("id") id: String, @Body item: com.example.ui.data.InventoryItem): ApiResponse

    @POST("api/categories")
    suspend fun addCategory(@Body item: com.example.ui.data.CategoryItem): ApiResponse

    @PUT("api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body item: com.example.ui.data.CategoryItem): ApiResponse

    @POST("api/stock_history")
    suspend fun addStockHistory(@Body item: Map<String, String>): ApiResponse

    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse

    @POST("api/admins")
    suspend fun addAdminMap(@Body req: Map<String, String>): ApiResponse

    @PUT("api/admins/{id}")
    suspend fun updateAdminMap(@Path("id") id: String, @Body req: Map<String, String>): ApiResponse

    @PUT("api/admins/{id}")
    suspend fun updateAdmin(@Path("id") id: String, @Body req: Map<String, String>): ApiResponse

    @GET("api/mikrotik/status/{id}")
    suspend fun getMikrotikStatus(@Path("id") id: String): MikrotikStatus

    @GET("api/mikrotik/logs/{id}")
    suspend fun getMikrotikLogs(@Path("id") id: String): List<MikrotikLog>

    

    @GET("api/mikrotik/profiles/{id}")
    suspend fun getMikrotikProfiles(@Path("id") id: String): List<MikrotikProfile>

    @GET("api/mikrotik/secrets/{id}")
    suspend fun getMikrotikSecrets(@Path("id") id: String): List<com.example.ui.screens.PPPoESecret>

    @GET("api/mikrotik/traffic/{id}")
    suspend fun getMikrotikTraffic(@Path("id") areaId: String, @Query("interface") interfaceName: String): List<TrafficResponse>

    @POST("api/mikrotik/secrets/{id}")
    suspend fun addMikrotikSecret(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse

    @POST("api/mikrotik/secrets/{id}/disable")
    suspend fun disableMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse
    
    @POST("api/mikrotik/secrets/{id}/enable")
    suspend fun enableMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse
    
    @POST("api/mikrotik/secrets/{id}/remove-active")
    suspend fun removeActiveMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse

    @POST("api/mikrotik/secrets/{id}/delete")
    suspend fun deleteMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse



    @GET("api/acs/devices")
    suspend fun getAcsDevices(): List<AcsDevice>
    
    @POST("api/acs/devices/{id}/action")
    suspend fun acsAction(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse
    @GET("api/ping")
    suspend fun ping(): PingResponse
}

@kotlinx.serialization.Serializable
data class PengeluaranItem(
    val category: String,
    val amount: Long = 0,
    val description: String = "",
    val updated_at: String = ""
)

@kotlinx.serialization.Serializable
data class PengeluaranRequest(
    val category: String,
    val amount: Long,
    val description: String
)

data class PaymentHistory(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String? = null
)

data class SyncResponse(
    val customers: List<CustomerSyncItem>,
    val tagihan: List<TagihanSyncItem>,
    val routerStatus: List<RouterStatusSyncItem>
)

data class CustomerSyncItem(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
    val area: String? = null,
    val address: String? = null,
    val username: String? = null,
    val billingDate: String? = null,
    val status: String? = null,
    val price: String? = null,
    val discount: String? = null,
    val register_date: String? = null,
    val isolate_date: String? = null,
    val package_name: String? = null,
    val pppoe_secret: String? = null,
    val odp_id: String? = null,
    val odp_port: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null
)

data class TagihanSyncItem(
    val id: String,
    val customer_id: String? = null,
    val bulan: String? = null,
    val tahun: Int? = null,
    val amount: String? = null,
    val status: String? = null,
    val admin_name: String? = null,
    val created_at: String? = null
)

data class RouterStatusSyncItem(
    val id: String,
    val area_id: String? = null,
    val area_name: String? = null,
    val cpu_load: String? = null,
    val uptime: String? = null,
    val active_pppoe: String? = null,
    val offline_pppoe: String? = null,
    val status: String? = null,
    val updated_at: String? = null
)

data class PingResponse(val status: String)

@kotlinx.serialization.Serializable
data class MikrotikLog(
    val id: String = "",
    val time: String = "",
    val topics: String = "",
    val message: String = ""
)
