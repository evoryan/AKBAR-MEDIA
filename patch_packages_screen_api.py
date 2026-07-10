with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'r') as f:
    content = f.read()

import re

# Patch onDelete
content = re.sub(
    r'onDelete = \{ packages.remove\(pkg\) \}',
    r'''onDelete = { 
                            coroutineScope.launch {
                                try {
                                    com.example.ui.data.remote.ApiClient.apiService.deletePackage(pkg.id)
                                    val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
                                    packages.clear()
                                    packages.addAll(res)
                                } catch(e: Exception) {}
                            }
                        }''',
    content
)

# Patch onSave
old_onSave = """                onSave = { newPkg ->
                    if (packageToEdit != null) {
                        val index = packages.indexOfFirst { it.id == packageToEdit!!.id }
                        if (index != -1) {
                            packages[index] = newPkg.copy(id = packageToEdit!!.id)
                        }
                    } else {
                        packages.add(newPkg.copy(id = (packages.size + 1).toString()))
                    }
                    showAddDialog = false
                    packageToEdit = null
                }"""

new_onSave = """                onSave = { newPkg ->
                    coroutineScope.launch {
                        try {
                            if (packageToEdit != null) {
                                // Assume we need to delete then add for edit (simplified) or API supports it.
                                // Our server.js doesn't have PUT /api/packages/:id yet. 
                                // Let's delete old and add new for simplicity, or just add.
                                com.example.ui.data.remote.ApiClient.apiService.deletePackage(packageToEdit!!.id)
                                com.example.ui.data.remote.ApiClient.apiService.addPackage(newPkg)
                            } else {
                                com.example.ui.data.remote.ApiClient.apiService.addPackage(newPkg)
                            }
                            val res = com.example.ui.data.remote.ApiClient.apiService.getPackages()
                            packages.clear()
                            packages.addAll(res)
                            showAddDialog = false
                            packageToEdit = null
                        } catch(e: Exception) {}
                    }
                }"""

content = content.replace(old_onSave, new_onSave)

with open('app/src/main/java/com/example/ui/screens/PackagesScreen.kt', 'w') as f:
    f.write(content)
