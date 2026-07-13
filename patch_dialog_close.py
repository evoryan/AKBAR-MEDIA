import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

# Remove external showDialog = false
content = content.replace("                    showDialog = false\n                }) {", "                }) {")

# Add internal showDialog = false
content = content.replace("                                odcList = ApiClient.apiService.getOdcList()", "                                odcList = ApiClient.apiService.getOdcList()\n                                showDialog = false")

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("                    showDialog = false\n                }) {", "                }) {")
content = content.replace("                                odpList = ApiClient.apiService.getOdpList()", "                                odpList = ApiClient.apiService.getOdpList()\n                                showDialog = false")

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
