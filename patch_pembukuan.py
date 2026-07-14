with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                            PembukuanItem(
                                "Total Pemasukkan Lain2", 
                                "Rp. ${categories["Pemasukkan Lain2"] ?: 0L.let { String.format("%,d", it).replace(",", ".") }}", 
                                successGreen, 
                                modifier = Modifier.weight(1f).clickable { onNavigateToSemuaPembukuan("Pemasukan") }
                            )"""

replacement = """                            PembukuanItem(
                                "Total Pemasukkan Lain2", 
                                "Rp. ${categories["Pemasukkan Lain2"] ?: 0L.let { String.format("%,d", it).replace(",", ".") }}", 
                                successGreen, 
                                modifier = Modifier.weight(1f).clickable {
                                    selectedCategory = "Pemasukkan Lain2"
                                    selectedType = "pemasukan"
                                    inputAmount = ""
                                    inputDescription = ""
                                    showAddDialog = true
                                }
                            )"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
