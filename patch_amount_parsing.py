import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target = """val amount = inputAmount.toLongOrNull() ?: 0.0"""
rep = """val amount = inputAmount.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0.0"""
content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target2 = """val amountDouble = jumlah.toDoubleOrNull() ?: 0.0"""
rep2 = """val amountDouble = jumlah.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0"""
content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)

