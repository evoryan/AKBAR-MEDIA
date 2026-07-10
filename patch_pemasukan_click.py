with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

import re

# Transaksi Cash
old_cash = r'PembukuanItem\("Transaksi Cash", "Rp. \$\{ \(categories\["Transaksi Cash"\] \?: 0L\)\.let \{ String\.format\("%,d", it\)\.replace\(",", "\."\) \} \}", successGreen, modifier = Modifier\.weight\(1f\)\)'
new_cash = 'PembukuanItem("Transaksi Cash", "Rp. ${ (categories["Transaksi Cash"] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", successGreen, modifier = Modifier.weight(1f).clickable { onNavigateToBilling(1) })'

content = re.sub(old_cash, new_cash, content)

# Pemasukkan Lain2
old_lain = r"""                                modifier = Modifier\.weight\(1f\)\.clickable \{
                                    selectedCategory = "Pemasukkan Lain2"
                                    selectedType = "pemasukan"
                                    inputAmount = ""
                                    inputDescription = ""
                                    showAddDialog = true
                                \}"""
new_lain = """                                modifier = Modifier.weight(1f).clickable { onNavigateToSemuaPembukuan("Pemasukan") }"""

content = re.sub(old_lain, new_lain, content)

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
