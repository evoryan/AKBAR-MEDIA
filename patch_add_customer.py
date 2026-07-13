import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

old_price = """price = selectedPackage?.price?.let { "Rp. $it" } ?: "Rp. 0\""""
new_price = """price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: "Rp. 0\""""

content = content.replace(old_price, new_price)

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
