import re

with open("app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt", "r") as f:
    content = f.read()

# remove password inside text
target1 = """                    var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    OutlinedTextField(
                        value = password,"""

rep1 = """                    OutlinedTextField(
                        value = password,"""

content = content.replace(target1, rep1)

# add password alongside name and username
target2 = """    if (showDialog) {
        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var username by remember { mutableStateOf(editItem?.username ?: "") }"""

rep2 = """    if (showDialog) {
        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var username by remember { mutableStateOf(editItem?.username ?: "") }
        var password by remember { mutableStateOf("") }"""

content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt", "w") as f:
    f.write(content)
