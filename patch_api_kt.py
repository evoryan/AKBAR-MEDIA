import re

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """data class PembukuanRequest(
    val type: String,
    val category: String,
    val amount: Double,
    val description: String
)"""

rep = """data class PembukuanRequest(
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
    val created_at: String?
)

data class SetoranRequest(
    val adminName: String,
    val amount: Double
)"""

content = content.replace(target, rep)

target_service = """    @GET("api/pembukuan")
    suspend fun getPembukuan(): PembukuanResponse

    @POST("api/pembukuan")
    suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse"""

rep_service = """    @GET("api/pembukuan")
    suspend fun getPembukuan(): PembukuanResponse

    @GET("api/pembukuan/all")
    suspend fun getAllPembukuan(): List<PembukuanItem>

    @POST("api/pembukuan")
    suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse
    
    @PUT("api/pembukuan/{id}")
    suspend fun updatePembukuan(@Path("id") id: String, @Body request: PembukuanRequest): ApiResponse
    
    @DELETE("api/pembukuan/{id}")
    suspend fun deletePembukuan(@Path("id") id: String): ApiResponse

    @POST("api/setoran")
    suspend fun addSetoran(@Body request: SetoranRequest): ApiResponse"""

content = content.replace(target_service, rep_service)

# Update UangAdminResponse
target_uang = """data class UangAdminResponse(
    val adminName: String,
    val totalAmount: Double,
    val jmlPlggn: Int? = 0
)"""

rep_uang = """data class UangAdminResponse(
    val adminName: String,
    val totalDiterima: Double? = 0.0,
    val setor: Double? = 0.0,
    val pengeluaran: Double? = 0.0,
    val jmlPlggn: Int? = 0
)"""
content = content.replace(target_uang, rep_uang)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
