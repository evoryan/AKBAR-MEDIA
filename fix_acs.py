import re

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """
import com.example.ui.screens.Area
import com.example.ui.data.remote.ApiClient
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
"""

if "import com.example.ui.screens.Area" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch" + imports_to_add)

# Add states inside AcsScreen
states_to_add = """
    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var expandedAreaDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAreas()
            areas = res.filter { it.apiDomain != null && it.apiDomain.isNotEmpty() }
            if (areas.isNotEmpty()) {
                selectedArea = areas.first()
            }
        } catch(e: Exception) {
        }
    }
"""

content = re.sub(r'(var showOnlyOffline by remember \{ mutableStateOf\(false\) \})', r'\1\n' + states_to_add, content)

# Add the dropdown UI before Summary Cards
dropdown_ui = """
            // Area Selector
            if (areas.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedAreaDropdown = !expandedAreaDropdown },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                        border = androidx.compose.foundation.BorderStroke(1.dp, textSecondary)
                    ) {
                        Text(selectedArea?.name ?: "Pilih Area ACS", modifier = Modifier.weight(1f))
                        Icon(if (expandedAreaDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedAreaDropdown,
                        onDismissRequest = { expandedAreaDropdown = false },
                        modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                    ) {
                        areas.forEach { area ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(area.name, color = textMain, fontWeight = FontWeight.Bold)
                                        Text(area.apiDomain ?: "", color = textSecondary, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedArea = area
                                    expandedAreaDropdown = false
                                }
                            )
                        }
                    }
                }
                
                if (selectedArea != null) {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF1F0216)).border(1.dp, primaryCyan.copy(alpha=0.3f), RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Column {
                            Text("ACS URL: ${selectedArea?.apiDomain}", color = primaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Username: ${selectedArea?.acsUser}", color = textSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF2B0B3F)).padding(12.dp)) {
                    Text("Belum ada ACS yang didaftarkan. Silakan tambahkan di menu Area.", color = textMain, fontSize = 14.sp)
                }
            }
            
            // Summary Cards
"""

content = content.replace("// Summary Cards", dropdown_ui)

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'w') as f:
    f.write(content)

