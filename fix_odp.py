import re

filepath = '/app/applet/app/src/main/java/com/example/ui/screens/OdpScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('import com.example.ui.data.MockOdcOdpData\n', '')
content = content.replace('val odcList by MockOdcOdpData.odcList.collectAsState()', '''
    var odcList by remember { mutableStateOf<List<com.example.ui.data.OdcItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getOdcList()
            odcList = res
        } catch (e: Exception) {
        }
    }
''')

# Fix operations on MockOdcOdpData.odpList to modify odpList directly instead
content = content.replace('MockOdcOdpData.odpList.value = MockOdcOdpData.odpList.value.filter { it.id != item.id }', 'odpList = odpList.filter { it.id != item.id }')
content = content.replace('MockOdcOdpData.odpList.value = MockOdcOdpData.odpList.value + newItem', 'odpList = odpList + newItem')
content = content.replace('MockOdcOdpData.odpList.value = MockOdcOdpData.odpList.value.map {', 'odpList = odpList.map {')

with open(filepath, 'w') as f:
    f.write(content)

