with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

# Add imports for dialog and input
if "import androidx.compose.ui.text.input.KeyboardType" not in content:
    content = content.replace("import androidx.compose.runtime.LaunchedEffect", "import androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.foundation.text.KeyboardOptions\nimport com.example.ui.data.remote.PembukuanRequest\nimport androidx.compose.ui.window.DialogProperties")

# Add state variables
state_vars_old = """    var pemasukan by remember { mutableStateOf(0L) }
    var pengeluaran by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {"""

state_vars_new = """    var pemasukan by remember { mutableStateOf(0L) }
    var pengeluaran by remember { mutableStateOf(0L) }
    var categories by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }

    fun fetchData() {
        coroutineScope.launch {
            try {
                val res = ApiClient.apiService.getPembukuan()
                pemasukan = res.pemasukan
                pengeluaran = res.pengeluaran
                categories = res.categories
            } catch (e: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }
"""
content = content.replace(state_vars_old, state_vars_new)
content = content.replace("IconButton(onClick = { /* refresh */ }", "IconButton(onClick = { fetchData() }")

# Let's replace the hardcoded values with values from `categories`
# Also need to make items clickable if they are in pengeluaran or Pemasukkan Lain2
def make_pembukuan_item_call(label, category, color_var, type_str):
    value_expr = f'categories["{category}"] ?: 0L'
    return f"""PembukuanItem(
                                "{label}", 
                                "Rp. ${{{value_expr}.let {{ String.format("%,d", it).replace(",", ".") }}}}", 
                                {color_var}, 
                                modifier = Modifier.weight(1f).clickable {{
                                    selectedCategory = "{category}"
                                    selectedType = "{type_str}"
                                    inputAmount = ""
                                    inputDescription = ""
                                    showAddDialog = true
                                }}
                            )"""

cash_old = 'PembukuanItem("Transaksi Cash", "Rp. 2.575.000", successGreen, modifier = Modifier.weight(1f))'
cash_new = f"""PembukuanItem("Transaksi Cash", "Rp. ${{ (categories["Transaksi Cash"] ?: 0L).let {{ String.format("%,d", it).replace(",", ".") }} }}", successGreen, modifier = Modifier.weight(1f))"""
content = content.replace(cash_old, cash_new)

online_old = 'PembukuanItem("Transaksi Online", "Rp. 0", successGreen, modifier = Modifier.weight(1f))'
online_new = f"""PembukuanItem("Transaksi Online", "Rp. ${{ (categories["Transaksi Online"] ?: 0L).let {{ String.format("%,d", it).replace(",", ".") }} }}", successGreen, modifier = Modifier.weight(1f))"""
content = content.replace(online_old, online_new)

lain_old = 'PembukuanItem("Total Pemasukkan Lain2", "Rp. 0", successGreen, modifier = Modifier.weight(1f))'
lain_new = make_pembukuan_item_call("Total Pemasukkan Lain2", "Pemasukkan Lain2", "successGreen", "pemasukan")
content = content.replace(lain_old, lain_new)

pengeluaran_loop_old = """                        val pengeluaranItems = listOf(
                            "Gaji Karyawan", "Pasang Baru",
                            "Perbaikan Alat", "Bayar Bandwith",
                            "Bayar Kang Tagih", "Listrik / PDAM / Pulsa",
                            "Bayar Marketing", "Lain lain"
                        )
                        
                        for (i in pengeluaranItems.indices step 2) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                PembukuanItem(pengeluaranItems[i], "Rp. 0", errorRed, modifier = Modifier.weight(1f))
                                if (i + 1 < pengeluaranItems.size) {
                                    PembukuanItem(pengeluaranItems[i + 1], "Rp. 0", errorRed, modifier = Modifier.weight(1f))
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                            if (i < pengeluaranItems.size - 2) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }"""

pengeluaran_loop_new = """                        val pengeluaranItems = listOf(
                            "Gaji Karyawan", "Pasang Baru",
                            "Perbaikan Alat", "Bayar Bandwidth",
                            "Bayar Kang Tagih", "Listrik / PDAM / Pulsa",
                            "Bayar Marketing", "Lain-lain"
                        )
                        
                        for (i in pengeluaranItems.indices step 2) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val cat1 = pengeluaranItems[i]
                                PembukuanItem(
                                    cat1, 
                                    "Rp. ${ (categories[cat1] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", 
                                    errorRed, 
                                    modifier = Modifier.weight(1f).clickable {
                                        selectedCategory = cat1
                                        selectedType = "pengeluaran"
                                        inputAmount = ""
                                        inputDescription = ""
                                        showAddDialog = true
                                    }
                                )
                                if (i + 1 < pengeluaranItems.size) {
                                    val cat2 = pengeluaranItems[i + 1]
                                    PembukuanItem(
                                        cat2, 
                                        "Rp. ${ (categories[cat2] ?: 0L).let { String.format("%,d", it).replace(",", ".") } }", 
                                        errorRed, 
                                        modifier = Modifier.weight(1f).clickable {
                                            selectedCategory = cat2
                                            selectedType = "pengeluaran"
                                            inputAmount = ""
                                            inputDescription = ""
                                            showAddDialog = true
                                        }
                                    )
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                            if (i < pengeluaranItems.size - 2) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }"""
content = content.replace(pengeluaran_loop_old, pengeluaran_loop_new)

# Pendapatan part
pendapatan_old_1 = 'Text("Rp. 2.575.000", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)'
pendapatan_new_1 = 'Text("Rp. ${ String.format("%,d", pemasukan).replace(",", ".") }", color = successGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)'
content = content.replace(pendapatan_old_1, pendapatan_new_1)

pendapatan_old_2 = 'Text("Rp. 0", color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)'
pendapatan_new_2 = 'Text("Rp. ${ String.format("%,d", pengeluaran).replace(",", ".") }", color = errorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)'
content = content.replace(pendapatan_old_2, pendapatan_new_2)

pendapatan_old_3 = 'Text("Rp. 2.575.000", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)'
pendapatan_new_3 = 'Text("Rp. ${ String.format("%,d", pemasukan - pengeluaran).replace(",", ".") }", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)'
content = content.replace(pendapatan_old_3, pendapatan_new_3)


# Add the Add Dialog to the end of Scaffold (inside inner padding or before `}`)
add_dialog = """        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF11111A),
                title = { Text("Input Nominal ($selectedCategory)", color = textMain, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("Nominal (Rp)", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputDescription,
                            onValueChange = { inputDescription = it },
                            label = { Text("Keterangan", color = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val amount = inputAmount.toDoubleOrNull() ?: 0.0
                                    ApiClient.apiService.addPembukuan(PembukuanRequest(selectedType, selectedCategory, amount, inputDescription))
                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Batal", color = textMain)
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
"""

content = content.replace("        // Menu Lain", add_dialog + "\n        // Menu Lain")

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)

