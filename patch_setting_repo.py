with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

content = content.replace('"satriaevo77", "Akbar-Media"', '"evoryan", "AKBAR-MEDIA"')

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
    f.write(content)
