import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.MikrotikSecret
import com.example.ui.data.OdpItem
import com.example.ui.data.AreaItem
"""

# Insert imports
if "LocalContext" not in content:
    content = content.replace('import androidx.compose.ui.unit.sp\n', 'import androidx.compose.ui.unit.sp\n' + imports)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
