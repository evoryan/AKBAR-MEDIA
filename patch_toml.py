import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace("[libraries]", "[libraries]\nopencsv = { module = \"com.opencsv:opencsv\", version = \"5.9\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
