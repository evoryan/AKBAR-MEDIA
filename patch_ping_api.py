import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

models = """data class PingResponse(val status: String)
"""

if "PingResponse" not in content:
    content = content.replace("package com.example.ui.data.remote\n\n", "package com.example.ui.data.remote\n\n" + models)

ping_method = """
    @GET("api/ping")
    suspend fun ping(): PingResponse
"""

if "suspend fun ping()" not in content:
    content = content + ping_method

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
