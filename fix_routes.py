import re

filepath = 'app/src/main/java/com/example/ui/navigation/Routes.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content + "\n@Serializable\nobject StockBarangRoute\n"

with open(filepath, 'w') as f:
    f.write(content)
