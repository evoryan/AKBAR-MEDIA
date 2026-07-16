import re

for filename in ["app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "app/src/main/java/com/example/ui/screens/BillingScreen.kt"]:
    with open(filename, "r") as f:
        content = f.read()

    content = content.replace("androidx.compose.material.icons.filled.Lock", "Icons.Default.Lock")
    if "import androidx.compose.material.icons.filled.Lock" not in content:
        content = content.replace("import androidx.compose.material.icons.filled.Close", "import androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.Lock")

    with open(filename, "w") as f:
        f.write(content)

