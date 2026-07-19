with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Change verticalArrangement of main Content Column
content = content.replace("verticalArrangement = Arrangement.spacedBy(16.dp)", "verticalArrangement = Arrangement.spacedBy(8.dp)")

# Change padding inside BELUM BAYAR and SUDAH BAYAR Summary boxes
content = content.replace(".padding(16.dp)\n                    ) {\n                        Column {\n                            Text(\"Total Tagihan Belum Dibayar\"", ".padding(12.dp)\n                    ) {\n                        Column {\n                            Text(\"Total Tagihan Belum Dibayar\"")
content = content.replace(".padding(16.dp)\n                    ) {\n                        Column {\n                            Text(\"Total Tagihan Sudah Dibayar\"", ".padding(12.dp)\n                    ) {\n                        Column {\n                            Text(\"Total Tagihan Sudah Dibayar\"")

# Add height constraints to filter forms
content = content.replace("modifier = Modifier.menuAnchor().fillMaxWidth(),", "modifier = Modifier.menuAnchor().fillMaxWidth().height(52.dp),")
content = content.replace("modifier = Modifier.weight(1f),", "modifier = Modifier.weight(1f).height(52.dp),")

# Reduce padding in BillingCustomerItem
content = content.replace(".padding(8.dp)\n    ) {\n        Column {", ".padding(6.dp)\n    ) {\n        Column {")

# Reduce space between rows inside BillingCustomerItem
content = content.replace("Spacer(modifier = Modifier.height(4.dp))", "Spacer(modifier = Modifier.height(2.dp))")

# Decrease font sizes slightly in BillingCustomerItem
content = content.replace("Text(customer.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)", "Text(customer.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)")
content = content.replace("Text(customer.status, color = if (customer.status != \"LUNAS CASH\") Color(0xFFFF003C) else neonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)", "Text(customer.status, color = if (customer.status != \"LUNAS CASH\") Color(0xFFFF003C) else neonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)")
content = content.replace("Text(\"Area: ${customer.area}\", color = textSecondary, fontSize = 12.sp)", "Text(\"Area: ${customer.area}\", color = textSecondary, fontSize = 11.sp)")
content = content.replace("Text(customer.phone, color = textSecondary, fontSize = 12.sp)", "Text(customer.phone, color = textSecondary, fontSize = 11.sp)")
content = content.replace("Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)", "Text(customer.price, color = textMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)")

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
