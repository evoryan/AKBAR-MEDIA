import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = bgMain,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {"""

rep = """                    FloatingActionButton(
                        onClick = { 
                            editingItem = null
                            keterangan = ""
                            jumlah = ""
                            tipePembukuan = "Pilih Tipe Pembukuan"
                            showAddDialog = true 
                        },
                        containerColor = bgMain,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)

