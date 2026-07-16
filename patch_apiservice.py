import re

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """    @POST("api/mikrotik/secrets/{id}")
    suspend fun addMikrotikSecret(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse"""

rep = """    @POST("api/mikrotik/secrets/{id}")
    suspend fun addMikrotikSecret(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse

    @POST("api/mikrotik/secrets/{id}/disable")
    suspend fun disableMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse
    
    @POST("api/mikrotik/secrets/{id}/enable")
    suspend fun enableMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse
    
    @POST("api/mikrotik/secrets/{id}/remove-active")
    suspend fun removeActiveMikrotikSecret(@Path("id") areaId: String, @Body request: Map<String, String>): ApiResponse"""

if target in content:
    content = content.replace(target, rep)
    with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")

