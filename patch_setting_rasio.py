import re

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'r') as f:
    content = f.read()

nav = """    onNavigateToOdp: () -> Unit,
    onNavigateToRasio: () -> Unit,"""
content = content.replace("    onNavigateToOdp: () -> Unit,", nav)

item = """                    SettingItem(icon = Icons.Default.DeviceHub, title = "Data ODP", subtitle = "Manajemen Optical Distribution Point", iconTint = textMain, onClick = onNavigateToOdp)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.LinearScale, title = "Data Rasio", subtitle = "Manajemen Rasio Splitter", iconTint = textMain, onClick = onNavigateToRasio)"""
content = content.replace("                    SettingItem(icon = Icons.Default.DeviceHub, title = \"Data ODP\", subtitle = \"Manajemen Optical Distribution Point\", iconTint = textMain, onClick = onNavigateToOdp)", item)

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'w') as f:
    f.write(content)
