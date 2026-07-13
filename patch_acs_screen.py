import re

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'r') as f:
    content = f.read()

old_fun = """fun AcsScreen(onBack: () -> Unit) {"""
new_fun = """fun AcsScreen(onBack: () -> Unit, initialSearchQuery: String = "") {"""
content = content.replace(old_fun, new_fun)

old_var = """    var searchQuery by remember { mutableStateOf("") }"""
new_var = """    var searchQuery by remember { mutableStateOf(initialSearchQuery) }"""
content = content.replace(old_var, new_var)

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'w') as f:
    f.write(content)
