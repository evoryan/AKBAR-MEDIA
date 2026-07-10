filepath = 'app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('password = "password"', '')
content = content.replace(',\n                                    \n                                )', '\n                                )')

with open(filepath, 'w') as f:
    f.write(content)
