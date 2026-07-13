import re

with open('app/src/main/java/com/example/ui/screens/UpdateProfilScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("            modifier = Modifier\n                .fillMaxSize()\n                .padding(innerPadding)\n                .padding(16.dp),",
                          "            modifier = Modifier\n                .fillMaxSize()\n                .padding(innerPadding)\n                .imePadding()\n                .padding(16.dp),")

with open('app/src/main/java/com/example/ui/screens/UpdateProfilScreen.kt', 'w') as f:
    f.write(content)
