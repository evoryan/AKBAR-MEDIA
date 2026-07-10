with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

import re

old_save = r"""                                    val amount = inputAmount\.toDoubleOrNull\(\) \?: 0\.0
                                    ApiClient\.apiService\.addPembukuan\(PembukuanRequest\(selectedType, selectedCategory, amount, inputDescription\)\)
                                    fetchData\(\)
                                    showAddDialog = false"""

new_save = """                                    val amount = inputAmount.toLongOrNull() ?: 0L
                                    if (selectedType == "pengeluaran") {
                                        ApiClient.apiService.updatePengeluaranDetail(com.example.ui.data.remote.PengeluaranRequest(selectedCategory, amount, inputDescription))
                                    } else {
                                        ApiClient.apiService.addPembukuan(com.example.ui.data.remote.PembukuanRequest(selectedType, selectedCategory, amount.toDouble(), inputDescription))
                                    }
                                    fetchData()
                                    showAddDialog = false"""

content = re.sub(old_save, new_save, content)

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
