filepath = 'app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# add showAddDialog state
target_scaffold = """    val textMain = Color(0xFFFFFFFF)
    val cardBg = Color(0xFF11111A)

    Scaffold(
        containerColor = Color.Transparent,"""

replacement_scaffold = """    val textMain = Color(0xFFFFFFFF)
    val cardBg = Color(0xFF11111A)
    
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = bgMain,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00FFFF),
                contentColor = Color.Black,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Mikrotik")
            }
        },"""

content = content.replace(target_scaffold, replacement_scaffold)

# add dialog to the end of MikrotikScreen, before MikrotikCard
target_end = """        }
    }
}

@Composable
fun MikrotikCard"""

replacement_end = """        }
        
        if (showAddDialog) {
            AreaFormDialog(
                initialArea = null,
                onDismiss = { showAddDialog = false },
                onSave = { newArea ->
                    coroutineScope.launch {
                        try {
                            com.example.ui.data.remote.ApiClient.apiService.addArea(newArea)
                            val res = com.example.ui.data.remote.ApiClient.apiService.getAreas()
                            areas = res.filter { it.routerIp != null && it.routerIp.isNotEmpty() }
                        } catch(e: Exception) {}
                    }
                    showAddDialog = false
                },
                bgMain = bgMain,
                textMain = textMain,
                textSecondary = Color(0xFFAAAAAA),
                primaryPurple = Color(0xFF2B0B3F),
                neonCyan = Color(0xFF00FFFF)
            )
        }
    }
}

@Composable
fun MikrotikCard"""

content = content.replace(target_end, replacement_end)

if "import androidx.compose.material.icons.filled.Add" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Router", "import androidx.compose.material.icons.filled.Router\nimport androidx.compose.material.icons.filled.Add")

if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

with open(filepath, 'w') as f:
    f.write(content)
