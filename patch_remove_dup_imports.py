import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i in [28, 29, 30, 31]: # Note: 0-indexed. line 29 is index 28.
        pass
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.writelines(new_lines)

