import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """    com.example.ui.data.remote.ApiClient.init(applicationContext)"""

rep = """    com.example.ui.data.remote.ApiClient.init(applicationContext)
    com.example.ui.data.remote.NetworkMonitor.init(applicationContext)"""

if target in content and "NetworkMonitor.init" not in content:
    content = content.replace(target, rep)
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
    print("Patched MainActivity.kt")
else:
    print("Target not found or already patched")
