import re

filepath = 'app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("onNavigateToManageSecrets: () -> Unit = {}", "onNavigateToManageSecrets: (String) -> Unit = {}")
content = content.replace("MikrotikCard(area = area, onNavigateToManageSecrets = onNavigateToManageSecrets)", "MikrotikCard(area = area, onNavigateToManageSecrets = { onNavigateToManageSecrets(area.id) })")
content = content.replace("fun MikrotikCard(area: Area, onNavigateToManageSecrets: () -> Unit) {", "fun MikrotikCard(area: Area, onNavigateToManageSecrets: () -> Unit) {") # wait this is used by mikrotikcard

with open(filepath, 'w') as f:
    f.write(content)
