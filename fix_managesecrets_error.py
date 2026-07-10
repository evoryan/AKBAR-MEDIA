import re

filepath = 'app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {"""

replacement = """            if (networkError != null) {
                Text(networkError!!, color = errorRed, fontSize = 14.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {"""

content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
