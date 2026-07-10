filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

imports = """
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.data.remote.ApiClient
import kotlinx.coroutines.launch
"""

# Just add imports after "import androidx.compose.runtime.Composable"
content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\n" + imports)

with open(filepath, 'w') as f:
    f.write(content)

