import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

new_method = """
data class MikrotikProfile(
    val id: String,
    val name: String
)

    @GET("api/mikrotik/profiles/{id}")
    suspend fun getMikrotikProfiles(@Path("id") id: String): List<MikrotikProfile>
"""

content = content.replace("    @GET(\"api/mikrotik/secrets/{id}\")", new_method + "\n    @GET(\"api/mikrotik/secrets/{id}\")")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
