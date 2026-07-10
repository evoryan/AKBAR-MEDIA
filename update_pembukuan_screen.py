import re

filepath = 'app/src/main/java/com/example/ui/screens/PembukuanScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add imports
if "com.example.ui.data.UserSession" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole")

if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
    content = content.replace("var showMonthPicker by remember { mutableStateOf(false) }", "var showMonthPicker by remember { mutableStateOf(false) }\n    val currentUser by UserSession.currentUser.collectAsState()")

# We need to selectively render Pemasukan, Pengeluaran, Total Pendapatan based on role
# item { Card ... Pemasukan } -> if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) { item { Card ... } }
# Let's use regex or string replace.

def conditional_wrap_item(content, search_str, end_str, condition):
    start_idx = content.find(search_str)
    if start_idx == -1: return content
    # Find the end of the item block. We can just find the next item comment.
    end_idx = content.find(end_str, start_idx)
    if end_idx == -1: return content
    block = content[start_idx:end_idx]
    wrapped = f"if ({condition}) {{\n            {block}\n            }}\n            "
    return content[:start_idx] + wrapped + content[end_idx:]

content = conditional_wrap_item(content, "item {\n                Card(\n                    modifier = Modifier.fillMaxWidth(),\n                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))\n                ) {\n                    Column(modifier = Modifier.padding(16.dp)) {\n                        Row(\n                            modifier = Modifier.fillMaxWidth(),\n                            horizontalArrangement = Arrangement.SpaceBetween", "// Pengeluaran", "currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN")

content = conditional_wrap_item(content, "// Pengeluaran\n            item {\n                Card(\n                    modifier = Modifier.fillMaxWidth(),\n                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))\n                ) {\n                    Column(modifier = Modifier.padding(16.dp)) {\n                        Row(\n                            modifier = Modifier.fillMaxWidth(),\n                            horizontalArrangement = Arrangement.SpaceBetween", "// Total Pendapatan", "currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN")

content = conditional_wrap_item(content, "// Total Pendapatan\n            item {\n                Card(\n                    modifier = Modifier.fillMaxWidth(),\n                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A))\n                ) {\n                    Column(modifier = Modifier.padding(16.dp)) {\n                        Text(\"Total Pendapatan\"", "// Menu Lain", "currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN")

# For Menu Lain:
menu_lain_replacement = """// Menu Lain
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Menu Lain",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                            MenuLainItem(
                                icon = Icons.Default.AccountCircle,
                                title = "Uang di Admin",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToUangDiAdmin
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        MenuLainItem(
                            icon = Icons.Default.List,
                            title = "Semua Pembukuan",
                            tint = Color(0xFF03A9F4),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToSemuaPembukuan
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.COLLECTOR) {
                            MenuLainItem(
                                icon = Icons.Default.DirectionsRun,
                                title = "Pembyrn By Admin",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToPembayaranByAdmin
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        if (currentUser?.role == UserRole.SUPER_ADMIN || currentUser?.role == UserRole.ADMIN) {
                            MenuLainItem(
                                icon = Icons.Default.PieChart,
                                title = "Rangkuman Keuangan",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.weight(1f),
                                onClick = onNavigateToRangkuman
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
"""

start_idx = content.find("// Menu Lain")
if start_idx != -1:
    end_idx = content.find("        }\n    }\n}")
    if end_idx != -1:
        content = content[:start_idx] + menu_lain_replacement + content[end_idx:]

with open(filepath, 'w') as f:
    f.write(content)
