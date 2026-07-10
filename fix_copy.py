filepath = '/app/applet/app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("adminList[index] = editItem?.copy(name = name, username = username, role = selectedRole)", """val current = editItem
                            if (current != null) {
                                adminList[index] = current.copy(name = name, username = username, role = selectedRole)
                            }""")

with open(filepath, 'w') as f:
    f.write(content)

