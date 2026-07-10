import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "UserSession.currentUser.value = user",
    "UserSession.saveSession(context, user)"
)

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)
