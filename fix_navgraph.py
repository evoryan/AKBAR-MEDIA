import re

filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("onNavigateToManageSecrets = { navController.navigate(ManageSecretsRoute) }", "onNavigateToManageSecrets = { areaId -> navController.navigate(ManageSecretsRoute(areaId)) }")
content = content.replace("composable<ManageSecretsRoute> {", "composable<ManageSecretsRoute> {\n                val route = it.toRoute<ManageSecretsRoute>()")

with open(filepath, 'w') as f:
    f.write(content)
