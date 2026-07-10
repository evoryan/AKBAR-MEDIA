with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("onClick = onNavigateToSemuaPembukuan", "onClick = { onNavigateToSemuaPembukuan(\"\") }")

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
