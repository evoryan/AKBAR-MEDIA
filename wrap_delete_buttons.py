import os
import re

screens_dir = 'app/src/main/java/com/example/ui/screens'

for filename in os.listdir(screens_dir):
    if not filename.endswith('.kt'):
        continue
    filepath = os.path.join(screens_dir, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    if "Icons.Default.Delete" in content and "DaftarAdminScreen" not in filename:
        # Add imports if needed
        if "import com.example.ui.data.UserSession" not in content:
            content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.UserSession\nimport com.example.ui.data.UserRole")
            
        if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
            # Inject collecting user state in the main Composable
            # We'll just look for standard definitions, e.g., @Composable\nfun XXX(
            func_match = re.search(r"@Composable\s+fun\s+[A-Za-z0-9_]+\(.*?\)\s*\{", content)
            if func_match:
                insert_idx = func_match.end()
                content = content[:insert_idx] + "\n    val currentUser by UserSession.currentUser.collectAsState()" + content[insert_idx:]

        # Now find IconButton with Delete icon
        # This is tricky with regex because of nested braces, we can find:
        # IconButton(onClick = { ... }) { ... Icon(Icons.Default.Delete ... ) ... }
        # And wrap it with if (UserSession.hasDeletePrivilege()) { ... }
        
        # A simpler way since these are standard:
        # Let's find "IconButton(onClick" that precedes "Icons.Default.Delete" closely
        # Actually, let's do it manually for the files found to be safe: AreaScreen, PackagesScreen, InventoryScreen, KategoriScreen, OdcScreen, OdpScreen
