import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                                    val amountStr = jumlah.replace(Regex("[^0-9]"), "")
                                    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0"""

rep = """                                    val amountStr = jumlah.replace(Regex("[^0-9.]"), "")
                                    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0"""

content = content.replace(target, rep)

target2 = """                        jumlah = item.amount.toString()"""
rep2 = """                        jumlah = item.amount.toLong().toString()"""
content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
