import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

bad_str = 'Toast.makeText(context, "ACS Data:\nRxPower: ${acsDevice?.rxPower}\nSSID: ${acsDevice?.ssid}\nConnected: ${acsDevice?.connectedUsers}", Toast.LENGTH_LONG).show()'
good_str = 'Toast.makeText(context, "ACS Data:\\nRxPower: ${acsDevice?.rxPower}\\nSSID: ${acsDevice?.ssid}\\nConnected: ${acsDevice?.connectedUsers}", Toast.LENGTH_LONG).show()'

content = content.replace(bad_str, good_str)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
