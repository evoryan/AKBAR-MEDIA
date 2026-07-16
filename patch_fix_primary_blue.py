import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

content = content.replace("tint = primaryBlue", "tint = Color(0xFF2196F3)")
content = content.replace("androidx.compose.material.icons.Icons", "androidx.compose.material.icons.Icons")

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
