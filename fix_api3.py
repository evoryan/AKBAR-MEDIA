import re
filepath = 'app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

endpoints = """
    @GET("api/packages")
    suspend fun getPackages(): List<com.example.ui.screens.InternetPackage>

    @POST("api/packages")
    suspend fun addPackage(@Body pkg: com.example.ui.screens.InternetPackage): com.example.ui.screens.InternetPackage

    @DELETE("api/packages/{id}")
    suspend fun deletePackage(@Path("id") id: String)

    @POST("api/areas")
    suspend fun addArea(@Body area: com.example.ui.screens.Area): com.example.ui.screens.Area
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + endpoints + "\n}"

with open(filepath, 'w') as f:
    f.write(content)
