import re

filepath = '/app/applet/app/src/main/java/com/example/ui/screens/OdcScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('import com.example.ui.data.MockOdcOdpData\n', '')
content = content.replace('MockOdcOdpData.odcList.value = MockOdcOdpData.odcList.value.filter { it.id != item.id }', 'odcList = odcList.filter { it.id != item.id }')
content = content.replace('MockOdcOdpData.odcList.value = MockOdcOdpData.odcList.value + newItem', 'odcList = odcList + newItem')
content = content.replace('MockOdcOdpData.odcList.value = MockOdcOdpData.odcList.value.map {', 'odcList = odcList.map {')

with open(filepath, 'w') as f:
    f.write(content)
