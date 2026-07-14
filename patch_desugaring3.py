import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("coreLibraryDesugaringEnabled = true", "isCoreLibraryDesugaringEnabled = true")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
