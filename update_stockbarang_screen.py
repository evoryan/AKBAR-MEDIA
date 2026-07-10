import re

filepath = 'app/src/main/java/com/example/ui/screens/StockBarangScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Update signature
content = content.replace("fun StockBarangScreen(onBack: () -> Unit) {", "fun StockBarangScreen(\n    onBack: () -> Unit,\n    onNavigateToInventory: () -> Unit,\n    onNavigateToKategori: () -> Unit,\n    onNavigateToHistory: () -> Unit\n) {")

# Update TopAppBar
old_topappbar = """            TopAppBar(
                title = { },
                navigationIcon = {"""
new_topappbar = """            TopAppBar(
                title = { Text("Stock Barang", color = textMain, fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {"""
content = content.replace(old_topappbar, new_topappbar)

# Remove Per- STOCK BARANG -an header
header_text_code = """            // Header Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Per-", color = textSecondary, fontSize = 16.sp)
                Text(
                    "STOCK BARANG",
                    color = primaryBg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text("-an", color = textSecondary, fontSize = 16.sp)
            }"""
content = content.replace(header_text_code, "")

# Update onClick handlers
content = content.replace("onClick = { /* TODO */ }", "onClick = onNavigateToInventory", 1)
content = content.replace("onClick = { /* TODO */ }", "onClick = onNavigateToKategori", 1)
content = content.replace("onClick = { /* TODO */ }", "onClick = onNavigateToHistory", 1)

with open(filepath, 'w') as f:
    f.write(content)
