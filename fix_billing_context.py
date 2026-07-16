import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

content = content.replace("    val context = LocalContext.current\n    val context = androidx.compose.ui.platform.LocalContext.current", "    val context = androidx.compose.ui.platform.LocalContext.current")

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)

