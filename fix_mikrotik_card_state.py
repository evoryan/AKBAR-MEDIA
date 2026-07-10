filepath = 'app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)

    Box("""

replacement = """    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    
    var mikrotikStatus by remember { mutableStateOf<com.example.ui.data.remote.MikrotikStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(area.id) {
        try {
            isLoading = true
            errorMsg = null
            mikrotikStatus = com.example.ui.data.remote.ApiClient.apiService.getMikrotikStatus(area.id)
        } catch(e: Exception) {
            errorMsg = "Gagal mengambil data: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box("""

content = content.replace(target, replacement)
with open(filepath, 'w') as f:
    f.write(content)
