import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

# Fix pppoeSecret
content = content.replace('customer?.pppoeSecret ?: "-"', 'customer?.pppoeSecret?.takeIf { it.isNotBlank() } ?: "-"')
# Fix odpPort
content = content.replace('customer?.odpPort ?: "-"', 'customer?.odpPort?.takeIf { it.isNotBlank() } ?: "-"')
# Fix registerDate
content = content.replace('customer?.registerDate ?: "-"', 'customer?.registerDate?.takeIf { it.isNotBlank() } ?: "-"')
# Fix isolateDate
content = content.replace('customer?.isolateDate ?: "-"', 'customer?.isolateDate?.takeIf { it.isNotBlank() } ?: "-"')
# Fix packageName
content = content.replace('customer?.packageName ?: "-"', 'customer?.packageName?.takeIf { it.isNotBlank() } ?: "-"')

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
