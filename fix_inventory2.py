import re

filepath = '/app/applet/app/src/main/java/com/example/ui/screens/InventoryScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\nimport com.example.ui.data.remote.ApiClient')

content = re.sub(r'id = System\.currentTimeMillis\(\)\.toString\(\),\s*type =.*?\s*itemName = name,\s*quantity =.*?,\s*adminName = "Admin",\s*timestamp = System\.currentTimeMillis\(\)\s*', '', content, flags=re.DOTALL)

with open(filepath, 'w') as f:
    f.write(content)

