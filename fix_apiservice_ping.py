with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

content = content.replace("data class PingResponse(val status: String)\n", "")
content = content + "\n\ndata class PingResponse(val status: String)\n"

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
