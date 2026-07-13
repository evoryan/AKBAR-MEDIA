import re

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("text = \"Akbar Media\"", "text = com.example.ui.data.SettingsManager.companyName")

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(content)
