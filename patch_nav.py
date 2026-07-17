import re

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

if "object JaringanRoute" not in content:
    content = content.replace("@Serializable\nobject DashboardRoute", "@Serializable\nobject JaringanRoute\n@Serializable\nobject DashboardRoute")

if "onNavigateToJaringan = { navController.navigate(JaringanRoute) }" not in content:
    content = content.replace("onNavigateToSetting = { navController.navigate(SettingRoute) }", "onNavigateToSetting = { navController.navigate(SettingRoute) },\n                    onNavigateToJaringan = { navController.navigate(JaringanRoute) }")

jaringan_composable = """
            composable<JaringanRoute> {
                com.example.ui.screens.JaringanScreen(
                    onBack = { navController.popBackStack() }
                )
            }
"""
if "JaringanScreen" not in content:
    content = content.replace("composable<CustomersRoute> {", jaringan_composable + "\n            composable<CustomersRoute> {")

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
