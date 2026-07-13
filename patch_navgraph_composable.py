import re
with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    content = f.read()

old = "composable<GatewayPaymentRoute> { GatewayPaymentScreen(onBack = { navController.popBackStack() }) }"
new = "composable<GatewayPaymentRoute> { GatewayPaymentScreen(onBack = { navController.popBackStack() }) }\n            composable<CompanySettingsRoute> { com.example.ui.screens.CompanySettingsScreen(onBack = { navController.popBackStack() }) }"

content = content.replace(old, new)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.write(content)
