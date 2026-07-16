import re

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

target = """                Button(
                    onClick = { 
                        isUpdating = true
                        updateResult = null
                        coroutineScope.launch {
                            delay(1500)
                            isUpdating = false
                            updateResult = "Setoran sebesar Rp. $nominalSetoran berhasil ditambahkan!"
                            nominalSetoran = ""
                        }
                    },"""

rep = """                Button(
                    onClick = { 
                        isUpdating = true
                        updateResult = null
                        coroutineScope.launch {
                            try {
                                val amount = nominalSetoran.replace(Regex("[^0-9]"), "").toDoubleOrNull() ?: 0.0
                                com.example.ui.data.remote.ApiClient.apiService.addSetoran(
                                    com.example.ui.data.remote.SetoranRequest(admin.name, amount)
                                )
                                isUpdating = false
                                updateResult = "Setoran sebesar Rp. $nominalSetoran berhasil ditambahkan!"
                                nominalSetoran = ""
                            } catch (e: Exception) {
                                isUpdating = false
                                updateResult = "Gagal menyetor uang"
                            }
                        }
                    },"""

content = content.replace(target, rep)

# also need to make UangDiAdminScreen fetch real data instead of dummy data if it isn't already.
with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
