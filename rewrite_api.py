api_service_content = """package com.example.ui.data.remote

import com.example.ui.data.AdminUser
import com.example.ui.screens.Customer
import com.example.ui.screens.AcsDevice
import com.example.ui.screens.Area
import com.example.ui.data.OdcItem
import com.example.ui.data.OdpItem
import com.example.ui.data.CategoryItem
import com.example.ui.data.InventoryItem
import com.example.ui.data.StockHistory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body

data class LoginRequest(val username: String, val password: String = "password")

data class PembukuanResponse(
    val pemasukan: Long,
    val pengeluaran: Long
)

data class ApiResponse(val message: String, val id: String? = null)

data class MikrotikStatus(val cpuLoad: String, val uptime: String, val activePppoe: String, val offlinePppoe: String)

interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): AdminUser

    @GET("api/customers")
    suspend fun getCustomers(): List<Customer>

    @GET("api/pembukuan")
    suspend fun getPembukuan(): PembukuanResponse

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

    @GET("api/packages")
    suspend fun getPackages(): List<com.example.ui.screens.InternetPackage>

    @POST("api/packages")
    suspend fun addPackage(@Body pkg: com.example.ui.screens.InternetPackage): ApiResponse

    @DELETE("api/packages/{id}")
    suspend fun deletePackage(@Path("id") id: String)

    @POST("api/areas")
    suspend fun addArea(@Body area: com.example.ui.screens.Area): ApiResponse

    @POST("api/odc")
    suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): ApiResponse

    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse

    @POST("api/inventory")
    suspend fun addInventory(@Body item: com.example.ui.data.InventoryItem): ApiResponse

    @POST("api/categories")
    suspend fun addCategory(@Body item: com.example.ui.data.CategoryItem): ApiResponse

    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse

    @GET("api/mikrotik/status/{id}")
    suspend fun getMikrotikStatus(@Path("id") id: String): MikrotikStatus

    @GET("api/mikrotik/secrets/{id}")
    suspend fun getMikrotikSecrets(@Path("id") id: String): List<com.example.ui.screens.PPPoESecret>

    @GET("api/acs/devices")
    suspend fun getAcsDevices(): List<AcsDevice>
    
    @POST("api/acs/devices/{id}/action")
    suspend fun acsAction(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse
}
"""

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(api_service_content)

