import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
    'onNavigateToStockBarang: () -> Unit,\n    viewModel:',
    'onNavigateToStockBarang: () -> Unit,\n    onNavigateToSetting: () -> Unit,\n    viewModel:'
)

content = content.replace(
    'MenuItem(icon = Icons.Default.Settings, title = "Lainnya", tint = textSecondary, onClick = {})',
    'MenuItem(icon = Icons.Default.Settings, title = "Setting", tint = primaryBg, onClick = onNavigateToSetting)'
)

with open(filepath, 'w') as f:
    f.write(content)
