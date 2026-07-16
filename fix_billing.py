import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

# Fix context duplication
content = content.replace("val context = LocalContext.current\n    val context = LocalContext.current", "val context = LocalContext.current")

# Add Lock import if missing
if "import androidx.compose.material.icons.filled.Lock" not in content:
    content = "import androidx.compose.material.icons.filled.Lock\n" + content

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)

