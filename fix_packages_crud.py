filepath = 'app/src/main/java/com/example/ui/screens/PackagesScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace static packages list with LaunchedEffect fetching from API
target_packages = """    val packages = remember {
        mutableStateListOf(
            InternetPackage("1", "Paket Hemat", "10 Mbps", 125000.0, 0.0, "hemat-profile", "Paket internet murah untuk keluarga kecil"),
            InternetPackage("2", "Paket Standar", "20 Mbps", 200000.0, 11.0, "standar-profile", "Cocok untuk streaming dan browsing harian"),
            InternetPackage("3", "Paket Premium", "50 Mbps", 350000.0, 11.0, "premium-profile", "Kecepatan tinggi untuk gaming dan kerja dari rumah")
        )
    }"""

replacement_packages = """    var packages = remember { mutableStateListOf<InternetPackage>() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
            packages.clear()
            packages.addAll(res)
        } catch (e: Exception) {
            // handle
        }
    }"""

content = content.replace(target_packages, replacement_packages)

# Find delete button in PackagesScreen and update it
# "onClick = { packages.remove(pkg) }"
target_delete = """                                    IconButton(
                                        onClick = { packages.remove(pkg) }
                                    ) {"""
replacement_delete = """                                    IconButton(
                                        onClick = { 
                                            coroutineScope.launch {
                                                try {
                                                    com.example.ui.data.remote.ApiClient.apiService.deletePackage(pkg.id)
                                                    packages.remove(pkg)
                                                } catch(e: Exception) {}
                                            }
                                        }
                                    ) {"""
content = content.replace(target_delete, replacement_delete)

# Find add button in addDialog inside PackagesScreen
# "packages.add(newPackage)"
target_add = """                            packages.add(newPackage)
                            showAddDialog = false"""
replacement_add = """                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.addPackage(newPackage)
                                    val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
                                    packages.clear()
                                    packages.addAll(res)
                                } catch(e: Exception) {}
                            }
                            showAddDialog = false"""
content = content.replace(target_add, replacement_add)

if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

with open(filepath, 'w') as f:
    f.write(content)
