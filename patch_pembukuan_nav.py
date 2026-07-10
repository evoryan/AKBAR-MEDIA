with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("onNavigateToSemuaPembukuan: () -> Unit", "onNavigateToSemuaPembukuan: (String) -> Unit")
content = content.replace("fun PembukuanScreen(", "fun PembukuanScreen(onNavigateToBilling: (Int) -> Unit, ")
# Note: Since I'm not sure if there are other occurrences, let's be careful.
with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
