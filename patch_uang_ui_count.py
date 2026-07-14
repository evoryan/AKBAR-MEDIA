import re

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

target = """                AdminData(
                    name = admin.name,
                    totalDiterima = formattedAmount,
                    setor = "Rp. 0",
                    sisa = formattedAmount,
                    sisaColor = warningYellow,
                    persentase = "100%",
                    pengeluaran = "Rp. 0",
                    persentaseSisa = "100%",
                    jmlPlggn = "-" // count not available yet
                )"""

rep = """                val jml = uangAdmin.find { it.adminName == admin.name }?.jmlPlggn ?: 0
                AdminData(
                    name = admin.name,
                    totalDiterima = formattedAmount,
                    setor = "Rp. 0",
                    sisa = formattedAmount,
                    sisaColor = warningYellow,
                    persentase = "100%",
                    pengeluaran = "Rp. 0",
                    persentaseSisa = "100%",
                    jmlPlggn = jml.toString()
                )"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
