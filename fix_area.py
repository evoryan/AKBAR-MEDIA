filepath = '/app/applet/app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('val mikrotikApiAddress: String = "",', 'val routerIp: String = "",')
content = content.replace('val acsApiAddress: String = ""', 'val apiDomain: String = ""')
content = content.replace('mikrotikApiAddress', 'routerIp')
content = content.replace('acsApiAddress', 'apiDomain')

with open(filepath, 'w') as f:
    f.write(content)

