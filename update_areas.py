import re

filepath = 'app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import com.example.ui.data.remote.ApiClient" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.remote.ApiClient\nimport androidx.compose.runtime.LaunchedEffect")

pattern = re.compile(r"val\s+areas\s*=\s*MockData\.areas", re.DOTALL)
replacement = """val areas = remember { mutableStateListOf<Area>() }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAreas()
            areas.clear()
            areas.addAll(res)
        } catch (e: Exception) {
            // Handle error
        }
    }"""
content = pattern.sub(replacement, content, count=1)

with open(filepath, 'w') as f:
    f.write(content)

