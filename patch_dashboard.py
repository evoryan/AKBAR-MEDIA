import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_menu = 'MenuItem(icon = Icons.Default.Report, title = "Jaringan", tint = textErrorPrimary, onClick = onNavigateToJaringan)'
new_menu = 'MenuItem(icon = Icons.Default.AccountTree, title = "Jaringan", tint = primaryBg, onClick = onNavigateToJaringan)'

content = content.replace(old_menu, new_menu)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
