import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

if "import com.example.ui.data.RasioItem" not in content:
    content = content.replace("import com.example.ui.data.OdcItem", "import com.example.ui.data.OdcItem\nimport com.example.ui.data.RasioItem")

if "fun getRasioList" not in content:
    content = content.replace("suspend fun getOdcList(): List<OdcItem>", "suspend fun getOdcList(): List<OdcItem>\n    @GET(\"api/rasio\")\n    suspend fun getRasioList(): List<RasioItem>")

if "fun addRasio" not in content:
    content = content.replace("suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): ApiResponse", "suspend fun addOdc(@Body item: com.example.ui.data.OdcItem): ApiResponse\n    @POST(\"api/rasio\")\n    suspend fun addRasio(@Body item: com.example.ui.data.RasioItem): ApiResponse")

if "fun updateRasio" not in content:
    content = content.replace("suspend fun updateOdc(@Path(\"id\") id: String, @Body item: com.example.ui.data.OdcItem): ApiResponse", "suspend fun updateOdc(@Path(\"id\") id: String, @Body item: com.example.ui.data.OdcItem): ApiResponse\n    @PUT(\"api/rasio/{id}\")\n    suspend fun updateRasio(@Path(\"id\") id: String, @Body item: com.example.ui.data.RasioItem): ApiResponse")

if "fun deleteRasio" not in content:
    content = content.replace("suspend fun deleteOdc(@Path(\"id\") id: String)", "suspend fun deleteOdc(@Path(\"id\") id: String)\n    @DELETE(\"api/rasio/{id}\")\n    suspend fun deleteRasio(@Path(\"id\") id: String)")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
