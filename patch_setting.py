import re

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'r') as f:
    content = f.read()

old_setting = """                    SettingItem(icon = Icons.Default.Hub, title = "ODP", subtitle = "Kelola ODP", iconTint = textMain, onClick = onNavigateToOdp)
                }
            }"""

new_setting = """                    SettingItem(icon = Icons.Default.Hub, title = "ODP", subtitle = "Kelola ODP", iconTint = textMain, onClick = onNavigateToOdp)
                    HorizontalDivider(color = cardBorder)
                    SettingItem(icon = Icons.Default.AccountTree, title = "Rasio", subtitle = "Kelola Data Rasio", iconTint = textMain, onClick = onNavigateToRasio)
                }
            }"""

content = content.replace(old_setting, new_setting)

with open('app/src/main/java/com/example/ui/screens/SettingScreen.kt', 'w') as f:
    f.write(content)
