import re

filepath = 'app/src/main/java/com/example/ui/screens/MikrotikScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """@Composable
fun MikrotikCard(area: Area, onNavigateToManageSecrets: () -> Unit) {
    val cardBg = Color(0xFF11111A)
    val neonCyan = Color(0xFF00FFFF)
    val cardBorder = neonCyan.copy(alpha = 0.3f)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    
    Box("""

replacement = """@Composable
fun MikrotikCard(area: Area, onNavigateToManageSecrets: () -> Unit) {
    val cardBg = Color(0xFF11111A)
    val neonCyan = Color(0xFF00FFFF)
    val cardBorder = neonCyan.copy(alpha = 0.3f)
    val textMain = Color(0xFFFFFFFF)
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

# Replace the hardcoded stats with variables
target_stats = """                    Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(48.dp), tint = neonCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Connected (${area.routerIp})", color = neonCyan, fontSize = 14.sp)
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MikrotikStatCard("CPU Load", "12%", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                MikrotikStatCard("Uptime", "14d 2h", neonCyan, textMain, textSecondary, Modifier.weight(1f))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MikrotikStatCard("Active PPPoE", "842", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                MikrotikStatCard("PPPoE Offline", "120", neonCyan, textMain, textSecondary, Modifier.weight(1f))
            }"""

replacement_stats = """                    Icon(Icons.Default.Router, contentDescription = null, modifier = Modifier.size(48.dp), tint = neonCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if (isLoading) "Connecting to ${area.routerIp}..." else if (errorMsg != null) "Error" else "Connected (${area.routerIp})", color = if (errorMsg != null) Color(0xFFFF003C) else neonCyan, fontSize = 14.sp)
                    }
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = neonCyan)
                }
            } else if (errorMsg != null) {
                Text(errorMsg!!, color = Color(0xFFFF003C), fontSize = 14.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MikrotikStatCard("CPU Load", mikrotikStatus?.cpuLoad ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                    MikrotikStatCard("Uptime", mikrotikStatus?.uptime ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MikrotikStatCard("Active PPPoE", mikrotikStatus?.activePppoe ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                    MikrotikStatCard("PPPoE Offline", mikrotikStatus?.offlinePppoe ?: "-", neonCyan, textMain, textSecondary, Modifier.weight(1f))
                }
            }"""

content = content.replace(target_stats, replacement_stats)

with open(filepath, 'w') as f:
    f.write(content)
