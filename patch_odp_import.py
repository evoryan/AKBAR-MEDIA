import re
with open("app/src/main/java/com/example/ui/screens/OdpScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.ArrowDropDown" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.ArrowDropDown")

with open("app/src/main/java/com/example/ui/screens/OdpScreen.kt", "w") as f:
    f.write(content)
