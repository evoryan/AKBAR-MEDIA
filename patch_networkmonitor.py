import re

with open("app/src/main/java/com/example/ui/data/remote/NetworkMonitor.kt", "r") as f:
    content = f.read()

content = content.replace("                    SyncManager.triggerSync(context)\n", "")

with open("app/src/main/java/com/example/ui/data/remote/NetworkMonitor.kt", "w") as f:
    f.write(content)
