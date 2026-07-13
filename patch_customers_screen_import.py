import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Edit")

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
