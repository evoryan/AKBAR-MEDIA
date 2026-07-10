import os
import re

def wrap_delete(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    if "Icons.Default.Delete" not in content:
        return
        
    if "import com.example.ui.data.UserSession" not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole")
        
    if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
        func_match = re.search(r"@Composable\s*fun\s+[A-Za-z0-9_]+\(.*?\)\s*\{", content)
        if func_match:
            insert_idx = func_match.end()
            content = content[:insert_idx] + "\n    val currentUser by UserSession.currentUser.collectAsState()" + content[insert_idx:]

    # For AreaScreen and PackagesScreen, they use Row with Edit and Delete IconButtons.
    # We want to hide the Delete IconButton.
    
    # regex to match: IconButton(onClick = { ... }) { Icon(Icons.Default.Delete ... ) }
    # This might span multiple lines
    pattern = re.compile(r"(IconButton\(onClick\s*=\s*\{.*?\}\)\s*\{\s*Icon\(Icons\.Default\.Delete.*?\)\s*\})", re.DOTALL)
    
    def repl(m):
        return f"if (currentUser?.role == UserRole.SUPER_ADMIN) {{\n                                    {m.group(1)}\n                                }}"

    content = pattern.sub(repl, content)

    # Some screens use DropdownMenuItem for Delete, e.g., DropdownMenuItem(text = { Text("Hapus" ... ) }, onClick = { ... })
    # Let's check for DropdownMenuItem with "Hapus"
    hapus_pattern = re.compile(r"(DropdownMenuItem\(\s*text\s*=\s*\{\s*Text\(\"Hapus\".*?\)\s*\},.*?\))", re.DOTALL)
    content = hapus_pattern.sub(repl, content)
    
    # for AreaScreen there is also `SwipeToDismiss` or `Icon(Icons.Default.Delete` not in an IconButton.
    # Let's check where it is: AreaScreen.kt has an IconButton:
    # IconButton(onClick = { itemToDelete = item; showDeleteDialog = true }) { Icon(Icons.Default.Delete...) }
    
    with open(filepath, 'w') as f:
        f.write(content)

files = [
    'app/src/main/java/com/example/ui/screens/AreaScreen.kt',
    'app/src/main/java/com/example/ui/screens/PackagesScreen.kt',
    'app/src/main/java/com/example/ui/screens/InventoryScreen.kt',
    'app/src/main/java/com/example/ui/screens/KategoriScreen.kt',
    'app/src/main/java/com/example/ui/screens/OdcScreen.kt',
    'app/src/main/java/com/example/ui/screens/OdpScreen.kt',
    'app/src/main/java/com/example/ui/screens/BotWaScreen.kt',
]

for file in files:
    if os.path.exists(file):
        wrap_delete(file)

