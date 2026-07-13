with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

# Remove the previously injected ping method and closing brace
content = content.replace('    @GET("api/ping")\n    suspend fun ping(): PingResponse\n}', "}")

# Find the real end of the interface and inject there
old_end = "suspend fun getRangkuman(): RangkumanResponse\n"
new_end = "suspend fun getRangkuman(): RangkumanResponse\n\n    @GET(\"api/ping\")\n    suspend fun ping(): PingResponse\n"

if old_end in content:
    content = content.replace(old_end, new_end)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
