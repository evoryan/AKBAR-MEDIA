filepath = 'app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                neonCyan = Color(0xFF00FFFF)
            )"""

replacement = """                neonCyan = Color(0xFF00FFFF),
                cardBg = cardBg
            )"""

content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
