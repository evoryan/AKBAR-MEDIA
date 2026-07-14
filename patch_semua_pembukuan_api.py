with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\nimport com.example.ui.data.remote.ApiClient\n")
if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport kotlinx.coroutines.launch\nimport com.example.ui.data.remote.ApiClient")

# Add coroutineScope
if "val coroutineScope = rememberCoroutineScope()" not in content:
    content = content.replace("var searchQuery by remember { mutableStateOf(\"\") }", "var searchQuery by remember { mutableStateOf(\"\") }\n    val coroutineScope = rememberCoroutineScope()")

target_simpan = """                    Button(
                        onClick = { 
                            showAddDialog = false
                            keterangan = ""
                            jumlah = ""
                            tipePembukuan = "Pilih Tipe Pembukuan"
                        },"""

rep_simpan = """                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                try {
                                    val amountStr = jumlah.replace(Regex("[^0-9]"), "")
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
                                    
                                    ApiClient.apiService.addPembukuan(
                                        com.example.ui.data.remote.PembukuanRequest(
                                            type, category, amountDouble, keterangan
                                        )
                                    )
                                    showAddDialog = false
                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },"""

content = content.replace(target_simpan, rep_simpan)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
print("Patched SIMPAN button in SemuaPembukuanScreen")
