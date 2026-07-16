import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """    var showAddDialog by remember { mutableStateOf(false) }
    var tipeDropdownExpanded by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Pemasukan", "Pengeluaran", "Setor")
    var tipePembukuan by remember { mutableStateOf(initialType) }
    var keterangan by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()"""

rep = """    var showAddDialog by remember { mutableStateOf(false) }
    var tipeDropdownExpanded by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Pemasukan", "Pengeluaran", "Setor")
    var tipePembukuan by remember { mutableStateOf(initialType) }
    var keterangan by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    var pembukuanList by remember { mutableStateOf(emptyList<com.example.ui.data.remote.PembukuanItem>()) }
    var isLoading by remember { mutableStateOf(true) }
    var editingItem by remember { mutableStateOf<com.example.ui.data.remote.PembukuanItem?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            pembukuanList = ApiClient.apiService.getAllPembukuan()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }"""

content = content.replace(target, rep)

target2 = """                        Text(
                text = "Tidak ada Data",
                color = textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )"""

rep2 = """                        if (isLoading) {
                CircularProgressIndicator(color = primaryBlue, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (pembukuanList.isEmpty()) {
                Text(
                    text = "Tidak ada Data",
                    color = textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pembukuanList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = headerBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.description ?: "Tidak ada keterangan", color = textMain, fontWeight = FontWeight.Bold)
                                    Text(item.category ?: item.type, color = textSecondary, fontSize = 12.sp)
                                    Text(item.created_at?.take(10) ?: "", color = textSecondary, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val amountColor = if (item.type == "pemasukan") successGreen else if (item.type == "pengeluaran") errorRed else warningYellow
                                    Text(
                                        text = (if (item.type == "pemasukan") "+ " else "- ") + "Rp " + String.format(java.util.Locale("id", "ID"), "%,d", item.amount.toLong()),
                                        color = amountColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = primaryBlue,
                                            modifier = Modifier.size(20.dp).clickable {
                                                editingItem = item
                                                keterangan = item.description ?: ""
                                                jumlah = item.amount.toLong().toString()
                                                tipePembukuan = if (item.type == "pemasukan") "Pemasukan" else if (item.type == "pengeluaran") "Pengeluaran" else "Setor"
                                                showAddDialog = true
                                            }
                                        )
                                        Icon(
                                            androidx.compose.material.icons.Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = errorRed,
                                            modifier = Modifier.size(20.dp).clickable {
                                                coroutineScope.launch {
                                                    try {
                                                        ApiClient.apiService.deletePembukuan(item.id)
                                                        pembukuanList = ApiClient.apiService.getAllPembukuan()
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }"""

# add icons import
target3 = """import androidx.compose.material.icons.filled.Search"""
rep3 = """import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items"""
content = content.replace(target3, rep3)

content = content.replace(target2, rep2)

target4 = """                                    ApiClient.apiService.addPembukuan(
                                        com.example.ui.data.remote.PembukuanRequest(
                                            type, category, amountDouble, keterangan
                                        )
                                    )
                                    showAddDialog = false
                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {"""

rep4 = """                                    val req = com.example.ui.data.remote.PembukuanRequest(
                                        type, category, amountDouble, keterangan
                                    )
                                    if (editingItem != null) {
                                        ApiClient.apiService.updatePembukuan(editingItem!!.id, req)
                                    } else {
                                        ApiClient.apiService.addPembukuan(req)
                                    }
                                    pembukuanList = ApiClient.apiService.getAllPembukuan()
                                    showAddDialog = false
                                    editingItem = null
                                    keterangan = ""
                                    jumlah = ""
                                    tipePembukuan = "Pilih Tipe Pembukuan"
                                } catch (e: Exception) {"""

content = content.replace(target4, rep4)

# handle dialog dismiss to reset editingItem
target5 = """            onDismissRequest = { showAddDialog = false },"""
rep5 = """            onDismissRequest = { showAddDialog = false; editingItem = null; keterangan = ""; jumlah = ""; tipePembukuan = "Pilih Tipe Pembukuan" },"""
content = content.replace(target5, rep5)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
