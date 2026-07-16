import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.Block", "androidx.compose.material.icons.filled.Lock")
content = content.replace("Icons.Default.Block", "androidx.compose.material.icons.filled.Lock")
content = content.replace("val coroutineScope = rememberCoroutineScope()", "val coroutineScope = rememberCoroutineScope()\n    val context = LocalContext.current")

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Default.Block", "androidx.compose.material.icons.filled.Lock")
content = content.replace("Icons.Default.Block", "androidx.compose.material.icons.filled.Lock")
# Check if context is defined in BillingScreen
if "val context = LocalContext.current" not in content:
    content = content.replace("val coroutineScope = rememberCoroutineScope()", "val coroutineScope = rememberCoroutineScope()\n    val context = LocalContext.current")

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)

