with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

content = content.replace('var pemasukan by remember { mutableStateOf(0L) }', 'var pemasukan by remember { mutableStateOf(0.0) }')
content = content.replace('var pengeluaran by remember { mutableStateOf(0L) }', 'var pengeluaran by remember { mutableStateOf(0.0) }')
content = content.replace('var categories by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }', 'var categories by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }')

# Fix references
content = content.replace('?: 0L', '?: 0.0')

# Fix formatting
content = content.replace('String.format("%,d", it)', 'String.format("%,d", it.toLong())')
content = content.replace('String.format("%,d", pemasukan)', 'String.format("%,d", pemasukan.toLong())')
content = content.replace('String.format("%,d", pengeluaran)', 'String.format("%,d", pengeluaran.toLong())')
content = content.replace('String.format("%,d", pemasukan - pengeluaran)', 'String.format("%,d", (pemasukan - pengeluaran).toLong())')

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)
print("Patched types in PembukuanScreen")
