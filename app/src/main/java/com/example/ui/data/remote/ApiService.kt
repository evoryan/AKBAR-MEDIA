package com.example.ui.data.remote


import com.example.ui.data.AdminUser
import com.example.ui.screens.Customer
import com.example.ui.screens.AcsDevice
import com.example.ui.screens.Area
import com.example.ui.data.OdcItem
import com.example.ui.data.OdpItem
import com.example.ui.data.CategoryItem
import com.example.ui.data.InventoryItem
import com.example.ui.data.StockHistory
import retrofit2.http.*
import com.example.data.DashboardSummaryResponse


data class LoginRequest(val username: String, val password: String = "password")

data class PembukuanResponse(
    val pemasukan: Long,
    val pengeluaran: Long,
    val categories: Map<String, Long> = emptyMap()
)

data class PembukuanRequest(
    val type: String,
    val category: String,
    val amount: Double,
    val description: String
)

data class ApiResponse(val message: String, val id: String? = null)

data class MikrotikStatus(val cpuLoad: String, val uptime: String, val activePppoe: String, val offlinePppoe: String)


data class OfflinePppoeUser(val name: String, val lastLogoff: String, val area: String)
data class PaymentRequest(val customerId: String, val adminName: String, val totalAmount: Double)
data class DeleteBillingRequest(val customerId: String)

data class MikrotikProfile(
    val id: String,
    val name: String
)

interface ApiService {
    @GET("api/dashboard/pppoe-offline")
    suspend fun getPppoeOffline(): List<OfflinePppoeUser>

    @POST("api/billing/pay")
    suspend fun payBilling(@Body request: PaymentRequest): ApiResponse

    @POST("api/billing/delete")
    suspend fun deleteBilling(@Body request: DeleteBillingRequest): ApiResponse

    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummaryResponse

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): AdminUser

    @GET("api/customers")
    suspend fun getCustomers(): List<Customer>

    @GET("api/pembukuan")
    suspend fun getPembukuan(): PembukuanResponse

    @POST("api/pembukuan")
    suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse

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

    @DELETE("api/odp/{id}")
    suspend fun deleteOdp(@Path("id") id: String)

    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String)

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

    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse

    @PUT("api/odc/{id}")
    suspend fun updateOdc(@Path("id") id: String, @Body item: com.example.ui.data.OdcItem): ApiResponse

    @PUT("api/odp/{id}")
    suspend fun updateOdp(@Path("id") id: String, @Body item: com.example.ui.data.OdpItem): ApiResponse

    @POST("api/inventory")
    suspend fun addInventory(@Body item: com.example.ui.data.InventoryItem): ApiResponse

    @POST("api/categories")
    suspend fun addCategory(@Body item: com.example.ui.data.CategoryItem): ApiResponse

    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse

    @GET("api/mikrotik/status/{id}")
    suspend fun getMikrotikStatus(@Path("id") id: String): MikrotikStatus

    

    @GET("api/mikrotik/profiles/{id}")
    suspend fun getMikrotikProfiles(@Path("id") id: String): List<MikrotikProfile>

    @GET("api/mikrotik/secrets/{id}")
    suspend fun getMikrotikSecrets(@Path("id") id: String): List<com.example.ui.screens.PPPoESecret>

    @POST("api/mikrotik/secrets/{id}")
    suspend fun addMikrotikSecret(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse


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




data class PingResponse(val status: String)
