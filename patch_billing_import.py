import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

if "import android.widget.Toast" not in content:
    content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport android.widget.Toast")

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
