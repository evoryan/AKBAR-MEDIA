filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

imports = """
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch
"""

# Insert imports
content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\n" + imports)

with open(filepath, 'w') as f:
    f.write(content)

