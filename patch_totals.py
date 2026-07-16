import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                    Text("Total Pengeluaran", color = textMain, fontSize = 14.sp)
                    Text("-", color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pemasukan Lain2", color = textMain, fontSize = 14.sp)
                    Text("-", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Setor", color = textMain, fontSize = 14.sp)
                    Text("-", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }"""

rep = """                    Text("Total Pengeluaran", color = textMain, fontSize = 14.sp)
                    Text(
                        "Rp. ${String.format("%,d", pembukuanList.filter { it.type.lowercase() == "pengeluaran" }.sumOf { it.amount.toLong() }).replace(",", ".")}",
                        color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pemasukan Lain2", color = textMain, fontSize = 14.sp)
                    Text(
                        "Rp. ${String.format("%,d", pembukuanList.filter { it.type.lowercase() == "pemasukan" }.sumOf { it.amount.toLong() }).replace(",", ".")}",
                        color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Setor", color = textMain, fontSize = 14.sp)
                    Text(
                        "Rp. ${String.format("%,d", pembukuanList.filter { it.type.lowercase() == "setor" }.sumOf { it.amount.toLong() }).replace(",", ".")}",
                        color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)

