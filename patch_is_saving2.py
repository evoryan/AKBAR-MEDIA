import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target_vars = """    var inputDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()"""

rep_vars = """    var inputDescription by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()"""

content = content.replace(target_vars, rep_vars)

target_button = """                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val amount = inputAmount.toLongOrNull() ?: 0.0"""

rep_button = """                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val amount = inputAmount.toLongOrNull() ?: 0.0"""

content = content.replace(target_button, rep_button)

target_button_end = """                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan)
                    ) {
                        Text("Simpan", color = Color.White)
                    }"""

rep_button_end = """                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Menyimpan..." else "Simpan", color = Color.White)
                    }"""

content = content.replace(target_button_end, rep_button_end)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)

