import re

with open("app/src/main/java/com/example/ui/data/remote/ApiClient.kt", "r") as f:
    content = f.read()

content = content.replace("                .addInterceptor(OfflineInterceptor(database!!, context))\n", "")
content = content.replace("            // Trigger sync on startup\n            SyncManager.triggerSync(context)\n", "")

with open("app/src/main/java/com/example/ui/data/remote/ApiClient.kt", "w") as f:
    f.write(content)
