import re

filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("Scaffold(\n        containerColor = neonCyan,", "Scaffold(\n        containerColor = bgMain,")

with open(filepath, 'w') as f:
    f.write(content)

