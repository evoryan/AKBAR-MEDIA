import re

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'r') as f:
    content = f.read()

# Fix InternetPackage instantiation
content = content.replace("pppoeProfile = \"default\"", "")
content = content.replace("description = description,\n", "description = description\n")

# Import ApiClient correctly
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport com.example.ui.data.remote.ApiClient")

# Double check if I need to remove trailing commas from InternetPackage instantiation
content = content.replace("description = description,", "description = description")

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'w') as f:
    f.write(content)
