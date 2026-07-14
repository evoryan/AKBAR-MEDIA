import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

content = content.replace("TextFieldDefaults.outlinedTextFieldColors(", "OutlinedTextFieldDefaults.colors(")

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
