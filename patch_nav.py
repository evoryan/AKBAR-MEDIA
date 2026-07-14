import re

with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "r") as f:
    content = f.read()

target1 = """@Serializable
object HistoryStockRoute

@Serializable
object SettingRoute"""

rep1 = """@Serializable
object HistoryStockRoute

@Serializable
object BackupRestoreRoute

@Serializable
object SettingRoute"""

target2 = """                    onNavigateToCompanySettings = { navController.navigate(CompanySettingsRoute) },"""

rep2 = """                    onNavigateToCompanySettings = { navController.navigate(CompanySettingsRoute) },
                    onNavigateToBackupRestore = { navController.navigate(BackupRestoreRoute) },"""

target3 = """            composable<CompanySettingsRoute> { com.example.ui.screens.CompanySettingsScreen(onBack = { navController.popBackStack() }) }"""

rep3 = """            composable<CompanySettingsRoute> { com.example.ui.screens.CompanySettingsScreen(onBack = { navController.popBackStack() }) }
            composable<BackupRestoreRoute> { com.example.ui.screens.BackupRestoreScreen(onBack = { navController.popBackStack() }) }"""

if target2 in content:
    if target1 in content:
        content = content.replace(target1, rep1)
    content = content.replace(target2, rep2)
    content = content.replace(target3, rep3)
    with open("app/src/main/java/com/example/ui/navigation/NavGraph.kt", "w") as f:
        f.write(content)
    print("Patched NavGraph.kt")
else:
    print("Target not found")
