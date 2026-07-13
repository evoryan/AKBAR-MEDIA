import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("com.example.ui.data.remote.ApiClient.init(applicationContext)", 
                          "com.example.ui.data.remote.ApiClient.init(applicationContext)\n    com.example.ui.data.SettingsManager.init(applicationContext)")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
