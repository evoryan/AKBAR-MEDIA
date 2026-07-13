import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

content = content.replace('    @GET("api/ping")\n    suspend fun ping(): PingResponse', "")

ping = """    @GET("api/ping")
    suspend fun ping(): PingResponse
}"""

content = content.replace("}", ping, 1)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
