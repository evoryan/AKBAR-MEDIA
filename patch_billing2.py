import re

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

old_unpaid = """val unpaidSum = unpaidCustomers.sumOf { it.price.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }"""
new_unpaid = """val unpaidSum = unpaidCustomers.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }"""
content = content.replace(old_unpaid, new_unpaid)

old_paid = """val paidSum = paidCustomers.sumOf { it.price.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }"""
new_paid = """val paidSum = paidCustomers.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }"""
content = content.replace(old_paid, new_paid)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
