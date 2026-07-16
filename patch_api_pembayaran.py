import re

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """data class SetoranRequest(
    val adminName: String,
    val amount: Double
)"""

rep = """data class SetoranRequest(
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
)"""

content = content.replace(target, rep)

target_service = """    @GET("api/pembukuan/all")
    suspend fun getAllPembukuan(): List<PembukuanItem>"""

rep_service = """    @GET("api/pembukuan/all")
    suspend fun getAllPembukuan(): List<PembukuanItem>

    @GET("api/pembayaran")
    suspend fun getPembayaranHistory(): List<PembayaranHistoryItem>"""

content = content.replace(target_service, rep_service)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
