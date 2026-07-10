with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

import_statement = "import com.example.ui.screens.AcsDevice\n"
if import_statement not in content:
    content = content.replace("import com.example.ui.screens.Area", import_statement + "import com.example.ui.screens.Area")

if "getAcsDevices" not in content:
    new_api = """
    @GET("api/acs/devices")
    suspend fun getAcsDevices(): List<AcsDevice>
    
    @POST("api/acs/devices/{id}/action")
    suspend fun acsAction(@Path("id") id: String, @Body request: Map<String, String>): ApiResponse
"""
    content = content.replace("}", new_api + "\n}")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
