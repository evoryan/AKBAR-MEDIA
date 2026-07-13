import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import androidx.compose.material.icons.filled.Router\nimport androidx.compose.material.icons.filled.Search',
    'import androidx.compose.material.icons.filled.Router\nimport androidx.compose.material.icons.filled.Search\nimport androidx.compose.material.icons.filled.Close'
)

old_filter = """    val areas = listOf("Semua") + customers.map { it.area }.distinct().sorted()
    var selectedArea by remember { mutableStateOf("Semua") }
    
    val filteredCustomers = if (selectedArea == "Semua") customers else customers.filter { it.area == selectedArea }"""

new_filter = """    val areas = listOf("Semua") + customers.map { it.area }.distinct().sorted()
    var selectedArea by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredByArea = if (selectedArea == "Semua") customers else customers.filter { it.area == selectedArea }
    val filteredCustomers = if (searchQuery.isBlank()) filteredByArea else filteredByArea.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.username.contains(searchQuery, ignoreCase = true) || 
        it.phone.contains(searchQuery, ignoreCase = true)
    }"""

content = content.replace(old_filter, new_filter)

old_appbar = """            TopAppBar(
                title = { Text("Daftar Pelanggan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = textMain)
                    }
                },"""

new_appbar = """            TopAppBar(
                title = { 
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari pelanggan...", color = textMain.copy(alpha = 0.5f), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textMain, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = neonCyan
                            ),
                            singleLine = true
                        )
                    } else {
                        Text("Daftar Pelanggan", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchActive = !isSearchActive 
                        if (!isSearchActive) searchQuery = ""
                    }) {
                        Icon(if (isSearchActive) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search", tint = textMain)
                    }
                    if (!isSearchActive) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = textMain)
                        }
                    }
                },"""

content = content.replace(old_appbar, new_appbar)

old_icons = """                Text(text = customer.discount, fontSize = 12.sp, color = greenText)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = yellowBadge, modifier = Modifier.size(20.dp))
                    Icon(Icons.Default.Router, contentDescription = "Device", tint = neonCyan, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}"""

new_icons = """                Text(text = customer.discount, fontSize = 12.sp, color = greenText)
            }
        }
    }
}"""

content = content.replace(old_icons, new_icons)

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
