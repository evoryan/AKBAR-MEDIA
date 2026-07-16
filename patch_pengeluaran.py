import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                                            val detail = pengeluaranDetails.find { it.category == cat1 }
                                            inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                            inputDescription = detail?.description ?: ""
                                            showAddDialog = true"""

rep = """                                            val detail = pengeluaranDetails.find { it.category == cat1 }
                                            inputAmount = ""
                                            inputDescription = ""
                                            showAddDialog = true"""

content = content.replace(target, rep)

target2 = """                                                val detail = pengeluaranDetails.find { it.category == cat2 }
                                                inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                                inputDescription = detail?.description ?: ""
                                                showAddDialog = true"""

rep2 = """                                                val detail = pengeluaranDetails.find { it.category == cat2 }
                                                inputAmount = ""
                                                inputDescription = ""
                                                showAddDialog = true"""

content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)

