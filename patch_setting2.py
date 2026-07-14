import re

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

target1 = """    onNavigateToGatewayPayment: () -> Unit,
    onNavigateToCompanySettings: () -> Unit,"""
rep1 = """    onNavigateToGatewayPayment: () -> Unit,
    onNavigateToCompanySettings: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,"""

target2 = """                        SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                    }
                }"""
rep2 = """                        SettingItem(icon = Icons.Default.Payments, title = "Pengaturan Gateway Payment", subtitle = "Integrasi payment gateway", iconTint = textMain, onClick = onNavigateToGatewayPayment)
                        HorizontalDivider(color = cardBorder)
                        SettingItem(icon = Icons.Default.Backup, title = "Backup & Restore", subtitle = "Database Pelanggan", iconTint = textMain, onClick = onNavigateToBackupRestore)
                    }
                }"""

if target1 in content:
    content = content.replace(target1, rep1)
    content = content.replace(target2, rep2)
    with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
        f.write(content)
    print("Patched SettingScreen.kt")
else:
    print("Target not found")
