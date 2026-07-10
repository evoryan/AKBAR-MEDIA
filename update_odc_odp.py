import re
import os

def update_odc():
    filepath = 'app/src/main/java/com/example/ui/screens/OdcScreen.kt'
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        content = f.read()

    if "import com.example.ui.data.remote.ApiClient" not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.remote.ApiClient\nimport androidx.compose.runtime.LaunchedEffect")

    # Replace `val odcList by MockOdcOdpData.odcList.collectAsState()`
    pattern = re.compile(r"val\s+odcList\s+by\s+MockOdcOdpData\.odcList\.collectAsState\(\)", re.DOTALL)
    replacement = """var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdcList()
            odcList = res
        } catch (e: Exception) {
        }
    }"""
    content = pattern.sub(replacement, content, count=1)

    with open(filepath, 'w') as f:
        f.write(content)

def update_odp():
    filepath = 'app/src/main/java/com/example/ui/screens/OdpScreen.kt'
    if not os.path.exists(filepath): return
    with open(filepath, 'r') as f:
        content = f.read()

    if "import com.example.ui.data.remote.ApiClient" not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.remote.ApiClient\nimport androidx.compose.runtime.LaunchedEffect")

    # Replace `val odpList by MockOdcOdpData.odpList.collectAsState()`
    pattern = re.compile(r"val\s+odpList\s+by\s+MockOdcOdpData\.odpList\.collectAsState\(\)", re.DOTALL)
    replacement = """var odpList by remember { mutableStateOf<List<com.example.ui.data.OdpItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdpList()
            odpList = res
        } catch (e: Exception) {
        }
    }"""
    content = pattern.sub(replacement, content, count=1)

    with open(filepath, 'w') as f:
        f.write(content)

update_odc()
update_odp()
