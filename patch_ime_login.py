import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("Box(\n        modifier = Modifier\n            .fillMaxSize()\n            .background(bgDark),",
                          "Box(\n        modifier = Modifier\n            .fillMaxSize()\n            .imePadding()\n            .background(bgDark),")

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)
