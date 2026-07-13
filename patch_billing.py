import re

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

old_totals = """    // Hardcoded sum for demonstration
    val totalUnpaid = "Rp. ${unpaidCustomers.size * 100}.000"
    val totalPaid = "Rp. ${paidCustomers.size * 100}.000\""""

new_totals = """    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
    val unpaidSum = unpaidCustomers.sumOf { it.price.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
    val paidSum = paidCustomers.sumOf { it.price.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
    val totalUnpaid = "Rp. ${formatter.format(unpaidSum)}"
    val totalPaid = "Rp. ${formatter.format(paidSum)}\""""

content = content.replace(old_totals, new_totals)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
