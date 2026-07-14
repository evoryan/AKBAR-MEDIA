import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target = """    val coroutineScope = rememberCoroutineScope()"""

rep = """    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current"""

if "val context = androidx.compose.ui.platform.LocalContext.current" not in content:
    content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
