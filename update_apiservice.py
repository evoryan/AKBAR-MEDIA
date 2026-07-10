import re
filepath = '/app/applet/app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import retrofit2.http.DELETE" not in content:
    content = content.replace("import retrofit2.http.POST", "import retrofit2.http.POST\nimport retrofit2.http.DELETE\nimport retrofit2.http.Path")

if "deleteArea" not in content:
    content = content.replace("suspend fun getAreas(): List<Area>", "suspend fun getAreas(): List<Area>\n\n    @DELETE(\"api/areas/{id}\")\n    suspend fun deleteArea(@Path(\"id\") id: String)")

with open(filepath, 'w') as f:
    f.write(content)
