import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target1 = """                                PembukuanItem(
                                    cat1, 
                                    "Rp. ${ (categories[cat1] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", 
                                    errorRed, 
                                    modifier = Modifier.weight(1f).clickable {
                                        selectedCategory = cat1
                                        selectedType = "pengeluaran"
                                        val detail = pengeluaranDetails.find { it.category == cat1 }
                                        inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                        inputDescription = detail?.description ?: ""
                                        showAddDialog = true
                                    }
                                )"""

rep1 = """                                PembukuanItem(
                                    cat1, 
                                    "Rp. ${ (categories[cat1] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", 
                                    errorRed, 
                                    modifier = Modifier.weight(1f).clickable {
                                        if (cat1 == "Lain-lain") {
                                            onNavigateToSemuaPembukuan("Pengeluaran")
                                        } else {
                                            selectedCategory = cat1
                                            selectedType = "pengeluaran"
                                            val detail = pengeluaranDetails.find { it.category == cat1 }
                                            inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                            inputDescription = detail?.description ?: ""
                                            showAddDialog = true
                                        }
                                    }
                                )"""

target2 = """                                    PembukuanItem(
                                        cat2, 
                                        "Rp. ${ (categories[cat2] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", 
                                        errorRed, 
                                        modifier = Modifier.weight(1f).clickable {
                                            selectedCategory = cat2
                                            selectedType = "pengeluaran"
                                            val detail = pengeluaranDetails.find { it.category == cat2 }
                                            inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                            inputDescription = detail?.description ?: ""
                                            showAddDialog = true
                                        }
                                    )"""

rep2 = """                                    PembukuanItem(
                                        cat2, 
                                        "Rp. ${ (categories[cat2] ?: 0.0).let { String.format("%,d", it.toLong()).replace(",", ".") } }", 
                                        errorRed, 
                                        modifier = Modifier.weight(1f).clickable {
                                            if (cat2 == "Lain-lain") {
                                                onNavigateToSemuaPembukuan("Pengeluaran")
                                            } else {
                                                selectedCategory = cat2
                                                selectedType = "pengeluaran"
                                                val detail = pengeluaranDetails.find { it.category == cat2 }
                                                inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                                inputDescription = detail?.description ?: ""
                                                showAddDialog = true
                                            }
                                        }
                                    )"""

content = content.replace(target1, rep1)
content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)
