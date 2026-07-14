import re

with open("app/src/main/java/com/example/ui/navigation/Routes.kt", "r") as f:
    content = f.read()

content += "\n@Serializable\nobject BackupRestoreRoute\n"

with open("app/src/main/java/com/example/ui/navigation/Routes.kt", "w") as f:
    f.write(content)
