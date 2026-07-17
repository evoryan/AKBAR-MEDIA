import re

with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'r') as f:
    content = f.read()

if "object JaringanRoute" not in content:
    content = content + "\n@Serializable\nobject JaringanRoute"

if "object RasioRoute" not in content:
    content = content + "\n@Serializable\nobject RasioRoute"

with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'w') as f:
    f.write(content)
