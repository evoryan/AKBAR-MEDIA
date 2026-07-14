import re

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """data class UangAdminResponse(
    val adminName: String,
    val totalAmount: Double
)"""

rep = """data class UangAdminResponse(
    val adminName: String,
    val totalAmount: Double,
    val jmlPlggn: Int? = 0
)"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)
