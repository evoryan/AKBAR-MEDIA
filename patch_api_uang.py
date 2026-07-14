with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = "data class AdminUser("
replacement = """data class UangAdminResponse(
    val adminName: String?,
    val totalAmount: Double?
)

data class AdminUser("""

if target in content and "UangAdminResponse" not in content:
    content = content.replace(target, replacement)

target2 = "suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse"
replacement2 = """suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse

    @GET("api/uang-di-admin")
    suspend fun getUangDiAdmin(): List<UangAdminResponse>"""

if target2 in content and "getUangDiAdmin" not in content:
    content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
print("Patched ApiService")
