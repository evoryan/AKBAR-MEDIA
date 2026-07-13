import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

# Remove MikrotikProfile from inside ApiService
old_profile_class = """
data class MikrotikProfile(
    val id: String,
    val name: String
)"""
content = content.replace(old_profile_class, "")

# Add it at the top level
content = content.replace("interface ApiService {", "data class MikrotikProfile(\n    val id: String,\n    val name: String\n)\n\ninterface ApiService {")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
