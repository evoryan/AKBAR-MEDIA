with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = "data class MikrotikStatus"
replacement = """data class UangAdminResponse(
    val adminName: String,
    val totalAmount: Double
)

data class MikrotikStatus"""

if target in content and "data class UangAdminResponse" not in content:
    content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
print("Added UangAdminResponse class")
