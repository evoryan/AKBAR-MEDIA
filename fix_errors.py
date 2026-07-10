import re

def fix_area_screen():
    filepath = 'app/src/main/java/com/example/ui/screens/AreaScreen.kt'
    with open(filepath, 'r') as f:
        content = f.read()
    
    # AreaItem needs currentUser passed to it
    content = content.replace("fun AreaItem(item: AreaData, onEdit: () -> Unit, onDelete: () -> Unit) {", "fun AreaItem(item: AreaData, onEdit: () -> Unit, onDelete: () -> Unit) {\n    val currentUser by UserSession.currentUser.collectAsState()")
    with open(filepath, 'w') as f:
        f.write(content)

def fix_packages_screen():
    filepath = 'app/src/main/java/com/example/ui/screens/PackagesScreen.kt'
    with open(filepath, 'r') as f:
        content = f.read()
    
    content = content.replace("fun PackageItem(item: PackageData, onEdit: () -> Unit, onDelete: () -> Unit) {", "fun PackageItem(item: PackageData, onEdit: () -> Unit, onDelete: () -> Unit) {\n    val currentUser by UserSession.currentUser.collectAsState()")
    with open(filepath, 'w') as f:
        f.write(content)

def fix_pembukuan_screen():
    filepath = 'app/src/main/java/com/example/ui/screens/PembukuanScreen.kt'
    with open(filepath, 'r') as f:
        content = f.read()
    
    if "import androidx.compose.runtime.collectAsState" not in content:
        content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")

    if "val currentUser by UserSession.currentUser.collectAsState()" not in content:
        content = content.replace("val primaryCyan = Color(0xFF00FFFF)", "val primaryCyan = Color(0xFF00FFFF)\n    val currentUser by UserSession.currentUser.collectAsState()")

    with open(filepath, 'w') as f:
        f.write(content)

def fix_botwa_screen():
    filepath = 'app/src/main/java/com/example/ui/screens/BotWaScreen.kt'
    with open(filepath, 'r') as f:
        content = f.read()
    if "import androidx.compose.runtime.collectAsState" not in content:
        content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue")
    with open(filepath, 'w') as f:
        f.write(content)

fix_area_screen()
fix_packages_screen()
fix_pembukuan_screen()
fix_botwa_screen()
