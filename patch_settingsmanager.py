import re

with open('app/src/main/java/com/example/ui/data/SettingsManager.kt', 'r') as f:
    content = f.read()

content = content.replace("private lateinit val prefs: SharedPreferences", "private lateinit var prefs: SharedPreferences")

with open('app/src/main/java/com/example/ui/data/SettingsManager.kt', 'w') as f:
    f.write(content)
