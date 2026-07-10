import re

# Fix DaftarAdminScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport com.example.ui.data.remote.ApiClient')
content = content.replace('editItem.id', 'editItem?.id')
content = content.replace('editItem.copy', 'editItem!!.copy')
with open(filepath, 'w') as f:
    f.write(content)


# Fix HistoryStockScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/HistoryStockScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport com.example.ui.data.remote.ApiClient')
with open(filepath, 'w') as f:
    f.write(content)


# Fix KategoriScreen
filepath = '/app/applet/app/src/main/java/com/example/ui/screens/KategoriScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()
if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport com.example.ui.data.remote.ApiClient')
with open(filepath, 'w') as f:
    f.write(content)

