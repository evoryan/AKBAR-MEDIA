import re

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "r") as f:
    content = f.read()

# Remove the incorrectly placed import
if content.startswith("import kotlinx.coroutines.launch\n"):
    content = content[len("import kotlinx.coroutines.launch\n"):]

# Add it after the package declaration
content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport kotlinx.coroutines.launch")

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "w") as f:
    f.write(content)
