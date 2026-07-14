import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target = """                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = cardBorder,
                            textColor = textMain
                        )"""

rep = """                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
