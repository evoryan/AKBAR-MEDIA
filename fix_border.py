import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("border = border(1.dp, cardBorder, RoundedCornerShape(12.dp)),", "border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),")
content = content.replace("import androidx.compose.foundation.BorderStroke as border", "")

with open(filepath, 'w') as f:
    f.write(content)
