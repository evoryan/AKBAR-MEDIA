import re

with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.Message
"""

if "import androidx.compose.foundation.clickable" not in content:
    content = content.replace("import androidx.compose.foundation.background\n", "import androidx.compose.foundation.background\n" + imports)

with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'w') as f:
    f.write(content)
