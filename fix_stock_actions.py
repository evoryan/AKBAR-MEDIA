filepath = 'app/src/main/java/com/example/ui/screens/StockBarangScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('onClick = onNavigateToInventory,\n                    modifier = Modifier', 'onClick = { /* TODO */ },\n                    modifier = Modifier')

content = content.replace('''                StockMenuCard(
                    title = "Barang",
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFF00FF4D), // Greenish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToKategori
                )
                StockMenuCard(
                    title = "Kategori",
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFFFB300), // Yellowish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory
                )
                StockMenuCard(
                    title = "History",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF00BFFF), // Blueish
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO */ }
                )''', '''                StockMenuCard(
                    title = "Barang",
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFF00FF4D), // Greenish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventory
                )
                StockMenuCard(
                    title = "Kategori",
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFFFB300), // Yellowish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToKategori
                )
                StockMenuCard(
                    title = "History",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF00BFFF), // Blueish
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHistory
                )''')

with open(filepath, 'w') as f:
    f.write(content)
