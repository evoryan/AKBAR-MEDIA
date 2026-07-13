import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

new_method = """    @GET("api/mikrotik/secrets/{id}")
    suspend fun getMikrotikSecrets(@Path("id") id: String): List<com.example.ui.screens.PPPoESecret>

    @POST("api/mikrotik/secrets/{id}")
    suspend fun addMikrotikSecret(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse
"""

content = re.sub(r'@GET\("api/mikrotik/secrets/\{id\}"\)\s+suspend fun getMikrotikSecrets\(@Path\("id"\) id: String\): List<com\.example\.ui\.screens\.PPPoESecret>', new_method, content)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
