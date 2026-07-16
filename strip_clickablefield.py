import re

with open("app/src/main/java/com/example/ui/screens/EditCustomerScreen.kt", "r") as f:
    content = f.read()

pattern = r"@Composable\s*fun ClickableField[\s\S]*?\}\s*\}\s*$"
content = re.sub(pattern, "", content)

with open("app/src/main/java/com/example/ui/screens/EditCustomerScreen.kt", "w") as f:
    f.write(content)
