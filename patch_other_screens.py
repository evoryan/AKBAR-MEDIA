import re

with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'r') as f:
    content = f.read()
content = content.replace('"PT.Akbar Media Group"', 'com.example.ui.data.SettingsManager.companyName')
with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'r') as f:
    content = f.read()
content = content.replace('name = "PT.Akbar Media Group"', 'name = com.example.ui.data.SettingsManager.companyName')
with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'w') as f:
    f.write(content)
