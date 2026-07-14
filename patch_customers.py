import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

target1 = """    val filteredCustomers = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.username.contains(searchQuery, ignoreCase = true) || 
        it.phone.contains(searchQuery, ignoreCase = true)
    }"""

rep1 = """    val filteredCustomers = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.username.contains(searchQuery, ignoreCase = true) || 
        it.phone.contains(searchQuery, ignoreCase = true)
    }
    
    val activeCustomers = customers.filter { it.status != "TERHAPUS" }
    val totalPendapatanGlobal = activeCustomers.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }
    val activeFilteredCustomers = filteredCustomers.filter { it.status != "TERHAPUS" }
    val totalPendapatanArea = activeFilteredCustomers.sumOf { it.price.replace(Regex("\\\\.0$"), "").replace(Regex("[^0-9]"), "").toLongOrNull() ?: 0L }"""

content = content.replace(target1, rep1)

target2 = """            var showDeleteConfirm by remember { mutableStateOf(false) }
    var customerToDeleteState by remember { mutableStateOf<Customer?>(null) }
    var expanded by remember { mutableStateOf(false) }"""

rep2 = """            var showDeleteConfirm by remember { mutableStateOf(false) }
    var customerToDeleteState by remember { mutableStateOf<Customer?>(null) }
    var expanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Global", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(totalPendapatanGlobal)}",
                            color = neonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total $selectedArea", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Rp. ${java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(totalPendapatanArea)}",
                            color = neonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }"""

content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
