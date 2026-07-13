import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

# Add PUT methods for ODC and ODP
old_odp = """    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse"""

new_odp = """    @POST("api/odp")
    suspend fun addOdp(@Body item: com.example.ui.data.OdpItem): ApiResponse

    @PUT("api/odc/{id}")
    suspend fun updateOdc(@Path("id") id: String, @Body item: com.example.ui.data.OdcItem): ApiResponse

    @PUT("api/odp/{id}")
    suspend fun updateOdp(@Path("id") id: String, @Body item: com.example.ui.data.OdpItem): ApiResponse"""
    
content = content.replace(old_odp, new_odp)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
