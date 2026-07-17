import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

if "onNavigateToJaringan: () -> Unit =" not in content:
    content = content.replace("onNavigateToSetting: () -> Unit = {}", "onNavigateToSetting: () -> Unit = {},\n    onNavigateToJaringan: () -> Unit = {}")

if "MenuItem(icon = Icons.Default.Report, title = \"Jaringan\"" in content:
    content = content.replace("MenuItem(icon = Icons.Default.Report, title = \"Jaringan\", tint = textErrorPrimary, onClick = {})", "MenuItem(icon = Icons.Default.Report, title = \"Jaringan\", tint = textErrorPrimary, onClick = onNavigateToJaringan)")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
