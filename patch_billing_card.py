with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Fix the unresolved reference and missing brace
content = content.replace("fun BillingCustomerItem(\n    customer: Customer, \n    cardBg: Color, \n    cardBorder: Color, \n    textMain: Color, \n    textSecondary: Color, \n    neonCyan: Color, \n    neonPink: Color, \n    onPayClick: () -> Unit,\n    onDetailClick: () -> Unit\n) {", 
"fun BillingCustomerItem(\n    customer: Customer, \n    cardBg: Color, \n    cardBorder: Color, \n    textMain: Color, \n    textSecondary: Color, \n    neonCyan: Color, \n    neonPink: Color, \n    onPayClick: () -> Unit,\n    onDetailClick: () -> Unit,\n    onDeleteClick: () -> Unit = {}\n) {")

content = content.replace("                Button(\n                    onClick = {", "                }\n                Button(\n                    onClick = {")

content = content.replace('border = if (customer.status != "BELUM BAYAR") androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,', 'border = if (customer.status == "LUNAS CASH") androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,')


with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
