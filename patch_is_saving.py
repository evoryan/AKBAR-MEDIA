import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target_vars = """    var tipePembukuan by remember { mutableStateOf("Pilih Tipe Pembukuan") }
    var expanded by remember { mutableStateOf(false) }"""

rep_vars = """    var tipePembukuan by remember { mutableStateOf("Pilih Tipe Pembukuan") }
    var expanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }"""

content = content.replace(target_vars, rep_vars)

target_button = """                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val amountDouble = jumlah.toDoubleOrNull() ?: 0.0"""

rep_button = """                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val amountDouble = jumlah.toDoubleOrNull() ?: 0.0"""

content = content.replace(target_button, rep_button)

target_button_end = """                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "Gagal menyimpan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("SIMPAN", color = Color.White, fontWeight = FontWeight.Bold)
                    }"""

rep_button_end = """                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "Gagal menyimpan: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF)),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "MENYIMPAN..." else "SIMPAN", color = Color.White, fontWeight = FontWeight.Bold)
                    }"""

content = content.replace(target_button_end, rep_button_end)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)

