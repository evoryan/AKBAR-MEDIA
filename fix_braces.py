with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('                    )\n                }\n            },\n            confirmButton = {', '                    )\n                }\n            },\n            confirmButton = {') # wait let's check exact match

