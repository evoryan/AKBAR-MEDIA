import re

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse"""

rep = """    @POST("api/admins")
    suspend fun addAdmin(@Body item: com.example.ui.data.AdminUser): ApiResponse

    @POST("api/admins")
    suspend fun addAdminMap(@Body req: Map<String, String>): ApiResponse

    @PUT("api/admins/{id}")
    suspend fun updateAdminMap(@Path("id") id: String, @Body req: Map<String, String>): ApiResponse"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
