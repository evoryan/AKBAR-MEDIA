import re

filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add onNavigateToSetting to DashboardScreen
content = content.replace("onNavigateToStockBarang = { navController.navigate(StockBarangRoute) }",
                          "onNavigateToStockBarang = { navController.navigate(StockBarangRoute) },\n                    onNavigateToSetting = { navController.navigate(SettingRoute) }")

# Add Composable SettingRoute
composable_setting = """
            composable<SettingRoute> {
                SettingScreen(onBack = { navController.popBackStack() })
            }
"""

content = content.replace("composable<ManageSecretsRoute> {", composable_setting + "            composable<ManageSecretsRoute> {")

with open(filepath, 'w') as f:
    f.write(content)
