import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

content = content.replace("val context = LocalContext.current\n    val context = LocalContext.current", "val context = LocalContext.current")

# Remove any extra if they weren't exactly separated by \n
content = re.sub(r'(val context = LocalContext\.current\s+){2,}', 'val context = LocalContext.current\n', content)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)

