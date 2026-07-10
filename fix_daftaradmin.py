filepath = '/app/applet/app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("""                    } else {
                                            }""", """                    } else {
                        val index = adminList.indexOfFirst { it.id == editItem.id }
                        if (index != -1) {
                            adminList[index] = editItem.copy(name = name, username = username, role = selectedRole)
                        }
                    }""")

with open(filepath, 'w') as f:
    f.write(content)
