import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target_imports = """import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch"""

replacement_imports = """import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.padding
"""

content = content.replace(target_imports, replacement_imports)

content = content.replace("androidx.compose.material.icons.Icons.Default.Edit", "Icons.Default.Edit")
content = content.replace("androidx.compose.material.icons.Icons.Default.Delete", "Icons.Default.Delete")

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
