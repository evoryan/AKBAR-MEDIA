with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Make OutlinedTextFields compact by using placeholder instead of label, and adjusting padding if possible
content = content.replace(
    'label = { Text("Area", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp) },',
    'placeholder = { Text("Area", color = textMain.copy(alpha = 0.7f), fontSize = 14.sp) },'
)
content = content.replace(
    'label = { Text("Cari", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp) },',
    'placeholder = { Text("Cari", color = textMain.copy(alpha = 0.7f), fontSize = 14.sp) },'
)

# Also reduce padding of the box
content = content.replace(
    '.padding(12.dp)\n                    ) {\n                        Column {\n                            Text("Total Tagihan Belum Dibayar"',
    '.padding(8.dp)\n                    ) {\n                        Column {\n                            Text("Total Tagihan Belum Dibayar"'
)
content = content.replace(
    '.padding(12.dp)\n                    ) {\n                        Column {\n                            Text("Total Tagihan Sudah Dibayar"',
    '.padding(8.dp)\n                    ) {\n                        Column {\n                            Text("Total Tagihan Sudah Dibayar"'
)
content = content.replace(
    'Spacer(modifier = Modifier.height(8.dp))\n                            Text(totalUnpaid',
    'Spacer(modifier = Modifier.height(4.dp))\n                            Text(totalUnpaid'
)
content = content.replace(
    'Spacer(modifier = Modifier.height(8.dp))\n                            Text(totalPaid',
    'Spacer(modifier = Modifier.height(4.dp))\n                            Text(totalPaid'
)

# And for BillingCustomerItem padding
content = content.replace(
    '.padding(6.dp)\n    ) {\n        Column {',
    '.padding(horizontal = 12.dp, vertical = 8.dp)\n    ) {\n        Column {'
)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
