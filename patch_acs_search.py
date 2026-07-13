import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

old_search = "acsDevice = acsList.find { it.username == c.pppoeSecret }"
new_search = "acsDevice = acsList.find { it.username.trim().equals(c.pppoeSecret?.trim(), ignoreCase = true) }"
content = content.replace(old_search, new_search)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
