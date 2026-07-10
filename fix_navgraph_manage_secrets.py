import re

filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("ManageSecretsScreen(onBack = { navController.popBackStack() })", "ManageSecretsScreen(areaId = route.areaId, onBack = { navController.popBackStack() })")

with open(filepath, 'w') as f:
    f.write(content)
