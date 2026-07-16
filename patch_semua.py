import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                try {
                                    val amountStr = jumlah.replace(Regex("[^0-9.]"), "")
                                    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
                                    var type = "pemasukan"
                                    var category = "Lain-lain"
                                    
                                    if (tipePembukuan == "Pemasukan") {
                                        type = "pemasukan"
                                        category = "Pemasukkan Lain2"
                                    } else if (tipePembukuan == "Pengeluaran") {
                                        type = "pengeluaran"
                                        category = "Lain-lain"
                                    } else if (tipePembukuan == "Setor") {
                                        type = "setor"
                                        category = "Lain-lain"
                                    }
                                    
                                    val req = com.example.ui.data.remote.PembukuanRequest(
                                        type, category, amountDouble, keterangan
                                    )
                                    if (editingItem != null) {
                                        ApiClient.apiService.updatePembukuan(editingItem!!.id, req)
                                    } else {
                                        ApiClient.apiService.addPembukuan(req)
                                    }"""

rep = """                    Button(
                        onClick = { 
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val amountLong = jumlah.replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L
                                    val amountDouble = amountLong.toDouble()
                                    var type = "pemasukan"
                                    var category = editingItem?.category ?: "Lain-lain"
                                    
                                    if (tipePembukuan == "Pemasukan") {
                                        type = "pemasukan"
                                        if (editingItem == null) category = "Pemasukkan Lain2"
                                    } else if (tipePembukuan == "Pengeluaran") {
                                        type = "pengeluaran"
                                        if (editingItem == null) category = "Lain-lain"
                                    } else if (tipePembukuan == "Setor") {
                                        type = "setor"
                                        if (editingItem == null) category = "Lain-lain"
                                    }
                                    
                                    val req = com.example.ui.data.remote.PembukuanRequest(
                                        type, category, amountDouble, keterangan
                                    )
                                    if (editingItem != null) {
                                        ApiClient.apiService.updatePembukuan(editingItem!!.id, req)
                                    } else {
                                        ApiClient.apiService.addPembukuan(req)
                                    }"""

if target in content:
    content = content.replace(target, rep)
else:
    print("Target not found")

target2 = """                                    editingItem = null
                                    keterangan = ""
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

rep2 = """                                    editingItem = null
                                    keterangan = ""
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

if target2 in content:
    content = content.replace(target2, rep2)
else:
    print("Target2 not found")

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)

