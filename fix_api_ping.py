with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    lines = f.readlines()

for i in range(len(lines)-1, -1, -1):
    if lines[i].strip() == "}":
        lines.insert(i, '    @GET("api/ping")\n    suspend fun ping(): PingResponse\n')
        break

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.writelines(lines)
