with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

import re

click_handler1 = """                                    modifier = Modifier.weight(1f).clickable {
                                        selectedCategory = cat1
                                        selectedType = "pengeluaran"
                                        val detail = pengeluaranDetails.find { it.category == cat1 }
                                        inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                        inputDescription = detail?.description ?: ""
                                        showAddDialog = true
                                    }"""

click_handler2 = """                                        modifier = Modifier.weight(1f).clickable {
                                            selectedCategory = cat2
                                            selectedType = "pengeluaran"
                                            val detail = pengeluaranDetails.find { it.category == cat2 }
                                            inputAmount = detail?.amount?.let { if (it > 0) it.toString() else "" } ?: ""
                                            inputDescription = detail?.description ?: ""
                                            showAddDialog = true
                                        }"""

content = re.sub(r"                                    modifier = Modifier\.weight\(1f\)\.clickable \{\s+selectedCategory = cat1\s+selectedType = \"pengeluaran\"\s+inputAmount = \"\"\s+inputDescription = \"\"\s+showAddDialog = true\s+\}", click_handler1, content)
content = re.sub(r"                                        modifier = Modifier\.weight\(1f\)\.clickable \{\s+selectedCategory = cat2\s+selectedType = \"pengeluaran\"\s+inputAmount = \"\"\s+inputDescription = \"\"\s+showAddDialog = true\s+\}", click_handler2, content)

# Patch the save action
save_action_old = """                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.addPembukuan(
                                                com.example.ui.data.remote.PembukuanRequest(
                                                    type = selectedType,
                                                    amount = amount,
                                                    description = inputDescription,
                                                    category = selectedCategory
                                                )
                                            )
                                            fetchData()
                                            showAddDialog = false
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }"""

save_action_new = """                                    coroutineScope.launch {
                                        try {
                                            if (selectedType == "pengeluaran") {
                                                ApiClient.apiService.updatePengeluaranDetail(
                                                    com.example.ui.data.remote.PengeluaranRequest(
                                                        category = selectedCategory,
                                                        amount = amount,
                                                        description = inputDescription
                                                    )
                                                )
                                            } else {
                                                ApiClient.apiService.addPembukuan(
                                                    com.example.ui.data.remote.PembukuanRequest(
                                                        type = selectedType,
                                                        amount = amount,
                                                        description = inputDescription,
                                                        category = selectedCategory
                                                    )
                                                )
                                            }
                                            fetchData()
                                            showAddDialog = false
                                        } catch (e: Exception) {
                                            // Handle error
                                        }
                                    }"""

content = content.replace(save_action_old, save_action_new)

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
