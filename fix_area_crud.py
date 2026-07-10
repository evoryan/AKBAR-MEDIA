filepath = 'app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target_add = """                    } else {
                        areas.add(newArea.copy(id = UUID.randomUUID().toString()))
                    }
                    showAddDialog = false"""

replacement_add = """                    } else {
                        coroutineScope.launch {
                            try {
                                com.example.ui.data.remote.ApiClient.apiService.addArea(newArea)
                                val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
                                areas.clear()
                                areas.addAll(res)
                            } catch(e: Exception) {}
                        }
                    }
                    showAddDialog = false"""

content = content.replace(target_add, replacement_add)

if "import androidx.compose.runtime.rememberCoroutineScope" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.runtime.rememberCoroutineScope")
if "val coroutineScope = rememberCoroutineScope()" not in content:
    # Need to inject coroutineScope into AreaScreen
    content = content.replace("val areas = remember { mutableStateListOf<Area>() }", "val areas = remember { mutableStateListOf<Area>() }\n    val coroutineScope = rememberCoroutineScope()")


with open(filepath, 'w') as f:
    f.write(content)
