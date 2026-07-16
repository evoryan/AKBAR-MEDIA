import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Tidak ada Data",
                color = textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )"""

replacement = """            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            } else if (pembukuanList.isEmpty()) {
                Text(
                    text = "Tidak ada Data",
                    color = textSecondary,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pembukuanList.filter { it.description?.contains(searchQuery, ignoreCase = true) == true || it.category?.contains(searchQuery, ignoreCase = true) == true || it.type.contains(searchQuery, ignoreCase = true) }) { item ->
                        PembukuanListItem(
                            item = item,
                            bgMain = bgMain,
                            textMain = textMain,
                            textSecondary = textSecondary,
                            onEdit = {
                                editingItem = item
                                tipePembukuan = when (item.type.lowercase()) {
                                    "pemasukan" -> "Pemasukan"
                                    "pengeluaran" -> "Pengeluaran"
                                    "setor" -> "Setor"
                                    else -> "Pilih Tipe Pembukuan"
                                }
                                keterangan = item.description ?: ""
                                jumlah = item.amount.toLong().toString()
                                showAddDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    try {
                                        ApiClient.apiService.deletePembukuan(item.id)
                                        pembukuanList = ApiClient.apiService.getAllPembukuan()
                                        android.widget.Toast.makeText(context, "Data berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context, "Gagal menghapus data", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }"""

content = content.replace(target, replacement)

target_item = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemuaPembukuanScreen"""

replacement_item = """@Composable
fun PembukuanListItem(
    item: com.example.ui.data.remote.PembukuanItem,
    bgMain: Color,
    textMain: Color,
    textSecondary: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgMain),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description ?: "-",
                    color = textMain,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.type.uppercase()} - ${item.category ?: "-"}",
                    color = textSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    color = textSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val color = if (item.type.lowercase() == "pemasukan") Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    text = "Rp. ${String.format("%,d", item.amount.toLong()).replace(",", ".")}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit", tint = primaryBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemuaPembukuanScreen"""

content = content.replace(target_item, replacement_item)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
